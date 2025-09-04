package com.hdfcbank.pacs004.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.sql.Date;

@Data
@ToString
@AllArgsConstructor
public class Pacs004Fields {
    String bizMsgIdr;
    String endToEndId;
    String txId;
    String amount;
    String swtch;
    String batchId;
}



