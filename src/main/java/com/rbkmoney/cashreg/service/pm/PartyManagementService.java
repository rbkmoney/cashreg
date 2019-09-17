package com.rbkmoney.cashreg.service.pm;

import com.rbkmoney.cashreg.service.exception.NotFoundException;
import com.rbkmoney.damsel.domain.Contract;
import com.rbkmoney.damsel.domain.PaymentInstitutionRef;
import com.rbkmoney.damsel.domain.Shop;

public interface PartyManagementService {

    Shop getShop(String partyId, String shopId) throws NotFoundException;

    PaymentInstitutionRef getPaymentInstitutionRef(String partyId, String contractId) throws NotFoundException;

    Contract getContract(String partyId, String contractId) throws NotFoundException;
}
