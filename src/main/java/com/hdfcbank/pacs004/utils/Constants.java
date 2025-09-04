package com.hdfcbank.pacs004.utils;

public class Constants {


    public static final String KAFKA_RESPONSE_TOPIC_DAPR_BINDING = "kafka-nilrouter-pubsub-component";
    public static final String NIL = "NIL";
    public static final String INWARD = "Inward";
	public static final String OUTWARD = "Outward";
    public static final String FC = "FC";
    public static final String EPH = "EPH";
    public static final String RECEIVED = "RECEIVED";

    public static final String PACS04= "pacs.004.001.10";
    public static final String SFMS="SFMS";
    public static final String BATCH_ID = "BatchId";
    public static final String BATCH_CREDT = "batchCreatedDate";
    public static final String MSSG_ID = "mssgId";
    public static final String DISPATCHED = "_DISPATCHED";
    public static final String INPROGRESS = "INPROGRESS";
    public static final String DOCUMENT_XPATH="/*[local-name()='RequestPayload']/*[local-name()='Document']";
    public static final String ORGNL_TXN_XPATH="./*[local-name()='OrgnlTxId']";
    public static final String HEADER_XPATH="/*[local-name()='RequestPayload']/*[local-name()='AppHdr']";
    public static final String GROUP_HEADER_XPATH=".//*[local-name()='GrpHdr']";
    public static final String END_TO_END_XPATH="./*[local-name()='OrgnlEndToEndId']";
    public static final String AMOUNT_XPATH="./*[local-name()='RtrdIntrBkSttlmAmt']";
    public static final String CDTTRFTXINF_XPATH="/*[local-name()='RequestPayload']/*[local-name()='Document']//*[local-name()='TxInf']";
    public static final String BIZMSGID_XPATH="/*[local-name()='RequestPayload']/*[local-name()='AppHdr']/*[local-name()='BizMsgIdr']";
    public static final String BATCH_ID_XPATH = ".//*[local-name()='OrgnlTxRef']/*[local-name()='UndrlygCstmrCdtTrf']//*[local-name()='RmtInf']";
    public static final String BATCH_CREDT_XPATH = "/*[local-name()='RequestPayload']/*[local-name()='AppHdr']/*[local-name()='CreDt']";
    public static final String BODY = "body";
    public static final String PAYLOAD ="payload";
    public static final String HEADER = "header";
    public static final String TARGET = "target";
    public static final String INTERMEDIATE_REQ_COUNT = "intermediateReqCount";

    public static final String ORIG_REQ_COUNT = "orignlReqCount";

    public static final String SENT_TO_DISPATCHER = "SENT_TO_DISPATCHER";
}
