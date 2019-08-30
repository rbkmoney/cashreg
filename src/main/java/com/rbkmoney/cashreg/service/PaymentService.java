package com.rbkmoney.cashreg.service;


import com.rbkmoney.cashreg.entity.Payment;
import com.rbkmoney.cashreg.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class PaymentService {

    private PaymentRepository paymentRepository;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment save(Payment inputPayment) {
        log.debug("Trying to save Payment from the DB");
        Payment payment = paymentRepository.save(inputPayment);
        log.debug("Finish save method. DB returned [{}]", payment);
        return payment;
    }

}
