package com.rbkmoney.cashreg.utils;

import com.rbkmoney.damsel.cashreg.Cart;
import com.rbkmoney.damsel.cashreg.ItemsLine;
import com.rbkmoney.damsel.cashreg.type.Debit;
import com.rbkmoney.damsel.cashreg.type.Type;
import com.rbkmoney.damsel.cashreg_domain.PaymentInfo;
import com.rbkmoney.damsel.cashreg_processing.CashRegParams;
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
        List<ItemsLine> lines = new ArrayList();
        cart.setLines(lines);
        paymentInfo.setCart(cart);
        return paymentInfo;
    }

    public static CashRegParams createCashRegParams() {
        CashRegParams params = new CashRegParams();
        params.setId("id");
        params.setPartyId("party_id");
        params.setShopId("shop_id");
        params.setType(Type.debit(new Debit()));
        params.setPaymentInfo(createPaymentInfo());
        return params;
    }

    public static CashRegParams createCashRegParams(
            String id, String partyId, String shopId, Type type, PaymentInfo paymentInfo
    ) {
        return new CashRegParams()
                .setId(id)
                .setPartyId(partyId)
                .setShopId(shopId)
                .setType(type)
                .setPaymentInfo(paymentInfo);
    }

}
