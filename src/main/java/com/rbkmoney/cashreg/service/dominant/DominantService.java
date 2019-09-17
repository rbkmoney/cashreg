package com.rbkmoney.cashreg.service.dominant;

import com.rbkmoney.cashreg.service.exception.NotFoundException;
import com.rbkmoney.damsel.domain.*;

public interface DominantService {
    PaymentInstitution getPaymentInstitution(PaymentInstitutionRef paymentInstitutionRef) throws NotFoundException;
    SystemAccountSet getSystemAccountSet(SystemAccountSetRef systemAccountSetRef) throws NotFoundException;
    ProxyObject getProxyObject(PaymentInstitutionRef paymentInstitutionRef) throws NotFoundException;
}
