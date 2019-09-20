package com.rbkmoney.cashreg.utils;

import com.rbkmoney.cashreg.service.dominant.DominantService;
import com.rbkmoney.cashreg.service.pm.PartyManagementService;
import com.rbkmoney.cashreg.utils.cashreg.creators.StatusCreators;
import com.rbkmoney.damsel.cashreg.provider.CashRegContext;
import com.rbkmoney.damsel.cashreg.provider.CashRegProviderSrv;
import com.rbkmoney.damsel.cashreg.provider.Session;
import com.rbkmoney.damsel.cashreg.provider.SourceCreation;
import com.rbkmoney.damsel.cashreg.type.Type;
import com.rbkmoney.damsel.cashreg_domain.*;
import com.rbkmoney.damsel.cashreg_processing.*;
import com.rbkmoney.damsel.domain.Contract;
import com.rbkmoney.damsel.domain.PaymentInstitutionRef;
import com.rbkmoney.damsel.domain.ProxyObject;
import com.rbkmoney.damsel.domain.Shop;
import com.rbkmoney.geck.serializer.Geck;
import com.rbkmoney.machinegun.base.Timer;
import com.rbkmoney.machinegun.msgpack.Value;
import com.rbkmoney.machinegun.stateproc.*;
import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProtoUtils {

    public static Change toCashRegCreated(
            CashRegParams params,
            PartyManagementService partyManagementService,
            DominantService dominantService
    ) {
        CreatedChange created = new CreatedChange();
        CashReg cashReg = new CashReg();
        cashReg.setId(params.getId());
        cashReg.setPaymentInfo(params.getPaymentInfo());
        cashReg.setType(params.getType());
        cashReg.setShopId(params.getShopId());
        cashReg.setPartyId(params.getPartyId());
        cashReg.setStatus(StatusCreators.createPendingStatus());

        Shop shop = partyManagementService.getShop(params.getShopId(), params.getPartyId());

        PaymentInstitutionRef paymentInstitutionRef = partyManagementService.getPaymentInstitutionRef(params.getPartyId(), shop.getContractId());
        ProxyObject proxyObject = dominantService.getProxyObject(paymentInstitutionRef);
        if (!proxyObject.isSetData()) {
            throw new IllegalStateException("ProxyObject not found; paymentInstitutionRef: " + paymentInstitutionRef.getId());
        }

        // Данные по ставкам, типу налогооблажения и т.д.
        Contract contract = partyManagementService.getContract(params.getPartyId(), shop.getContractId());
        Map<String, String> proxyOptions = proxyObject.getData().getOptions();

        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setLegalEntity(prepareLegalEntity(contract, proxyOptions));
        cashReg.setAccountInfo(accountInfo);

        created.setCashreg(cashReg);
        return Change.created(created);
    }

    public static CashReg listChangesToCashReg(List<Change> changes) {
        CashReg cashReg = new CashReg();
        if (changes.size() == 0) {
            throw new IllegalArgumentException("CashReg List<changes> is empty");
        }
        for (Change change : changes) {
            if (change.isSetCreated()) {
                cashReg = change.getCreated().getCashreg();
            }
            if (change.isSetStatusChanged()) {
                cashReg.setStatus(change.getStatusChanged().getStatus());
            }
            if (change.isSetSession()) {
                prepareSessionStatus(change, cashReg);
            }
        }
        return cashReg;
    }

    private static void prepareSessionStatus(Change change, CashReg cashReg) {
        if (change.getSession().getPayload().isSetFinished()) {
            SessionFinished sessionFinished = change.getSession().getPayload().getFinished();
            SessionResult sessionResult = sessionFinished.getResult();
            if (sessionResult.isSetFailed()) {
                cashReg.setStatus(StatusCreators.createFailedStatus());
            }
            if (sessionResult.isSetSucceeded()) {
                cashReg.setInfo(sessionResult.getSucceeded().getInfo());
                cashReg.setStatus(StatusCreators.createDeliveredStatus());
            }
        } else {
            cashReg.setStatus(StatusCreators.createPendingStatus());
        }
    }

    public static ComplexAction buildComplexActionWithDeadline(Instant deadline, HistoryRange historyRange) {
        return buildComplexActionWithTimer(Timer.deadline(deadline.toString()), historyRange);
    }

    public static ComplexAction buildComplexActionWithTimer(Timer timer, HistoryRange historyRange) {
        SetTimerAction setTimerAction = new SetTimerAction().setTimer(timer).setRange(historyRange);
        return new ComplexAction().setTimer(TimerAction.set_timer(setTimerAction));
    }

    public static HistoryRange buildLastEventHistoryRange() {
        HistoryRange historyRange = new HistoryRange();
        historyRange.setDirection(Direction.backward);
        historyRange.setLimit(1);
        return historyRange;
    }

    public static Value toValue(List<Change> changes) {
        List<Value> values = changes.stream().map(pc -> Value.bin(Geck.toMsgPack(pc))).collect(Collectors.toList());
        return Value.arr(values);
    }

    public static List<Change> toChangeList(Value value) {
        return value.getArr().stream().map(v -> Geck.msgPackToTBase(v.getBin(), Change.class)).collect(Collectors.toList());
    }

    private static SourceCreation prepareSourceCreation(CashReg cashreg) {
        SourceCreation sourceCreation = new SourceCreation();
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCart(cashreg.getPaymentInfo().getCart());
        paymentInfo.setCash(cashreg.getPaymentInfo().getCash());
        paymentInfo.setEmail(cashreg.getPaymentInfo().getEmail());
        sourceCreation.setPayment(paymentInfo);
        return sourceCreation;
    }

    private static Session prepareSession(Type type) {
        Session session = new Session();
        session.setType(type);
        return session;
    }

    private static LegalEntity prepareLegalEntity(Contract contract, Map<String, String> proxyOptions) {
        com.rbkmoney.damsel.domain.RussianLegalEntity russianLegalEntityDomain = contract.getContractor().getLegalEntity().getRussianLegalEntity();

        LegalEntity legalEntity = new LegalEntity();
        com.rbkmoney.damsel.cashreg_domain.RussianLegalEntity russianLegalEntity = new com.rbkmoney.damsel.cashreg_domain.RussianLegalEntity();

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

    public static ProxyObject extractProxyObject(
            PartyManagementService partyManagementService,
            DominantService dominantService,
            String shopId, String partyId

    ) {
        Shop shop = partyManagementService.getShop(shopId, partyId);
        PaymentInstitutionRef paymentInstitutionRef = partyManagementService.getPaymentInstitutionRef(partyId, shop.getContractId());
        ProxyObject proxyObject = dominantService.getProxyObject(paymentInstitutionRef);
        if (!proxyObject.isSetData()) {
            throw new IllegalStateException("ProxyObject not found; paymentInstitutionRef: " + paymentInstitutionRef.getId());
        }

        return proxyObject;
    }

    public static CashRegContext prepareCashRegContext(CashReg cashReg, Map<String, String> proxyOptions) {
        CashRegContext context = new CashRegContext();
        context.setAccountInfo(cashReg.getAccountInfo());
        context.setOptions(proxyOptions);

        Session session = prepareSession(cashReg.getType());
        context.setSession(session);

        SourceCreation sourceCreation = prepareSourceCreation(cashReg);
        context.setSourceCreation(sourceCreation);
        return context;
    }


    public static CashRegProviderSrv.Iface cashRegProviderSrv(String url, Integer networkTimeout) {
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
