package com.hdfcbank.pacs004.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hdfcbank.pacs004.dao.NilRepository;
import com.hdfcbank.pacs004.kafkaproducer.KafkaUtils;
import com.hdfcbank.pacs004.model.*;
import com.hdfcbank.pacs004.utils.Constants;
import com.hdfcbank.pacs004.utils.UtilityMethods;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.hdfcbank.pacs004.utils.Constants.*;

@Slf4j
@Service
public class Pacs004XmlProcessor {

    @Autowired
    NilRepository dao;
    @Autowired
    UtilityMethods utilityMethods;
    @Autowired
    KafkaUtils kafkautils;
    @Autowired
    ErrorHandling errorHandling;

    @Value("${topic.fctopic}")
    String fcTopic;
    @Value("${topic.ephtopic}")
    String ephTopic;

    public void parseXml(ReqPayload payload) throws Exception {
        Optional.ofNullable(payload.getHeader())
                .filter(h -> StringUtils.equalsIgnoreCase(INWARD, h.getFlowType()))
                .ifPresent(h -> processXML(payload.getBody().getPayload(), payload));
    }

    public void processXML(String xml, ReqPayload payload) {
        try {
            Document document = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xml)));

            XPath xpath = XPathFactory.newInstance().newXPath();

            String bizMsgIdr = xpath.evaluate(BIZMSGID_XPATH, document);
            String batchCreationTime = xpath.evaluate(BATCH_CREDT_XPATH, document);

            LocalDateTime batchCreationTimeStamp = LocalDateTime.parse(batchCreationTime, DateTimeFormatter.ISO_DATE_TIME);
            LocalDate batchCreationDate = Instant.parse(batchCreationTime).atZone(ZoneId.of("UTC")).toLocalDate();

            NodeList txInfNodes = (NodeList) xpath.evaluate(
                    CDTTRFTXINF_XPATH,
                    document, XPathConstants.NODESET);

            List<Pacs004Fields> pacs004 = IntStream.range(0, txInfNodes.getLength())
                    .mapToObj(i -> (Element) txInfNodes.item(i))
                    .map(el -> {
                        try {
                            String orgnlTxId = xpath.evaluate(ORGNL_TXN_XPATH, el);
                            String endToEndId = xpath.evaluate(END_TO_END_XPATH, el);
                            String amount = xpath.evaluate(AMOUNT_XPATH, el);

                            String batchId = Optional.ofNullable(
                                            (NodeList) xpath.evaluate(BATCH_ID_XPATH,
                                                    el, XPathConstants.NODESET))
                                    .map(list -> IntStream.range(0, list.getLength())
                                            .mapToObj(list::item)
                                            .map(Node::getTextContent)
                                            .filter(text -> text.contains(BATCH_ID))
                                            .findFirst()
                                            .orElse(""))
                                    .orElse("");

                            int digit = extractEndToEndIdDigit(orgnlTxId);
                            String swtch = (digit >= 0 && digit <= 4) ? "DISPATCHED_FC" : "DISPATCHED_EPH";

                            return new Pacs004Fields(bizMsgIdr, endToEndId, orgnlTxId, amount, swtch, batchId);

                        } catch (Exception e) {
                            log.error("Error parsing TxInf node", e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // partition into FC vs EPH
            Map<Boolean, List<Pacs004Fields>> partitioned = pacs004.stream()
                    .collect(Collectors.partitioningBy(f -> "DISPATCHED_FC".equals(f.getSwtch())));

            List<Pacs004Fields> fcList = partitioned.get(true);
            List<Pacs004Fields> ephList = partitioned.get(false);

            double consolidateAmountFC = fcList.stream().mapToDouble(f -> Double.parseDouble(f.getAmount())).sum();
            double consolidateAmountEPH = ephList.stream().mapToDouble(f -> Double.parseDouble(f.getAmount())).sum();

            boolean invalidReq = payload.getHeader().isInvalidPayload();
            String flowType = payload.getHeader().getFlowType();
            String prefix = payload.getHeader().getPrefix();

            if (!fcList.isEmpty()) {
                processGroup(payload,document, xml, prefix, batchCreationDate, batchCreationTimeStamp,
                        bizMsgIdr, consolidateAmountFC, fcList, "DISPATCHER_FC", fcTopic, invalidReq, flowType);
            }

            if (!ephList.isEmpty()) {
                processGroup(payload,document, xml, prefix, batchCreationDate, batchCreationTimeStamp,
                        bizMsgIdr, consolidateAmountEPH, ephList, "DISPATCHER_EPH", ephTopic, invalidReq, flowType);
            }

            // Save transaction audits
            List<TransactionAudit> audits = pacs004.stream()
                    .map(f -> {
                        try {
                            return
                                    buildTransactionAudit(document, xml, f, batchCreationDate, batchCreationTimeStamp);
                        } catch (XPathExpressionException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());

            dao.saveAllTransactionAudits(audits);

        } catch (Exception e) {
            log.error("Exception in processXML", e);
        }
    }

    private void processGroup(ReqPayload payload,Document document, String xml, String prefix, LocalDate batchDate,
                              LocalDateTime batchTime, String bizMsgIdr, double consolidateAmt,
                              List<Pacs004Fields> list, String target, String topic,
                              boolean invalidReq, String flowType) throws Exception {

        Document filteredDoc = filterOrgnlItmAndSts(document,
                "DISPATCHER_FC".equals(target) ? 0 : 5,
                "DISPATCHER_FC".equals(target) ? 4 : 9,
                list.size(), consolidateAmt);

        String outputDocString = documentToXml(filteredDoc);
        log.info("{} : {}", target, outputDocString);

        MsgEventTracker tracker = new MsgEventTracker();
        tracker.setMsgId(bizMsgIdr);
        tracker.setSource(SFMS);
        tracker.setTarget(target);
        tracker.setFlowType(flowType);
        tracker.setInvalidPayload(invalidReq);
        tracker.setIntermediateReq(prefix + outputDocString);
        tracker.setIntermediateCount(list.size());
        tracker.setStatus(Constants.SENT_TO_DISPATCHER);
        tracker.setOrgnlReqCount(list.size());
        tracker.setBatchId(list.get(0).getBatchId());
        tracker.setBatchCreationTime(batchTime);
        tracker.setBatchCreationDate(batchDate);
        tracker.setConsolidateAmt(BigDecimal.valueOf(consolidateAmt));
        tracker.setMsgType(utilityMethods.getMsgDefIdr(document));
        tracker.setTransformedJsonReq(payload);
        tracker.setOrgnlReq(prefix + xml);

        dao.saveDataInMsgEventTracker(tracker);
        kafkautils.publishToResponseTopic(outputDocString, topic,bizMsgIdr);
    }

    private TransactionAudit buildTransactionAudit(Document doc, String xml,
                                                   Pacs004Fields field, LocalDate batchDate,
                                                   LocalDateTime batchTime) throws XPathExpressionException {
        TransactionAudit tx = new TransactionAudit();
        tx.setMsgId(utilityMethods.getBizMsgIdr(doc));
        tx.setEndToEndId(field.getEndToEndId());
        tx.setTxnId(field.getTxId());
        tx.setMsgType(PACS04);
        tx.setSource(SFMS);
        tx.setBatchId(field.getBatchId());
        tx.setAmount(BigDecimal.valueOf(Double.parseDouble(field.getAmount())));
        tx.setTarget(field.getSwtch());
        tx.setFlowType(INWARD);
        tx.setBatchDate(batchDate);
        tx.setBatchTime(batchTime);
        tx.setReqPayload(xml);
        return tx;
    }

    private static Document filterOrgnlItmAndSts(Document document, int minDigit, int maxDigit, int count, double total) throws Exception {
        Document newDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = (Element) newDoc.importNode(document.getDocumentElement(), false);
        newDoc.appendChild(root);

        XPath xpath = XPathFactory.newInstance().newXPath();

        // Copy AppHdr
        Optional.ofNullable((NodeList) xpath.evaluate(HEADER_XPATH,
                        document, XPathConstants.NODESET))
                .ifPresent(list -> IntStream.range(0, list.getLength())
                        .mapToObj(list::item)
                        .forEach(node -> root.appendChild(newDoc.importNode(node, true))));

        // Copy Document with filtered TxInf
        NodeList documentList = (NodeList) xpath.evaluate(DOCUMENT_XPATH,
                document, XPathConstants.NODESET);
        if (documentList.getLength() > 0) {
            Element originalDoc = (Element) documentList.item(0);
            Element newDocElement = (Element) newDoc.importNode(originalDoc, false);
            root.appendChild(newDocElement);

            Element pmtRtr = createElementNS(newDoc, "PmtRtr");
            newDocElement.appendChild(pmtRtr);

            // Update GrpHdr
            NodeList grpHdrList = (NodeList) xpath.evaluate(GROUP_HEADER_XPATH, originalDoc, XPathConstants.NODESET);
            if (grpHdrList.getLength() > 0) {
                Element grpHdr = (Element) grpHdrList.item(0);
                Optional.ofNullable(grpHdr.getElementsByTagNameNS("*", "NbOfTxs").item(0))
                        .ifPresent(n -> n.setTextContent(String.valueOf(count)));
                Optional.ofNullable(grpHdr.getElementsByTagNameNS("*", "TtlRtrdIntrBkSttlmAmt").item(0))
                        .ifPresent(n -> n.setTextContent(String.valueOf(total)));

                pmtRtr.appendChild(newDoc.importNode(grpHdr, true));
            }

            // Filter TxInf
            NodeList txInf = (NodeList) xpath.evaluate(".//*[local-name()='TxInf']", originalDoc, XPathConstants.NODESET);
            List<Element> filtered = IntStream.range(0, txInf.getLength())
                    .mapToObj(txInf::item)
                    .map(n -> (Element) n)
                    .filter(el -> {
                        try {
                            String txId = xpath.evaluate("./*[local-name()='OrgnlTxId']", el);
                            int digit = extractEndToEndIdDigit(txId);
                            return digit >= minDigit && digit <= maxDigit;
                        } catch (XPathExpressionException e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());

            if (!filtered.isEmpty()) {
                Element newTxInf = createElementNS(newDoc, "Info");
                filtered.forEach(el -> newTxInf.appendChild(newDoc.importNode(el, true)));
                pmtRtr.appendChild(newTxInf);
            }
        }

        newDoc.setXmlStandalone(true);
        return newDoc;
    }

    public Boolean validateRequest(ReqPayload request) throws JsonProcessingException {
        Boolean isValid =  request.getHeader().isInvalidPayload();

        if(!isValid){
            errorHandling.handleInvalidPayload(request);
        }
        return isValid;
    }

    public String documentToXml(Document doc) throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }

    private static Element createElementNS(Document doc, String localName) {
        final String PACS_NS = "urn:iso:std:iso:20022:tech:xsd:pacs.004.001.10";
        return doc.createElementNS(PACS_NS, localName);
    }

    private static int extractEndToEndIdDigit(String endToEndId) {
        return Optional.ofNullable(endToEndId)
                .filter(id -> Pattern.compile("^.{14}(.)").matcher(id).find())
                .map(id -> Character.getNumericValue(id.charAt(14)))
                .orElse(-1);
    }
}
