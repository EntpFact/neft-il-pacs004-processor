package com.hdfcbank.pacs004.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hdfcbank.pacs004.model.MsgEventTracker;
import com.hdfcbank.pacs004.model.TransactionAudit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class NilRepositoryTest {

    @InjectMocks
    private NilRepository nilRepository;

    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindTargetByTxnIdSuccess() {
        when(namedParameterJdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(String.class)))
                .thenReturn("TARGET");

        String result = nilRepository.findTargetByTxnId("TXN123");
        assert result.equals("TARGET");
    }

    @Test
    void testFindTargetByTxnIdFailure() {
        when(namedParameterJdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(String.class)))
                .thenThrow(new RuntimeException("DB error"));

        String result = nilRepository.findTargetByTxnId("TXN123");
        assert result == null;
    }

    @Test
    void testSaveDataInMsgEventTracker() throws SQLException, JsonProcessingException {
        MsgEventTracker tracker = new MsgEventTracker();
        tracker.setMsgId("MSG123");
        tracker.setSource("SRC");
        tracker.setTarget("TGT");
        tracker.setFlowType("FLOW");
        tracker.setMsgType("TYPE");
        tracker.setOrgnlReq("<xml>data</xml>");
        tracker.setOrgnlReqCount(1);
        tracker.setBatchId("BATCH1");
        tracker.setInvalidPayload(false);
        tracker.setConsolidateAmt(BigDecimal.TEN);
        tracker.setIntermediateReq("INTREQ");
        tracker.setIntermediateCount(2);
        tracker.setStatus("NEW");
        tracker.setBatchCreationDate(LocalDate.now());
        tracker.setBatchCreationTime(LocalDateTime.now());

        nilRepository.saveDataInMsgEventTracker(tracker);
        verify(namedParameterJdbcTemplate, times(1)).update(anyString(), any(MapSqlParameterSource.class));
    }

    @Test
    void testSaveDuplicateEntryInsertPath() {
        MsgEventTracker tracker = new MsgEventTracker();
        tracker.setMsgId("MSG123");
        tracker.setSource("SRC");
        tracker.setTarget("TGT");
        tracker.setFlowType("FLOW");
        tracker.setMsgType("TYPE");
        tracker.setOrgnlReq("<xml>data</xml>");

        when(namedParameterJdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(BigDecimal.class)))
                .thenReturn(null);

        nilRepository.saveDuplicateEntry(tracker);
        verify(namedParameterJdbcTemplate).update(contains("INSERT INTO"), any(MapSqlParameterSource.class));
    }

    @Test
    void testSaveDuplicateEntryUpdatePath() {
        MsgEventTracker tracker = new MsgEventTracker();
        tracker.setMsgId("MSG123");
        tracker.setSource("SRC");
        tracker.setTarget("TGT");
        tracker.setFlowType("FLOW");
        tracker.setMsgType("TYPE");
        tracker.setOrgnlReq("<xml>data</xml>");

        when(namedParameterJdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(BigDecimal.class)))
                .thenReturn(BigDecimal.ONE);

        nilRepository.saveDuplicateEntry(tracker);
        verify(namedParameterJdbcTemplate).update(contains("UPDATE"), any(MapSqlParameterSource.class));
    }

    @Test
    void testSaveAllTransactionAudits() {
        TransactionAudit audit = new TransactionAudit();
        audit.setMsgId("MSG123");
        audit.setTxnId("TXN123");
        audit.setEndToEndId("E2E123");
        audit.setReturnId("RET123");
        audit.setReqPayload("<xml>payload</xml>");
        audit.setSource("SRC");
        audit.setTarget("TGT");
        audit.setFlowType("FLOW");
        audit.setMsgType("TYPE");
        audit.setAmount(BigDecimal.valueOf(100));
        audit.setBatchId("BATCH1");
        audit.setBatchDate(LocalDate.now());
        audit.setBatchTime(LocalDateTime.now());

        nilRepository.saveAllTransactionAudits(List.of(audit));
        verify(namedParameterJdbcTemplate).batchUpdate(anyString(), any(MapSqlParameterSource[].class));
    }
}

