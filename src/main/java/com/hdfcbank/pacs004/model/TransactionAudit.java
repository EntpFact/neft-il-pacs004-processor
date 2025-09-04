package com.hdfcbank.pacs004.model;

import lombok.Data;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TransactionAudit {

    private String msgId;
    private String txnId;
    private String endToEndId;
    private String returnId;
    private String reqPayload;
    private String source;
    private String target;
    private String flowType;
    private LocalDate batchDate;
    private LocalDateTime batchTime;
    private String msgType;
    private BigDecimal amount;
    private String batchId;

}
