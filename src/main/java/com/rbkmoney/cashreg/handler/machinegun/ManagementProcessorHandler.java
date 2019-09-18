package com.rbkmoney.cashreg.handler.machinegun;

import com.rbkmoney.cashreg.service.dominant.DominantService;
import com.rbkmoney.cashreg.service.pm.PartyManagementService;
import com.rbkmoney.cashreg.utils.cashreg.creators.StatusCreators;
import com.rbkmoney.damsel.cashreg.provider.LegalEntity;
import com.rbkmoney.damsel.cashreg.provider.RussianBankAccount;
import com.rbkmoney.damsel.cashreg.provider.*;
import com.rbkmoney.damsel.cashreg.status.Status;
import com.rbkmoney.damsel.cashreg.type.Type;
import com.rbkmoney.damsel.cashreg_processing.CashReg;
import com.rbkmoney.damsel.cashreg_processing.Change;
import com.rbkmoney.damsel.cashreg_processing.StatusChange;
import com.rbkmoney.damsel.domain.RussianLegalEntity;
import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.machinarium.domain.CallResultData;
import com.rbkmoney.machinarium.domain.SignalResultData;
import com.rbkmoney.machinarium.domain.TMachineEvent;
import com.rbkmoney.machinarium.handler.AbstractProcessorHandler;
import com.rbkmoney.machinegun.base.Timer;
import com.rbkmoney.machinegun.msgpack.Value;
import com.rbkmoney.machinegun.stateproc.ComplexAction;
import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.rbkmoney.cashreg.utils.ProtoUtils.*;


@Slf4j
@Component
public class ManagementProcessorHandler extends AbstractProcessorHandler<Value, Change> {

    private final PartyManagementService partyManagementService;
    private final DominantService dominantService;

    public ManagementProcessorHandler(
            PartyManagementService partyManagementService,
            DominantService dominantService
    ) {
        super(Value.class, Change.class);
        this.partyManagementService = partyManagementService;
        this.dominantService = dominantService;
    }

    @Override
    protected SignalResultData<Change> processSignalInit(String namespace, String machineId, Value args) {
        log.info("Request processSignalInit() machineId: {} value: {}", machineId, args);
//        Instant currentTime = Instant.now();
//        currentTime.plus(1, ChronoUnit.MINUTES);

        List<Change> changes = toChangeList(args);
        Change change = changes.get(0);
        CashReg cashreg = change.getCreated().getCashreg();

        // TODO: party
        Shop shop = partyManagementService.getShop(cashreg.getShopId(), cashreg.getPartyId());

        // TODO: how check kkt enabled or disabled? validate
        if (shop.isSetBlocking()) {
            throw new IllegalStateException("Shop " + shop.getDetails().getName() + " is blocked");
        }

        // TODO: dominant
        PaymentInstitutionRef paymentInstitutionRef = partyManagementService.getPaymentInstitutionRef(cashreg.getPartyId(), shop.getContractId());
        ProxyObject proxyObject = dominantService.getProxyObject(paymentInstitutionRef);
        if (!proxyObject.isSetData()) {
            throw new IllegalStateException("ProxyObject not found; paymentInstitutionRef: " + paymentInstitutionRef.getId());
        }

        // Данные по ставкам, типу налогооблажения и т.д.
        Contract contract = partyManagementService.getContract(cashreg.getPartyId(), shop.getContractId());
        ProxyDefinition proxyDefinition = proxyObject.getData();

        String url = proxyDefinition.getUrl();
        Map<String, String> proxyOptions = proxyDefinition.getOptions();

        // network timeout
        CashRegProviderSrv.Iface prv = cashRegProviderSrv(url, 10);
        // TODO: prepare

        CashRegContext context = prepareCashRegContext(contract, cashreg.getType(), cashreg, proxyOptions);

        // adapter
        CashRegResult result;
        try {
            result = prv.register(context);
        } catch (TException e) {
            throw new IllegalStateException("Can't receive result; paymentInstitutionRef: " + paymentInstitutionRef.getId());
        }

        Status status = StatusCreators.createFailedStatus();
        if (result.getIntent().getFinish().getStatus().isSetSuccess()) {
            status = StatusCreators.createPendingStatus();
        }

        Change statusChanged = Change.status_changed(new StatusChange().setStatus(status));
        changes.add(statusChanged);

        // TODO: timer
        SignalResultData<Change> resultData = new SignalResultData<>(
                toChangeList(toValue(changes)),
                buildComplexActionWithTimer(Timer.timeout(10), buildLastEventHistoryRange())
        );

        log.info("Response of processSignalInit: {}", resultData);
        return resultData;
    }

