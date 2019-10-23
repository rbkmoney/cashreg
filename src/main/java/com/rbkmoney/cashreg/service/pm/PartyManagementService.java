package com.rbkmoney.cashreg.service.pm;

import com.rbkmoney.damsel.domain.Contract;
import com.rbkmoney.damsel.domain.Shop;

public interface PartyManagementService {

    Shop getShop(String partyId, String shopId, Long revision);

    Contract getContract(String partyId, String contractId, Long revision);

    long getPartyRevision(String partyId);
}
