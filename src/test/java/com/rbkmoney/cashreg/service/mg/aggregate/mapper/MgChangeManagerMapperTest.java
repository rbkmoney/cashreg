package com.rbkmoney.cashreg.service.mg.aggregate.mapper;

import com.rbkmoney.cashreg.CashRegApplication;
import com.rbkmoney.cashreg.utils.CreateUtils;
import com.rbkmoney.cashreg.utils.TestData;
import com.rbkmoney.cashreg.utils.cashreg.creators.ChangeFactory;
import com.rbkmoney.damsel.cashreg.processing.*;
import com.rbkmoney.damsel.cashreg.receipt.ReceiptInfo;
import com.rbkmoney.damsel.cashreg.receipt.status.Delivered;
import com.rbkmoney.damsel.cashreg.receipt.status.Status;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = DEFINED_PORT)
@ContextConfiguration(classes = CashRegApplication.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MgChangeManagerMapperTest {

    @Autowired
    private MgChangeManagerMapper mgChangeManagerMapper;

    @Test
    public void testMgChangeManagerMapperProcess() {
        ReceiptParams params = CreateUtils.createDefaultReceiptParams();

        List<Change> changeList = new ArrayList<>();
        Change change = CreateUtils.createCreatedChange(params);
        changeList.add(change);
        changeList.add(ChangeFactory.createStatusChangeFailed());

        SessionChange sessionChange = new SessionChange();
        SessionChangePayload payload = new SessionChangePayload();
        SessionFinished sessionFinished = new SessionFinished();
        SessionResult sessionResult = new SessionResult();
        SessionSucceeded sessionSucceeded = new SessionSucceeded();
        sessionSucceeded.setInfo(new ReceiptInfo().setDaemonCode("daemon_code"));
        sessionResult.setSucceeded(sessionSucceeded);
        sessionFinished.setResult(sessionResult);
        payload.setFinished(sessionFinished);
        sessionChange.setPayload(payload);

        changeList.add(Change.session(sessionChange));

        Receipt receipt = mgChangeManagerMapper.process(changeList);

        assertEquals(TestData.RECEIPT_ID, receipt.getReceiptId());
        assertEquals(Status.delivered(new Delivered()), receipt.getStatus());
    }

}