    private CashRegContext prepareCashRegContext(Contract contract, Type type, CashReg cashreg, Map<String, String> proxyOptions) {
        CashRegContext context = new CashRegContext();
        AccountInfo accountInfo = new AccountInfo();

        accountInfo.setLegalEntity(prepareLegalEntity(contract, proxyOptions));
        context.setAccountInfo(accountInfo);
        context.setOptions(proxyOptions);

        Session session = prepareSession(type);
        context.setSession(session);

        SourceCreation sourceCreation = prepareSourceCreation(cashreg);
        context.setSourceCreation(sourceCreation);
        return context;
    }

    private SourceCreation prepareSourceCreation(CashReg cashreg) {
        SourceCreation sourceCreation = new SourceCreation();

        PaymentInfo paymentInfo = new PaymentInfo();
        // TODO: Корзину вынести в отдельный файл, чтобы не конвертировать
        paymentInfo.setCart(prepareCart(cashreg));
        paymentInfo.setCash(cashreg.getPaymentInfo().getCash());
        sourceCreation.setPayment(paymentInfo);
        return sourceCreation;
    }

    private Session prepareSession(Type type) {
        Session session = new Session();
        session.setType(type);
        return session;
    }

    private LegalEntity prepareLegalEntity(Contract contract, Map<String, String> proxyOptions) {
        RussianLegalEntity russianLegalEntityDomain = contract.getContractor().getLegalEntity().getRussianLegalEntity();

        LegalEntity legalEntity = new LegalEntity();
        com.rbkmoney.damsel.cashreg.provider.RussianLegalEntity russianLegalEntity = new com.rbkmoney.damsel.cashreg.provider.RussianLegalEntity();

        // TODO: email
        russianLegalEntity.setEmail(proxyOptions.get("russian_legal_entity_email"));
        russianLegalEntity.setActualAddress(russianLegalEntityDomain.getActualAddress());
        russianLegalEntity.setInn(russianLegalEntityDomain.getInn());
        russianLegalEntity.setPostAddress(russianLegalEntityDomain.getPostAddress());
        russianLegalEntity.setRegisteredName(russianLegalEntityDomain.getRegisteredName());
        russianLegalEntity.setRepresentativeDocument(russianLegalEntityDomain.getRepresentativeDocument());
        russianLegalEntity.setRepresentativeFullName(russianLegalEntityDomain.getRepresentativeFullName());
        russianLegalEntity.setRepresentativePosition(russianLegalEntityDomain.getRepresentativePosition());

        RussianBankAccount russianBankAccount = new RussianBankAccount();

        russianLegalEntity.setRussianBankAccount(russianBankAccount);

        // TODO: from options?
        russianLegalEntity.setTaxMode(TaxMode.usn_income);

        legalEntity.setRussianLegalEntity(russianLegalEntity);
        return legalEntity;
    }

    private com.rbkmoney.damsel.cashreg.provider.Cart prepareCart(CashReg cashreg) {
        com.rbkmoney.damsel.cashreg.provider.Cart providerCard = new com.rbkmoney.damsel.cashreg.provider.Cart();

        List<ItemsLine> itemsLines = new ArrayList<>();
        for (com.rbkmoney.damsel.cashreg_processing.ItemsLine itemsLine : cashreg.getPaymentInfo().getCart().getLines()) {
            ItemsLine item = new ItemsLine();
            item.setPrice(itemsLine.getPrice());
            item.setProduct(itemsLine.getProduct());
            item.setQuantity(itemsLine.getQuantity());
            item.setTax(itemsLine.getTax());
            itemsLines.add(item);
        }
        providerCard.setLines(itemsLines);

        return providerCard;
    }

    @Override
    protected SignalResultData<Change> processSignalTimeout(String namespace, String machineId, List<TMachineEvent<Change>> tMachineEvents) {

        // TODO: all logic
        // TODO: session, polling, fail or delivered
        log.info("Request processSignalTimeout() machineId: {} list: {}", machineId, tMachineEvents);
        SignalResultData<Change> resultData = new SignalResultData<>(Collections.emptyList(), new ComplexAction());
        log.info("Response of processSignalTimeout: {}", resultData);
        return resultData;
    }

    /**
     * For repairer
     */
    @Override
    protected CallResultData<Change> processCall(String namespace, String machineId, Value args, List<TMachineEvent<Change>> tMachineEvents) {
        return new CallResultData<>(getLastEvent(tMachineEvents), Collections.emptyList(), new ComplexAction());
    }

    private Change getLastEvent(List<TMachineEvent<Change>> tMachineEvents) {
        if (tMachineEvents.isEmpty()) {
            return null;
        }
        return tMachineEvents.get(tMachineEvents.size() - 1).getData();
    }

    public CashRegProviderSrv.Iface cashRegProviderSrv(String url, Integer networkTimeout) {
        try {
            return new THSpawnClientBuilder()
                    .withAddress(new URI(url))
                    .withNetworkTimeout(networkTimeout)
                    .build(CashRegProviderSrv.Iface.class);
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }
}
