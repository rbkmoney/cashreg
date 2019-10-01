package com.rbkmoney.cashreg.service.dominant;

import com.rbkmoney.cashreg.service.exception.NotFoundException;
import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.domain_config.VersionedObject;

public interface DominantService {
    PaymentInstitution getPaymentInstitution(PaymentInstitutionRef paymentInstitutionRef) throws NotFoundException;

    SystemAccountSet getSystemAccountSet(SystemAccountSetRef systemAccountSetRef) throws NotFoundException;

    ProxyObject getProxyObject(VersionedObject versionedObject);

    TerminalObject getTerminalObject(VersionedObject versionedObject);

    ProviderObject getProviderObject(VersionedObject versionedObject);

    VersionedObject getVersionedObjectFromPaymentInstitution(PaymentInstitutionRef paymentInstitutionRef) throws NotFoundException;

    VersionedObject getVersionedObjectFromSystemAccount(SystemAccountSetRef systemAccountSetRef);
}
