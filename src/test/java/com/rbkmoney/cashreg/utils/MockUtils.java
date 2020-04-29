package com.rbkmoney.cashreg.utils;

import com.rbkmoney.cashreg.service.dominant.DominantService;
import com.rbkmoney.cashreg.service.dominant.model.ResponseDominantWrapper;
import com.rbkmoney.cashreg.service.pm.PartyManagementService;
import com.rbkmoney.damsel.cashreg.adapter.*;
import com.rbkmoney.damsel.cashreg.processing.*;
import com.rbkmoney.damsel.cashreg.receipt.ReceiptInfo;
import com.rbkmoney.damsel.cashreg.receipt.status.Pending;
import com.rbkmoney.damsel.cashreg.receipt.status.Status;
import com.rbkmoney.damsel.domain.CashRegisterProvider;
import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.machinarium.client.AutomatonClient;
import com.rbkmoney.machinarium.domain.TMachineEvent;
import com.rbkmoney.machinegun.msgpack.Value;
import org.jetbrains.annotations.NotNull;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

public class MockUtils {

    public static void mockCashRegProvider(com.rbkmoney.cashreg.service.provider.CashRegProvider provider) {
        doAnswer((Answer<CashregResult>) invocation -> {
            CashregResult regResult = new CashregResult();

            ReceiptInfo cashregInfo = new ReceiptInfo();
            cashregInfo.setReceiptId(TestData.CASHREG_RECEIPT_ID);
            regResult.setInfo(cashregInfo);

            regResult.setIntent(Intent.finish(new FinishIntent().setStatus(FinishStatus.success(new Success()))));
            return regResult;
        }).when(provider).register(any());
    }

    public static void mockDominant(DominantService service) {
        doAnswer((Answer<ResponseDominantWrapper<CashRegisterProviderObject>>) invocation -> {
            CashRegisterProviderObject providerObject = new CashRegisterProviderObject();
            providerObject.setRef(new CashRegisterProviderRef().setId(1));

            Proxy proxy = new Proxy();
            proxy.setAdditional(TestData.prepareOptions());
            proxy.setRef(new ProxyRef());

            CashRegisterProvider provider = new CashRegisterProvider();
            provider.setName(TestData.PROVIDER_NAME);
            provider.setDescription(TestData.PROVIDER_DESCRIPTION);
            provider.setProxy(proxy);

            // TODO: list, more params
            List<CashRegisterProviderParameter> parameters = new ArrayList<>();
            CashRegisterProviderParameter parameter = new CashRegisterProviderParameter();
            parameter.setDescription("description");
            parameter.setId("id");
            parameter.setType(CashRegisterProviderParameterType.url_type(new CashRegisterProviderParameterUrl()));
            parameters.add(parameter);

            provider.setParamsSchema(parameters);
            providerObject.setData(provider);


            ResponseDominantWrapper<CashRegisterProviderObject> responseDominantWrapper = new ResponseDominantWrapper<>();
            responseDominantWrapper.setResponse(providerObject);
            responseDominantWrapper.setRevisionVersion(1L);
            return responseDominantWrapper;
        }).when(service).getCashRegisterProviderObject(any(), any());

        doAnswer((Answer<ResponseDominantWrapper<ProxyObject>>) invocation -> {
            ProxyObject proxyObject = new ProxyObject();
            proxyObject.setRef(new ProxyRef());

            ProxyDefinition proxyDefinition = new ProxyDefinition();
            proxyDefinition.setName(TestData.PROXY_NAME);
            proxyDefinition.setDescription(TestData.PROXY_DESCRIPTION);
            proxyDefinition.setOptions(TestData.prepareOptions());
            proxyDefinition.setUrl(TestData.PROXY_URL);
            proxyObject.setData(proxyDefinition);

            ResponseDominantWrapper<ProxyObject> responseDominantWrapper = new ResponseDominantWrapper<>();
            responseDominantWrapper.setResponse(proxyObject);
            responseDominantWrapper.setRevisionVersion(1L);
            return responseDominantWrapper;
        }).when(service).getProxyObject(any(), any());

    }

    public static void mockAutomatonClient(AutomatonClient<Value, Change> client) {
        Mockito.doNothing().when(client).start(any(), any());

        List<TMachineEvent<Change>> list = createtMachineEvents();
        doAnswer((Answer<List<TMachineEvent<Change>>>) invocation -> list).when(client).getEvents(any(), any());
        doAnswer((Answer<List<TMachineEvent<Change>>>) invocation -> list).when(client).getEvents(any());
    }

    @NotNull
    private static List<TMachineEvent<Change>> createtMachineEvents() {
        List<TMachineEvent<Change>> list = new ArrayList<>();

        ReceiptParams cashRegParams = CreateUtils.createDefaultReceiptParams();
        list.add(new TMachineEvent<>(1, Instant.now(), CreateUtils.createCreatedChange(cashRegParams)));

        Change pendingChange = Change.status_changed(new StatusChange().setStatus(Status.pending(new Pending())));
        list.add(new TMachineEvent<>(2, Instant.now(), pendingChange));

        Change sessionChange = Change.session(new SessionChange().setId("session_change_id").setPayload(SessionChangePayload.started(new SessionStarted())));
        list.add(new TMachineEvent<>(3, Instant.now(), sessionChange));
        return list;
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
        }).when(service).getContract(any(), any(), any());

        doAnswer((Answer<Shop>) invocation -> {
            Shop shop = new Shop();
            shop.setContractId(TestData.SHOP_CONTRACT_ID);
            return shop;
        }).when(service).getShop(any(), any(), any());

    }

}
