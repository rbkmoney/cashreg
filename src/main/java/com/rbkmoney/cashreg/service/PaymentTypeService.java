package com.rbkmoney.cashreg.service;


import com.rbkmoney.cashreg.entity.PaymentType;
import com.rbkmoney.cashreg.repository.PaymentTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Slf4j
@Service
public class PaymentTypeService {

    private PaymentTypeRepository paymentTypeRepository;

    @Autowired
    public PaymentTypeService(PaymentTypeRepository paymentTypeRepository) {
        Assert.notNull(paymentTypeRepository, "PaymentTypeRepository must not be null");

        this.paymentTypeRepository = paymentTypeRepository;
    }

    public PaymentType findByType(String paymentType) {
        log.debug("Trying to findByType PaymentType from the DB");
        PaymentType response = paymentTypeRepository.findByType(paymentType);
        log.debug("Finish findByType method. DB returned [{}]", response);
        return response;
    }

    public PaymentType save(PaymentType paymentType) {
        log.debug("Trying to save Payment from the DB");
        PaymentType response = paymentTypeRepository.save(paymentType);
        log.debug("Finish save method. DB returned [{}]", response);
        return response;
    }

}
