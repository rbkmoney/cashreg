package com.rbkmoney.cashreg.utils;

import com.rbkmoney.cashreg.service.dominant.DominantService;
import com.rbkmoney.cashreg.service.pm.PartyManagementService;
import com.rbkmoney.damsel.cashreg.CashRegInfo;
import com.rbkmoney.damsel.cashreg.provider.CashRegResult;
import com.rbkmoney.damsel.cashreg.provider.FinishIntent;
import com.rbkmoney.damsel.cashreg.provider.FinishStatus;
import com.rbkmoney.damsel.cashreg.provider.Success;
import com.rbkmoney.damsel.cashreg.status.Pending;
import com.rbkmoney.damsel.cashreg.status.Status;
import com.rbkmoney.damsel.cashreg_processing.*;
import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.machinarium.client.AutomatonClient;
import com.rbkmoney.machinarium.domain.TMachineEvent;
import com.rbkmoney.machinegun.msgpack.Value;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

public class MockUtils {

    public static void mockCashRegProvider(com.rbkmoney.cashreg.service.provider.CashRegProvider provider) {
        doAnswer((Answer<CashRegResult>) invocation -> {
            CashRegResult regResult = new CashRegResult();

            CashRegInfo cashregInfo = new CashRegInfo();
            cashregInfo.setReceiptId(TestData.CASHREG_RECEIPT_ID);
            regResult.setCashregInfo(cashregInfo);

            regResult.setIntent(com.rbkmoney.damsel.cashreg.provider.Intent.finish(new FinishIntent().setStatus(FinishStatus.success(new Success()))));
            return regResult;
        }).when(provider).register(any());
    }

    public static void mockDominant(DominantService service) {
        doAnswer((Answer<CashRegProviderObject>) invocation -> {
            CashRegProviderObject providerObject = new CashRegProviderObject();
            providerObject.setRef(new CashRegProviderRef().setId(1));

            CashRegProvider provider = new CashRegProvider();
            provider.setName(TestData.PROVIDER_NAME);
            provider.setDescription(TestData.PROVIDER_DESCRIPTION);

            Proxy proxy = new Proxy();
            proxy.setAdditional(TestData.prepareOptions());
            proxy.setRef(new ProxyRef());
            provider.setProxy(proxy);
            providerObject.setData(provider);

            return providerObject;
        }).when(service).getCashRegProviderObject(any());

        doAnswer((Answer<ProxyObject>) invocation -> {
            ProxyObject object = new ProxyObject();
            object.setRef(new ProxyRef());

            ProxyDefinition proxyDefinition = new ProxyDefinition();
            proxyDefinition.setName(TestData.PROXY_NAME);
            proxyDefinition.setDescription(TestData.PROXY_DESCRIPTION);
            proxyDefinition.setOptions(TestData.prepareOptions());
            proxyDefinition.setUrl(TestData.PROXY_URL);
            object.setData(proxyDefinition);
            return object;
        }).when(service).getProxyObject(any());

        doAnswer((Answer<TerminalObject>) invocation -> {
            TerminalObject object = new TerminalObject();
            object.setRef(new TerminalRef().setId(1));
            Terminal terminal = new Terminal();
            terminal.setName(TestData.TERMINAL_NAME);
            terminal.setDescription(TestData.TERMINAL_DESCRIPTION);
            terminal.setOptions(TestData.prepareOptions());
            terminal.setRiskCoverage(RiskScore.low);
            terminal.setTerms(new PaymentsProvisionTerms());
            object.setData(terminal);
            return object;
        }).when(service).getTerminalObject(any());

    }

    public static void mockAutomatonClient(AutomatonClient<Value, Change> client) {
        Mockito.doNothing().when(client).start(any(), any());

        doAnswer((Answer<List<TMachineEvent<Change>>>) invocation -> {
            List<TMachineEvent<Change>> list = new ArrayList<>();

            CashRegParams cashRegParams = CreateUtils.createDefaultCashRegParams();
            list.add(new TMachineEvent<>(1, Instant.now(), CreateUtils.createCreatedChange(cashRegParams)));

            Change pendingChange = Change.status_changed(new StatusChange().setStatus(Status.pending(new Pending())));
            list.add(new TMachineEvent<>(2, Instant.now(), pendingChange));

            Change sessionChange = Change.session(new SessionChange().setId("session_change_id").setPayload(SessionChangePayload.started(new SessionStarted())));
            list.add(new TMachineEvent<>(3, Instant.now(), sessionChange));

            return list;
        }).when(client).getEvents(any(), any());
    }

    public static void mockPartyManagement(PartyManagementService service) {
        doAnswer((Answer<Long>) invocation -> 1L).when(service).getPartyRevision(any());

        doAnswer((Answer<Contract>) invocation -> {
            Contract contract = new Contract();
            Contractor contractor = new Contractor();
            LegalEntity legalEntity = new LegalEntity();
            RussianLegalEntity russianLegalEntity = new RussianLegalEntity();

            russianLegalEntity.setActualAddress("ActualAddress");
            russianLegalEntity.setInn("INN");
            russianLegalEntity.setPostAddress("PostAddress");
            russianLegalEntity.setRegisteredName("RegisteredName");
            russianLegalEntity.setRepresentativeDocument("RepresentativeDocument");
            russianLegalEntity.setRepresentativeFullName("RepresentativeFullName");
            russianLegalEntity.setRepresentativePosition("RepresentativePosition");
            russianLegalEntity.setRegisteredNumber("RegisteredNumber");

            RussianBankAccount russianBankAccount = new RussianBankAccount();
            russianBankAccount.setAccount("Account");
            russianBankAccount.setBankName("BankName");
            russianBankAccount.setBankPostAccount("BankPostAccount");
            russianBankAccount.setBankBik("BankBik");
            russianLegalEntity.setRussianBankAccount(russianBankAccount);

            legalEntity.setRussianLegalEntity(russianLegalEntity);
            contractor.setLegalEntity(legalEntity);
            contract.setContractor(contractor);

            return contract;
        }).when(service).getContract(any(), any());

        doAnswer((Answer<Shop>) invocation -> {
            Shop shop = new Shop();
            shop.setContractId(TestData.SHOP_CONTRACT_ID);
            return shop;
        }).when(service).getShop(any(), any());

    }

}
