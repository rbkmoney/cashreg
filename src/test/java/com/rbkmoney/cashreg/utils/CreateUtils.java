package com.rbkmoney.cashreg.utils;

import com.rbkmoney.damsel.cashreg.domain.AccountInfo;
import com.rbkmoney.damsel.cashreg.domain.LegalEntity;
import com.rbkmoney.damsel.cashreg.domain.PaymentInfo;
import com.rbkmoney.damsel.cashreg.domain.RussianBankAccount;
import com.rbkmoney.damsel.cashreg.domain.RussianLegalEntity;
import com.rbkmoney.damsel.cashreg.domain.TaxMode;
import com.rbkmoney.damsel.cashreg.processing.*;
import com.rbkmoney.damsel.cashreg.receipt.Cart;
import com.rbkmoney.damsel.cashreg.receipt.ItemsLine;
import com.rbkmoney.damsel.cashreg.receipt.status.Pending;
import com.rbkmoney.damsel.cashreg.receipt.status.Status;
import com.rbkmoney.damsel.cashreg.receipt.type.Debit;
import com.rbkmoney.damsel.cashreg.receipt.type.Type;
import com.rbkmoney.damsel.domain.Cash;
import com.rbkmoney.damsel.domain.CurrencyRef;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CreateUtils {

    public static PaymentInfo createPaymentInfo() {
        Cart cart = new Cart();
        List<ItemsLine> lines = new ArrayList<>();
        cart.setLines(lines);

        Cash cash = new Cash()
                .setAmount(100L)
                .setCurrency(new CurrencyRef().setSymbolicCode("RUR"));

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCash(cash);
        paymentInfo.setCart(cart);
        paymentInfo.setEmail(TestData.TEST_EMAIL);
        return paymentInfo;
    }

    public static ReceiptParams createReceipParams(
            String id, String partyId, String shopId,
            Type type, PaymentInfo paymentInfo
    ) {

        List<CashRegisterProvider> providers = createProviders();
        return new ReceiptParams()
                .setProviders(providers)
                .setReceiptId(id)
                .setPartyId(partyId)
                .setShopId(shopId)
                .setType(type)
                .setPaymentInfo(paymentInfo);
    }

    @NotNull
    private static List<CashRegisterProvider> createProviders() {
        List<CashRegisterProvider> providers = new ArrayList<>();

        CashRegisterProvider provider = new CashRegisterProvider();
        provider.setProviderId(TestData.CASHREG_PROVIDER_ID);
        provider.setProviderParams(new HashMap<>());
        providers.add(provider);
        return providers;
    }

    public static ReceiptParams createDefaultReceiptParams() {
        return createReceipParams(
                TestData.RECEIPT_ID, TestData.PARTY_ID, TestData.SHOP_ID,
                Type.debit(new Debit()), CreateUtils.createPaymentInfo()
        );
    }

    public static Change createCreatedChange(ReceiptParams params) {
        CashRegisterProvider cashRegisterProvider = new CashRegisterProvider()
                .setProviderId(TestData.CASHREG_PROVIDER_ID)
                .setProviderParams(new HashMap<>());

        Receipt receipt = new Receipt()
                .setCashregProvider(cashRegisterProvider)
                .setReceiptId(params.getReceiptId())
                .setPaymentInfo(createPaymentInfo())
                .setType(Type.debit(new Debit()))
                .setShopId(params.getShopId())
                .setPartyId(params.getPartyId())
                .setStatus(Status.pending(new Pending()))
                .setAccountInfo(createAccountInfo())
                .setDomainRevision(1)
                .setPartyRevision(1);

        CreatedChange created = new CreatedChange();
        created.setReceipt(receipt);
        return Change.created(created);
    }

    public static AccountInfo createAccountInfo() {
        RussianBankAccount russianBankAccount = new RussianBankAccount()
                .setAccount("Account")
                .setBankName("BankName")
                .setBankPostAccount("BankPostAccount")
                .setBankBik("BankBik");

        RussianLegalEntity russianLegalEntity = new RussianLegalEntity()
                .setActualAddress("ActualAddress")
                .setInn("INN")
                .setPostAddress("PostAddress")
                .setRegisteredName("RegisteredName")
                .setRepresentativeDocument("RepresentativeDocument")
                .setRepresentativeFullName("RepresentativeFullName")
                .setRepresentativePosition("RepresentativePosition")
                .setRegisteredNumber("RegisteredNumber")
                .setRussianBankAccount(russianBankAccount)
                .setEmail(TestData.TEST_EMAIL)
                .setTaxMode(TaxMode.osn);

        LegalEntity legalEntity = new LegalEntity();
        legalEntity.setRussianLegalEntity(russianLegalEntity);

        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setLegalEntity(legalEntity);
        return accountInfo;
    }

}
