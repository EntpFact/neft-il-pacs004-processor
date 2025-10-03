package com.hdfcbank.pacs004.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hdfcbank.pacs004.exception.NILException;
import com.hdfcbank.pacs004.model.ReqPayload;
import com.hdfcbank.pacs004.model.Response;
import com.hdfcbank.pacs004.service.Pacs004XmlProcessor;
import com.hdfcbank.pacs004.utils.NILRouterCommonUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class ProcessController {

    @Autowired
    Pacs004XmlProcessor pacs004XmlProcessor;

    @Autowired
    NILRouterCommonUtility nilRouterCommonUtility;


    @CrossOrigin
    @GetMapping(path = "/healthz")
    public ResponseEntity<?> healthz() {
        return new ResponseEntity<>("Success", HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping(path = "/ready")
    public ResponseEntity<?> ready() {
        return new ResponseEntity<>("Success", HttpStatus.OK);
    }


    @CrossOrigin
    @PostMapping("/process")
    public Mono<ResponseEntity<Response>> process(@RequestBody String request) throws JsonProcessingException {
        log.info("....PACS004 Processing Started.... ");
        return Mono.fromCallable(() -> {
            try {
                ReqPayload requestMap = nilRouterCommonUtility.convertToMap(request);
                if(!pacs004XmlProcessor.validateRequest(requestMap)){
                    pacs004XmlProcessor.parseXml(requestMap);
                }
                return ResponseEntity.ok(new Response("SUCCESS", "Message Processed."));
            } catch (Exception ex) {
                log.error("Failed in consuming the message: {}", ex);
                throw new NILException("Failed in consuming the message", ex);
            } finally {
                log.info("....PACS004 Processing Completed.... ");
            }
        }).onErrorResume(ex -> {
            return Mono.just(new ResponseEntity<>(new Response("ERROR", "Message Processing Failed"), HttpStatus.INTERNAL_SERVER_ERROR));
        });
    }


    @CrossOrigin
    @PostMapping("/sendToProcessor")
    public ResponseEntity<String> sendToProcessor(@RequestBody String request)  {
        log.info("....PACS004 sendToProcessor Started.... ");
        ReqPayload requestMap = nilRouterCommonUtility.convertToMap(request);
        try {
            if (!pacs004XmlProcessor.validateRequest(requestMap)) {
            pacs004XmlProcessor.parseXml(requestMap);
                return new ResponseEntity<>("Message sent to processor: ", HttpStatus.OK);
            }else{
                pacs004XmlProcessor.saveInvalidPayload(requestMap);
                return new ResponseEntity<>("Invalid JSON : ", HttpStatus.BAD_REQUEST);
            }

        } catch (Exception ex) {
            log.error("Failed in consuming the message: {}", ex);
            return new ResponseEntity<>("Error processing to processor: ", HttpStatus.BAD_REQUEST);


        }

    }
}
