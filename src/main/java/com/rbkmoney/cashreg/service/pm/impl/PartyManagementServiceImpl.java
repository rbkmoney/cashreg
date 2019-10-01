package com.rbkmoney.cashreg.service.pm.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.rbkmoney.cashreg.service.exception.NotFoundException;
import com.rbkmoney.cashreg.service.exception.PartyNotFoundException;
import com.rbkmoney.cashreg.service.pm.PartyManagementService;
import com.rbkmoney.damsel.cashreg_processing.CashRegParams;
import com.rbkmoney.damsel.domain.Contract;
import com.rbkmoney.damsel.domain.Party;
import com.rbkmoney.damsel.domain.PaymentInstitutionRef;
import com.rbkmoney.damsel.domain.Shop;
import com.rbkmoney.damsel.payment_processing.*;
import com.rbkmoney.geck.common.util.TypeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.AbstractMap;
import java.util.Map;

import static com.rbkmoney.damsel.payment_processing.PartyRevisionParam.revision;

@Slf4j
@Service
public class PartyManagementServiceImpl implements PartyManagementService {

    private final UserInfo userInfo = new UserInfo("admin", UserType.internal_user(new InternalUser()));

    private final PartyManagementSrv.Iface partyManagementClient;

    private final Cache<Map.Entry<String, PartyRevisionParam>, Party> partyCache;

    @Autowired
    public PartyManagementServiceImpl(
            PartyManagementSrv.Iface partyManagementClient,
            @Value("${cache.maxSize}") long cacheMaximumSize
    ) {
        this.partyManagementClient = partyManagementClient;
        this.partyCache = Caffeine.newBuilder()
                .maximumSize(cacheMaximumSize)
                .build();
    }

    private Party getParty(String partyId, PartyRevisionParam partyRevisionParam) throws NotFoundException {
        log.info("Trying to get party, partyId='{}', partyRevisionParam='{}'", partyId, partyRevisionParam);
        Party party = partyCache.get(
                new AbstractMap.SimpleEntry<>(partyId, partyRevisionParam),
                key -> {
                    try {
                        return partyManagementClient.checkout(userInfo, partyId, partyRevisionParam);
                    } catch (PartyNotFound ex) {
                        throw new NotFoundException(
                                String.format("Party not found, partyId='%s', partyRevisionParam='%s'", partyId, partyRevisionParam), ex
                        );
                    } catch (InvalidPartyRevision ex) {
                        throw new NotFoundException(
                                String.format("Invalid party revision, partyId='%s', partyRevisionParam='%s'", partyId, partyRevisionParam), ex
                        );
                    } catch (TException ex) {
                        throw new RuntimeException(
                                String.format("Failed to get party, partyId='%s', partyRevisionParam='%s'", partyId, partyRevisionParam), ex
                        );
                    }
                });
        log.info("Party has been found, partyId='{}', partyRevisionParam='{}'", partyId, partyRevisionParam);
        return party;
    }

    @Override
    public Shop getShop(String partyId, String shopId) throws NotFoundException {
        log.info("Trying to get shop, partyId='{}', shopId='{}', ", partyId, shopId);
        PartyRevisionParam partyRevisionParam = PartyRevisionParam.timestamp(TypeUtil.temporalToString(Instant.now()));
        Party party = getParty(partyId, partyRevisionParam);
        Shop shop = party.getShops().get(shopId);
        if (shop == null) {
            throw new NotFoundException(String.format("Shop not found, partyId='%s', shopId='%s'", partyId, shopId));
        }
        log.info("Shop has been found, partyId='{}', shopId='{}'", partyId, shopId);
        return shop;
    }

    @Override
    public Shop getShop(CashRegParams cashRegParams) throws NotFoundException {
        return getShop(cashRegParams.getPartyId(), cashRegParams.getShopId());
    }

    @Override
    public Contract getContract(String partyId, String contractId) throws NotFoundException {
        log.info("Trying to get contract, partyId='{}', contractId='{}'", partyId, contractId);
        PartyRevisionParam partyRevisionParam = revision(getPartyRevision(partyId));
        Party party = getParty(partyId, partyRevisionParam);
        Contract contract = party.getContracts().get(contractId);
        if (contract == null) {
            throw new NotFoundException(String.format("Contract not found, partyId='%s', contractId='%s', partyRevisionParam='%s'", partyId, contractId, partyRevisionParam));
        }
        log.info("Contract has been found, partyId='{}', contractId='{}', partyRevisionParam='{}'", partyId, contractId, partyRevisionParam);
        return contract;
    }

    @Override
    public long getPartyRevision(String partyId) {
        try {
            log.info("Trying to get revision, partyId='{}'", partyId);
            long revision = partyManagementClient.getRevision(userInfo, partyId);
            log.info("Revision has been found, partyId='{}', revision='{}'", partyId, revision);
            return revision;
        } catch (PartyNotFound ex) {
            throw new PartyNotFoundException(String.format("Party not found, partyId='%s'", partyId), ex);
        } catch (TException ex) {
            throw new RuntimeException(String.format("Failed to get party revision, partyId='%s'", partyId), ex);
        }
    }

    @Override
    public PaymentInstitutionRef getPaymentInstitutionRef(String partyId, String contractId) throws NotFoundException {
        log.debug("Trying to get paymentInstitutionRef, partyId='{}', contractId='{}', partyRevisionParam='{}'", partyId, contractId);
        Contract contract = getContract(partyId, contractId);

        if (!contract.isSetPaymentInstitution()) {
            throw new NotFoundException(String.format("PaymentInstitutionRef not found, partyId='%s', contractId='%s'", partyId, contractId));
        }

        PaymentInstitutionRef paymentInstitutionRef = contract.getPaymentInstitution();
        log.info("PaymentInstitutionRef has been found, partyId='{}', contractId='{}', paymentInstitutionRef='{}'", partyId, contractId, paymentInstitutionRef);
        return paymentInstitutionRef;
    }
}
