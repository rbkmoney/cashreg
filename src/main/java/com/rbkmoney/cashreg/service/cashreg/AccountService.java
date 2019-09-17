package com.rbkmoney.cashreg.service.cashreg;


import com.rbkmoney.cashreg.entity.Account;
import com.rbkmoney.cashreg.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AccountService {

    private AccountRepository accountRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account findByMerchantIdAndShopId(String merchantId, String shopId) {
        log.debug("Trying to find Account by merchantId {} and shopId {} from the DB", merchantId, shopId);
        Account account = accountRepository.findByMerchantIdAndShopIdAndIsActiveTrue(merchantId, shopId);
        log.debug("Finish findAccount method. DB returned [{}]", account);
        return account;
    }

    public Account save(Account account) {
        log.debug("Trying to save Account from the DB");
        Account response = accountRepository.save(account);
        log.debug("Finish save method. DB returned [{}]", response);
        return response;
    }

}
