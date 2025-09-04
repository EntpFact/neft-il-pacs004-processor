package com.hdfcbank.pacs004.model;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@ToString
@NoArgsConstructor
public class Header {
    private String msgId;//
    private String source;//
    private String target;
    private String flowType;//
    private boolean replayInd;
    private boolean invalidPayload;
    private String prefix;
    private String status;
    private String msgType;

}
