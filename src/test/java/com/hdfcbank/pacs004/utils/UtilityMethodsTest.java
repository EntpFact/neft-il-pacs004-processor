package com.hdfcbank.pacs004.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import javax.xml.xpath.XPathExpressionException;
import javax.xml.parsers.DocumentBuilderFactory;

import static org.junit.jupiter.api.Assertions.*;

class UtilityMethodsTest {

    private UtilityMethods utilityMethods;

    @BeforeEach
    void setUp() {
        utilityMethods = new UtilityMethods();
    }

    private Document loadXml(String xmlContent) throws Exception {
        var factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        var builder = factory.newDocumentBuilder();
        return builder.parse(new org.xml.sax.InputSource(new java.io.StringReader(xmlContent)));
    }

    @Test
    void testGetBizMsgIdr_validXml() throws Exception {
        String xml = """
            <Envelope xmlns="urn:iso:std:iso:20022:tech:xsd:head.001.001.01">
                <AppHdr>
                    <BizMsgIdr>ABC123456789</BizMsgIdr>
                </AppHdr>
            </Envelope>
        """;
        Document doc = loadXml(xml);
        String result = utilityMethods.getBizMsgIdr(doc);
        assertEquals("ABC123456789", result);
    }

    @Test
    void testGetMsgDefIdr_validXml() throws Exception {
        String xml = """
            <Envelope xmlns="urn:iso:std:iso:20022:tech:xsd:head.001.001.01">
                <AppHdr>
                    <MsgDefIdr>pacs.004.001.09</MsgDefIdr>
                </AppHdr>
            </Envelope>
        """;
        Document doc = loadXml(xml);
        String result = utilityMethods.getMsgDefIdr(doc);
        assertEquals("pacs.004.001.09", result);
    }

    @Test
    void testGetBizMsgIdr_missingNode() throws Exception {
        String xml = """
            <Envelope xmlns="urn:iso:std:iso:20022:tech:xsd:head.001.001.01">
                <AppHdr>
                    <MsgDefIdr>pacs.004.001.09</MsgDefIdr>
                </AppHdr>
            </Envelope>
        """;
        Document doc = loadXml(xml);
        String result = utilityMethods.getBizMsgIdr(doc);
        assertNull(result);
    }

    @Test
    void testGetMsgDefIdr_missingNode() throws Exception {
        String xml = """
            <Envelope xmlns="urn:iso:std:iso:20022:tech:xsd:head.001.001.01">
                <AppHdr>
                    <BizMsgIdr>ABC123456789</BizMsgIdr>
                </AppHdr>
            </Envelope>
        """;
        Document doc = loadXml(xml);
        String result = utilityMethods.getMsgDefIdr(doc);
        assertNull(result);
    }

    @Test
    void testGetBizMsgIdr_emptyContent() throws Exception {
        String xml = """
            <Envelope xmlns="urn:iso:std:iso:20022:tech:xsd:head.001.001.01">
                <AppHdr>
                    <BizMsgIdr>   </BizMsgIdr>
                </AppHdr>
            </Envelope>
        """;
        Document doc = loadXml(xml);
        String result = utilityMethods.getBizMsgIdr(doc);
        assertEquals("", result);
    }
}
