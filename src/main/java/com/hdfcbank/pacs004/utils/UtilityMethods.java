package com.hdfcbank.pacs004.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;

@Slf4j
@Component
public class UtilityMethods {

    public String getBizMsgIdr(Document originalDoc) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        Node msgIdNode = (Node) xpath.evaluate("//*[local-name()='AppHdr']/*[local-name()='BizMsgIdr']", originalDoc, XPathConstants.NODE);
        String msgId = msgIdNode != null ? msgIdNode.getTextContent().trim() : null;
        return msgId;
    }

    public String getMsgDefIdr(Document originalDoc) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        Node msgIdNode = (Node) xpath.evaluate("//*[local-name()='AppHdr']/*[local-name()='MsgDefIdr']", originalDoc, XPathConstants.NODE);
        String msgId = msgIdNode != null ? msgIdNode.getTextContent().trim() : null;
        return msgId;

    }



}
