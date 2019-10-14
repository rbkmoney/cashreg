package com.rbkmoney.cashreg.utils;

import com.rbkmoney.damsel.cashreg.Cart;
import com.rbkmoney.damsel.cashreg.ItemsLine;
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
import com.rbkmoney.damsel.domain.Cash;
import com.rbkmoney.damsel.domain.CurrencyRef;

import java.util.ArrayList;
import java.util.List;

public class CreateUtils {

    public static PaymentInfo createPaymentInfo() {
        PaymentInfo paymentInfo = new PaymentInfo();

        Cash cash = new Cash();
        cash.setAmount(100L);
        cash.setCurrency(new CurrencyRef().setSymbolicCode("RUR"));
        paymentInfo.setCash(cash);

        Cart cart = new Cart();
        List<ItemsLine> lines = new ArrayList<>();
        cart.setLines(lines);
        paymentInfo.setCart(cart);
        paymentInfo.setEmail(TestData.TEST_EMAIL);
        return paymentInfo;
    }

    public static CashRegParams createCashRegParams(
            String id, String partyId, String shopId,
            Type type, PaymentInfo paymentInfo) {
        return new CashRegParams()
                .setId(id)
                .setPartyId(partyId)
                .setShopId(shopId)
                .setType(type)
                .setPaymentInfo(paymentInfo);
    }

    public static CashRegParams createDefaultCashRegParams() {
        return createCashRegParams(
                TestData.CASHREG_ID, TestData.PARTY_ID, TestData.SHOP_ID,
                Type.debit(new Debit()), CreateUtils.createPaymentInfo()
        );
    }

    public static Change createCreatedChange(CashRegParams params) {
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
