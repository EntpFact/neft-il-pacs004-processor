package com.hdfcbank.pacs004.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hdfcbank.pacs004.kafkaproducer.KafkaUtils;
import com.hdfcbank.pacs004.model.ReqPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ErrorHandling {


    @Autowired
    KafkaUtils kafkaUtils;

    @Value("${topic.defaultErrorSwitch}")
    private String defaultSwitch;

    @Value("${topic.dispatchertopic}")
    private String dispatchertopic;

    @Autowired
    ObjectMapper objectMapper;

    public void handleInvalidPayload(ReqPayload reqPayload) throws JsonProcessingException {
        reqPayload.getHeader().setTarget(defaultSwitch);
        String reqPayloadString = objectMapper.writeValueAsString(reqPayload);
        kafkaUtils.publishToResponseTopic(reqPayloadString,dispatchertopic,reqPayload.getHeader().getMsgId());

    }
}