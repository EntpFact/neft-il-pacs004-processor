package com.hdfcbank.pacs004.utils;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;

import static org.junit.jupiter.api.Assertions.*;

class NILRouterCommonUtilityTest {

    @Test
    void testParseXmlStringToDocument_validXml() throws Exception {
        String xml = "<root><child>value</child></root>";
        Document doc = NILRouterCommonUtility.parseXmlStringToDocument(xml);

        assertNotNull(doc);
        assertEquals("root", doc.getDocumentElement().getNodeName());
        assertEquals("child", doc.getDocumentElement().getFirstChild().getNodeName());
        assertEquals("value", doc.getDocumentElement().getFirstChild().getTextContent());
    }

//    @Test
    void testParseXmlStringToDocument_invalidXml() {
        String invalidXml = "<root><unclosed></root>";

        Exception exception = assertThrows(Exception.class, () ->
                NILRouterCommonUtility.parseXmlStringToDocument(invalidXml)
        );

        assertTrue(exception.getMessage().contains("XML"));
    }

    @Test
    void testDocumentToXmlString_validDocument() throws Exception {
        String xml = "<root><child>value</child></root>";
        Document doc = NILRouterCommonUtility.parseXmlStringToDocument(xml);
        String xmlOutput = NILRouterCommonUtility.documentToXmlString(doc);

        assertNotNull(xmlOutput);
        assertTrue(xmlOutput.contains("<root>"));
        assertTrue(xmlOutput.contains("<child>value</child>"));
    }

//    @Test
    void testDocumentToXmlString_nullDocument() {
        assertThrows(NullPointerException.class, () ->
                NILRouterCommonUtility.documentToXmlString(null)
        );
    }
}
