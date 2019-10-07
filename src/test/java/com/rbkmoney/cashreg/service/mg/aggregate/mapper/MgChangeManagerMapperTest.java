package com.rbkmoney.cashreg.service.mg.aggregate.mapper;

import com.rbkmoney.cashreg.CashRegApplication;
import com.rbkmoney.cashreg.utils.cashreg.creators.ChangeFactory;
import com.rbkmoney.damsel.cashreg.status.Failed;
import com.rbkmoney.damsel.cashreg.status.Pending;
import com.rbkmoney.damsel.cashreg.status.Status;
import com.rbkmoney.damsel.cashreg.type.Debit;
import com.rbkmoney.damsel.cashreg.type.Type;
import com.rbkmoney.damsel.cashreg_domain.AccountInfo;
import com.rbkmoney.damsel.cashreg_domain.PaymentInfo;
import com.rbkmoney.damsel.cashreg_processing.CashReg;
import com.rbkmoney.damsel.cashreg_processing.CashRegParams;
import com.rbkmoney.damsel.cashreg_processing.Change;
import com.rbkmoney.damsel.cashreg_processing.CreatedChange;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static com.rbkmoney.cashreg.utils.CreateUtils.createCashRegParams;
import static com.rbkmoney.cashreg.utils.CreateUtils.createPaymentInfo;
import static org.junit.Assert.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = DEFINED_PORT)
@ContextConfiguration(classes = CashRegApplication.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MgChangeManagerMapperTest {

    private String cashregId = "cashreg_id";
    private String partyId = "party_id";
    private String shopId = "shop_id";

    @Autowired
    private MgChangeManagerMapper mgChangeManagerMapper;

    @Test
    public void testMgChangeManagerMapperProcess() {

        CashRegParams params = createCashRegParams(
                cashregId, partyId, shopId,
                Type.debit(new Debit()), createPaymentInfo()
        );

        List<Change> changeList = new ArrayList<>();
        Change change = prepareCreatedChange(params);
        changeList.add(change);
        changeList.add(ChangeFactory.createStatusChangeFailed());

        CashReg cashReg = mgChangeManagerMapper.process(changeList);

        assertEquals(cashregId, cashReg.getId());
        assertEquals(Status.failed(new Failed()), cashReg.getStatus());
    }

    private Change prepareCreatedChange(CashRegParams params) {
        CreatedChange created = new CreatedChange();
        CashReg cashReg = new CashReg();
        cashReg.setId(params.getId());
        cashReg.setPaymentInfo(new PaymentInfo());
        cashReg.setType(Type.debit(new Debit()));
        cashReg.setShopId(params.getShopId());
        cashReg.setPartyId(params.getPartyId());
        cashReg.setStatus(Status.pending(new Pending()));
        cashReg.setAccountInfo(new AccountInfo());
        created.setCashreg(cashReg);
        return Change.created(created);
    }


}