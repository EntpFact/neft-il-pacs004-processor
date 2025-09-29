package com.hdfcbank.pacs004.controller;


import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ProcessControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @InjectMocks
    ProcessController controller;

    @Spy
    private MessageChannel routingChannel = Mockito.mock(MessageChannel.class);


    @Test
    public void testHealthzEndpoint() {
        webTestClient.get().uri("/healthz").exchange().expectStatus().isOk().expectBody(String.class).isEqualTo("Success");
    }

    @Test
    public void testReadyEndpoint() {
        webTestClient.get().uri("/ready").exchange().expectStatus().isOk().expectBody(String.class).isEqualTo("Success");
    }


    @Test
    void testProcessEndpoint() {

        String request = "{\"header\":{\"msgId\":\"RBIP202101146147295848\",\"source\":\"SFMS\",\"target\":\"pacs004Processor\"," +
                "\"msgType\":\"pacs.004.001.10\",\"flowType\":\"INWARD\",\"replayInd\":false," +
                "\"invalidPayload\":false,\"prefix\":\"CBS\"},\"body\":{\"payload\":\"<RequestPayload>\\r\\n" +
                "<AppHdr >\\r\\n    <Fr>\\r\\n        <FIId>\\r\\n            <FinInstnId>\\r\\n                <ClrSysMmbId>\\r\\n            " +
                "        <MmbId>RBIP0NEFTSC</MmbId>\\r\\n                </ClrSysMmbId>\\r\\n            </FinInstnId>\\r\\n        </FIId>\\r\\n    " +
                "</Fr></AppHdr ></RequestPayload>\\r\\n\"}}";
        String base64 = Base64.getEncoder().encodeToString(request.getBytes(StandardCharsets.UTF_8));
        String requestJson = "{\"data_base64\":\"" + base64 + "\"}";

        Mockito.when(routingChannel.send(any())).thenReturn(true);

        webTestClient.post().uri("/process").bodyValue(requestJson).exchange().expectStatus().is2xxSuccessful().expectBody().jsonPath("$.status").isEqualTo("SUCCESS");
    }

//    @Test
    void testProcess_invalidBase64_shouldReturnError() {
        // Invalid base64 will cause decoding to fail
        String invalidBase64 = "!@#$%^&*()_+";

        webTestClient.post().uri("/process").contentType(MediaType.APPLICATION_JSON).bodyValue(Map.of("data_base64", invalidBase64)).exchange().expectStatus().is5xxServerError().expectBody(ResponseEntity.class).value(response -> {
         });

        verifyNoInteractions(routingChannel);
    }
}
