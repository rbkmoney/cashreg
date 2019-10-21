package com.rbkmoney.cashreg.service.pm;

import com.rbkmoney.damsel.domain.Contract;
import com.rbkmoney.damsel.domain.PaymentInstitutionRef;
import com.rbkmoney.damsel.domain.Shop;

public interface PartyManagementService {

    Shop getShop(String partyId, String shopId);

    PaymentInstitutionRef getPaymentInstitutionRef(String partyId, String contractId);

    Contract getContract(String partyId, String contractId);

    long getPartyRevision(String partyId);
}
