package com.hdfcbank.pacs004.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hdfcbank.pacs004.exception.NILException;
import com.hdfcbank.pacs004.model.ReqPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Component
public class NILRouterCommonUtility {

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Parses an XML string into a Document object with namespace awareness.
     */
    public static Document parseXmlStringToDocument(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }

    /**
     * Converts a Document object to its XML string representation.
     */
    public static String documentToXmlString(Document doc) throws Exception {
        StringWriter writer = new StringWriter();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }
    public ReqPayload convertToMap(String request) {
        try {

            if (request == null || request.trim().isEmpty()) {
                return null;
            }

            String base64 = Base64.getEncoder().encodeToString(request.getBytes(StandardCharsets.UTF_8));
            String requestJson = "{\"data_base64\":\"" + base64 + "\"}";
            JsonNode rootNode = objectMapper.readTree(requestJson);
            String base64Data = rootNode.get("data_base64").asText();
            String reqPayloadString = new String(Base64.getDecoder().decode(base64Data), StandardCharsets.UTF_8);
            return objectMapper.readValue(reqPayloadString,  ReqPayload.class);
        } catch (Exception e) {
            log.error("Failed to convert request string to map", e);
            throw new NILException("Invalid request format. Expecting JSON object.", e);
        }
    }

}
