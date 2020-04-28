package com.rbkmoney.cashreg.service.management.impl;

import com.rbkmoney.cashreg.CashRegApplication;
import com.rbkmoney.cashreg.domain.SourceData;
import com.rbkmoney.cashreg.utils.CreateUtils;
import com.rbkmoney.damsel.cashreg.processing.Change;
import com.rbkmoney.damsel.cashreg.processing.ReceiptParams;
import com.rbkmoney.damsel.cashreg.processing.StatusChange;
import com.rbkmoney.damsel.cashreg.receipt.status.Pending;
import com.rbkmoney.damsel.cashreg.receipt.status.Status;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = DEFINED_PORT)
@ContextConfiguration(classes = CashRegApplication.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ManagementServiceImplTest {

    @Autowired
    private ManagementServiceImpl managementService;

    @Test
    public void singnalInit() {
        SourceData sourceData = managementService.signalInit();
        assertTrue(sourceData.getChange().getStatusChanged().getStatus().isSetPending());
    }

    @Test
    public void singnalTimeout() {
        List<Change> changes = new ArrayList<>();
        Change pendingChange = Change.status_changed(new StatusChange().setStatus(Status.pending(new Pending())));
        ReceiptParams params = CreateUtils.createDefaultReceiptParams();
        changes.add(CreateUtils.createCreatedChange(params));
        changes.add(pendingChange);

        SourceData sourceData = managementService.signalTimeout(changes);
        assertTrue(sourceData.getChange().getSession().getPayload().isSetStarted());
    }

}