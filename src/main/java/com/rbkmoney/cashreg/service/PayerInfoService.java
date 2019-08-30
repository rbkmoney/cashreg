package com.rbkmoney.cashreg.service;


import com.rbkmoney.cashreg.entity.PayerInfo;
import com.rbkmoney.cashreg.repository.PayerInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class PayerInfoService {

    private PayerInfoRepository payerInfoRepository;

    @Autowired
    public PayerInfoService(PayerInfoRepository payerInfoRepository) {
        this.payerInfoRepository = payerInfoRepository;
    }

    public PayerInfo findByContact(String contact) {
        log.debug("Trying to findByContact PayerInfo from the DB");
        PayerInfo paymentPayer = payerInfoRepository.findByContact(contact);
        log.debug("Finish findByContact method. DB returned [{}]", paymentPayer);
        return paymentPayer;
    }

    public PayerInfo save(PayerInfo inputPayerInfo) {
        log.debug("Trying to save PayerInfo from the DB");
        PayerInfo paymentPayer = payerInfoRepository.save(inputPayerInfo);
        log.debug("Finish save method. DB returned [{}]", paymentPayer);
        return paymentPayer;
    }

}
