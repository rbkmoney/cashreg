package com.rbkmoney.cashreg.utils;

import com.rbkmoney.damsel.cashreg.domain.AccountInfo;
import com.rbkmoney.damsel.cashreg.domain.PaymentInfo;
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

    public static ReceiptParams createReceipParams(
            String id, String partyId, String shopId,
            Type type, PaymentInfo paymentInfo) {

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
        com.rbkmoney.damsel.cashreg.domain.LegalEntity legalEntity = new com.rbkmoney.damsel.cashreg.domain.LegalEntity();
        com.rbkmoney.damsel.cashreg.domain.RussianLegalEntity russianLegalEntity = new com.rbkmoney.damsel.cashreg.domain.RussianLegalEntity();

        russianLegalEntity.setActualAddress("ActualAddress");
        russianLegalEntity.setInn("INN");
        russianLegalEntity.setPostAddress("PostAddress");
        russianLegalEntity.setRegisteredName("RegisteredName");
        russianLegalEntity.setRepresentativeDocument("RepresentativeDocument");
        russianLegalEntity.setRepresentativeFullName("RepresentativeFullName");
        russianLegalEntity.setRepresentativePosition("RepresentativePosition");
        russianLegalEntity.setRegisteredNumber("RegisteredNumber");

        com.rbkmoney.damsel.cashreg.domain.RussianBankAccount russianBankAccount = new com.rbkmoney.damsel.cashreg.domain.RussianBankAccount();
        russianBankAccount.setAccount("Account");
        russianBankAccount.setBankName("BankName");
        russianBankAccount.setBankPostAccount("BankPostAccount");
        russianBankAccount.setBankBik("BankBik");
        russianLegalEntity.setRussianBankAccount(russianBankAccount);
        russianLegalEntity.setEmail(TestData.TEST_EMAIL);
        russianLegalEntity.setTaxMode(TaxMode.osn);

        legalEntity.setRussianLegalEntity(russianLegalEntity);

        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setLegalEntity(legalEntity);
        return accountInfo;
    }

}
