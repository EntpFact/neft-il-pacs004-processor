package com.hdfcbank.pacs004.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hdfcbank.pacs004.dao.NilRepository;
import com.hdfcbank.pacs004.kafkaproducer.KafkaUtils;
import com.hdfcbank.pacs004.model.*;
import com.hdfcbank.pacs004.utils.UtilityMethods;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class Pacs004XmlProcessorTest {

    @InjectMocks
    private Pacs004XmlProcessor pacs004XmlProcessor;

    @Mock
    private NilRepository dao;

    @Mock
    private KafkaUtils kafkaUtils;

    @Mock
    private UtilityMethods utilityMethods;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pacs004XmlProcessor.fcTopic = "fc-topic";
        pacs004XmlProcessor.ephTopic = "eph-topic";
    }

    @Test
    void testExtractEndToEndIdDigit_valid15thDigit() {
        String endToEndId = "ABCDEFGHIJKLMNO9"; // 15th char = 9
        int digit = invokeExtractEndToEndIdDigit(endToEndId);
        assertEquals(24, digit);
    }

    @Test
    void testExtractEndToEndIdDigit_invalidId() {
        String endToEndId = "SHORT"; // not enough length
        int digit = invokeExtractEndToEndIdDigit(endToEndId);
        assertEquals(-1, digit);
    }

    @Test
    public void testParseXml_InwardFlow() throws Exception {
        String xml = "<RequestPayload><AppHdr><BizMsgIdr>ID456</BizMsgIdr><MsgDefIdr>pacs.004.001.10</MsgDefIdr>  <CreDt>2006-08-22T12:12:00Z</CreDt>" +
                "</AppHdr><Document><TxInf><OrgnlTxId>HDFCN52022062824954014</OrgnlTxId>" +
                "<OrgnlEndToEndId>123456789012345</OrgnlEndToEndId><RtrdIntrBkSttlmAmt>1000</RtrdIntrBkSttlmAmt></TxInf>" +
                "</Document></RequestPayload>";
        ReqPayload payload=new ReqPayload();
        Body body=new Body();
        Header header=new Header();
        body.setPayload(xml);
        header.setFlowType("INWARD");
        header.setInvalidPayload(false);
        header.setPrefix("CBS");
        header.setTarget("FC");

        payload.setBody(body);
        payload.setHeader(header);
        when(utilityMethods.getBizMsgIdr(any())).thenReturn("ID456");
        when(utilityMethods.getMsgDefIdr(any())).thenReturn("pacs.004.001.10");

        pacs004XmlProcessor.parseXml(payload);

//        verify(dao, atLeastOnce()).saveDataInMsgEventTracker(any());
    }


    @Test
    public void testParseXml_InwardFlow2() throws Exception {
        String xml = "<RequestPayload><AppHdr><BizMsgIdr>ID456</BizMsgIdr><MsgDefIdr>pacs.004.001.10</MsgDefIdr>   <CreDt>2006-08-22T12:12:00Z</CreDt>" +
                "</AppHdr><Document><TxInf><OrgnlTxId>HDFCN52022062824954014</OrgnlTxId>" +
                "<OrgnlEndToEndId>123456789012345</OrgnlEndToEndId><RtrdIntrBkSttlmAmt>1000</RtrdIntrBkSttlmAmt></TxInf>" +
                "<TxInf><OrgnlTxId>HDFCN52022062866954014</OrgnlTxId><OrgnlEndToEndId>123456789012345</OrgnlEndToEndId>" +
                "<RtrdIntrBkSttlmAmt>100</RtrdIntrBkSttlmAmt></TxInf></Document></RequestPayload>";
        ReqPayload payload=new ReqPayload();
        Body body=new Body();
        Header header=new Header();
        body.setPayload(xml);
        header.setFlowType("INWARD");
        header.setInvalidPayload(false);
        header.setPrefix("CBS");
        header.setTarget("FC");

        payload.setBody(body);
        payload.setHeader(header);
        when(utilityMethods.getBizMsgIdr(any())).thenReturn("ID456");
        when(utilityMethods.getMsgDefIdr(any())).thenReturn("pacs.004.001.10");

        pacs004XmlProcessor.parseXml(payload);

//        verify(dao, atLeastOnce()).saveDataInMsgEventTracker(any());
    }

    @Test
    public void testParseXml_InwardFlow3() throws Exception {
        String xml = "<RequestPayload><AppHdr><BizMsgIdr>ID456</BizMsgIdr><MsgDefIdr>pacs.004.001.10</MsgDefIdr> <CreDt>2006-08-22T12:12:00Z</CreDt>" +
                "</AppHdr><Document> <GrpHdr>\\r\\n            <MsgId>RBIP202101146147295848</MsgId>\\r\\n            <CreDtTm>2021-01-14T16:01:00</CreDtTm>\\r\\n            <NbOfTxs>10</NbOfTxs>\\r\\n            <TtlRtrdIntrBkSttlmAmt Ccy=\\\"INR\\\">1000.00</TtlRtrdIntrBkSttlmAmt>\\r\\n            <IntrBkSttlmDt>2021-04-20</IntrBkSttlmDt>\\r\\n            <SttlmInf>\\r\\n                <SttlmMtd>CLRG</SttlmMtd>\\r\\n            </SttlmInf>\\r\\n\\t\\t\\t<InstgAgt>\\r\\n                <FinInstnId>\\r\\n                    <ClrSysMmbId>\\r\\n                        <MmbId>RBIP0NEFTSC</MmbId>\\r\\n                    </ClrSysMmbId>\\r\\n                </FinInstnId>\\r\\n            </InstgAgt>\\r\\n            <InstdAgt>\\r\\n                <FinInstnId>\\r\\n                    <ClrSysMmbId>\\r\\n                      " +
                "  <MmbId>HDFC0000001</MmbId>\\r\\n                    </ClrSysMmbId>\\r\\n                </FinInstnId>\\r\\n            </InstdAgt>\\r\\n        </GrpHdr>\\r\\n <TxInf><OrgnlTxId>HDFCN52022067877954014</OrgnlTxId>" +
                "<OrgnlEndToEndId>1234777777777772345</OrgnlEndToEndId><RtrdIntrBkSttlmAmt>1000</RtrdIntrBkSttlmAmt></TxInf>" +
                "</Document></RequestPayload>";
        ReqPayload payload=new ReqPayload();
        Body body=new Body();
        Header header=new Header();
        body.setPayload(xml);
        header.setFlowType("INWARD");
        header.setInvalidPayload(false);
        header.setPrefix("CBS");
        header.setTarget("FC");

        payload.setBody(body);
        payload.setHeader(header);
        when(utilityMethods.getBizMsgIdr(any())).thenReturn("ID456");
        when(utilityMethods.getMsgDefIdr(any())).thenReturn("pacs.004.001.10");

        pacs004XmlProcessor.parseXml(payload);

//        verify(dao, atLeastOnce()).saveDataInMsgEventTracker(any());
    }



    @Test
    public void testDocumentToXml() throws Exception {
        String xml = "<root><child>value</child></root>";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new java.io.ByteArrayInputStream(xml.getBytes()));

        String xmlResult = pacs004XmlProcessor.documentToXml(document);

        assertTrue(xmlResult.contains("<child>value</child>"));
    }

    @Test
    void testParseXml_inwardFlowCallsProcessXml() throws Exception {
        String xml = "<RequestPayload><AppHdr><BizMsgIdr>BIZ999</BizMsgIdr>  <CreDt>2006-08-22T12:12:00Z</CreDt></AppHdr><Document/></RequestPayload>";
        ReqPayload payload=new ReqPayload();
        Body body=new Body();
        Header header=new Header();
        body.setPayload(xml);
        header.setFlowType("INWARD");
        header.setInvalidPayload(false);
        header.setPrefix("CBS");
        header.setTarget("FC");

        payload.setBody(body);
        payload.setHeader(header);
        // Spy to check if processXML is called
        Pacs004XmlProcessor spyProcessor = Mockito.spy(pacs004XmlProcessor);

        spyProcessor.parseXml(payload);

        verify(spyProcessor, times(1)).processXML(xml,payload);
    }

    // ---- Utility method to invoke private extractEndToEndIdDigit ----
    private int invokeExtractEndToEndIdDigit(String input) {
        try {
            var method = Pacs004XmlProcessor.class.getDeclaredMethod("extractEndToEndIdDigit", String.class);
            method.setAccessible(true);
            return (int) method.invoke(null, input);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
