package com.rbkmoney.cashreg.service.pm.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.rbkmoney.cashreg.service.exception.NotFoundException;
import com.rbkmoney.cashreg.service.exception.PartyNotFoundException;
import com.rbkmoney.cashreg.service.pm.PartyManagementService;
import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.payment_processing.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    private Party getParty(String partyId, PartyRevisionParam partyRevisionParam) {
        log.debug("Trying to get party, partyId='{}', partyRevisionParam='{}'", partyId, partyRevisionParam);
        Party party = partyCache.get(
                Map.entry(partyId, partyRevisionParam),
                key -> callCheckout(partyId, partyRevisionParam));
        log.debug("Party has been found, partyId='{}', partyRevisionParam='{}'", partyId, partyRevisionParam);
        return party;
    }

    @Override
    public Shop getShop(String partyId, String shopId, Long revision) {
        log.debug("Trying to get shop, partyId='{}', shopId='{}', ", partyId, shopId);
        PartyRevisionParam partyRevisionParam = PartyRevisionParam.revision(getPartyRevision(partyId));
        Party party = getParty(partyId, partyRevisionParam);
        Shop shop = party.getShops().get(shopId);
        if (shop == null) {
            throw new NotFoundException(String.format("Shop not found, partyId='%s', shopId='%s'", partyId, shopId));
        }
        log.debug("Shop has been found, partyId='{}', shopId='{}'", partyId, shopId);
        return shop;
    }

    @Override
    public Contract getContract(String partyId, String contractId, Long revision) {
        log.debug("Trying to get contract, partyId='{}', contractId='{}', revision='{}", partyId, contractId, revision);
        PartyRevisionParam partyRevisionParam = revision(revision);
        Party party = getParty(partyId, partyRevisionParam);
        Contract contract = party.getContracts().get(contractId);
        if (contract == null) {
            throw new NotFoundException(String.format("Contract not found, partyId='%s', contractId='%s', partyRevisionParam='%s'", partyId, contractId, partyRevisionParam));
        }
        log.debug("Contract has been found, partyId='{}', contractId='{}', partyRevisionParam='{}'", partyId, contractId, partyRevisionParam);
        return contract;
    }

    @Override
    public long getPartyRevision(String partyId) {
        try {
            log.debug("Trying to get revision, partyId='{}'", partyId);
            long revision = partyManagementClient.getRevision(userInfo, partyId);
            log.debug("Revision has been found, partyId='{}', revision='{}'", partyId, revision);
            return revision;
        } catch (PartyNotFound ex) {
            throw new PartyNotFoundException(String.format("Party not found, partyId='%s'", partyId), ex);
        } catch (TException ex) {
            throw new RuntimeException(String.format("Failed to get party revision, partyId='%s'", partyId), ex);
        }
    }

    @Override
    public Contractor getContractor(String partyId, String contractorId, Long revision) {
        log.debug("Trying to get Contractor, partyId='{}', contractId='{}', revision='{}", partyId, contractorId, revision);
        PartyRevisionParam partyRevisionParam = revision(revision);
        Party party = getParty(partyId, partyRevisionParam);
        PartyContractor partyContractor = party.getContractors().get(contractorId);
        if (partyContractor == null) {
            throw new NotFoundException(String.format("Contractor not found, partyId='%s', contractorId='%s'", party.getId(), contractorId));
        }
        log.debug("Contractor has been found, partyId='{}', contractorId='{}'", partyId, contractorId);
        return partyContractor.getContractor();
    }

    private Party callCheckout(String partyId, PartyRevisionParam partyRevisionParam) {
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
    }

}
