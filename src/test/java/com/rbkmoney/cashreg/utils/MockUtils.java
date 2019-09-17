package com.rbkmoney.cashreg.utils;

import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.domain_config.RepositoryClientSrv;
import com.rbkmoney.damsel.domain_config.VersionedObject;
import com.rbkmoney.damsel.payment_processing.PartyManagementSrv;
import org.apache.thrift.TException;
import org.mockito.stubbing.Answer;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

public class MockUtils {
    public static void mockDominant(RepositoryClientSrv.Iface dominantClient) throws TException {
        doAnswer((Answer<VersionedObject>) invocation -> {
            VersionedObject versionedObject = new VersionedObject();
            Reference reference = (Reference) invocation.getArguments()[1];
            if (reference.isSetPaymentInstitution()) {
                versionedObject.setObject(
                        DomainObject.payment_institution(
                                new PaymentInstitutionObject()
                                        .setRef(new PaymentInstitutionRef(1))
                                        .setData(new PaymentInstitution().setSystemAccountSet(SystemAccountSetSelector.value(new SystemAccountSetRef(1))))));
            } else if (reference.isSetSystemAccountSet()) {
                HashMap<CurrencyRef, SystemAccount> accounts = new HashMap<>();
                accounts.put(new CurrencyRef("RUB"), new SystemAccount().setSettlement(1));
                versionedObject.setObject(
                        DomainObject.system_account_set(
                                new SystemAccountSetObject()
                                        .setRef(new SystemAccountSetRef(1))
                                        .setData(new SystemAccountSet().setAccounts(accounts))
                        )
                );
            }
            return versionedObject;
        }).when(dominantClient).checkoutObject(any(), any());
    }

    public static void mockPartyManagement(PartyManagementSrv.Iface partyManagementClient) throws TException {
        doAnswer((Answer<Party>) invocation -> {
            Party party = new Party();
            HashMap<String, Shop> shops = new HashMap<>();
            Shop shop = new Shop();
            ShopAccount shopAccount = new ShopAccount();
            shopAccount.setSettlement(123);
            shopAccount.setGuarantee(124);
            shopAccount.setPayout(125);
            shop.setAccount(shopAccount);
            shops.put("shop_id", shop);
            party.setShops(shops);
            HashMap<String, Contract> contracts = new HashMap<>();
            contracts.put("contract_id", new Contract().setPaymentInstitution(new PaymentInstitutionRef(1)));
            party.setContracts(contracts);
            return party;
        }).when(partyManagementClient).checkout(any(), any(), any());
    }

}
