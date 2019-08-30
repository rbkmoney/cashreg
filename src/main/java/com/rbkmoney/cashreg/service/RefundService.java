package com.rbkmoney.cashreg.service;


import com.rbkmoney.cashreg.entity.Refund;
import com.rbkmoney.cashreg.repository.RefundRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RefundService {

    private RefundRepository refundRepository;

    @Autowired
    public RefundService(RefundRepository refundRepository) {
        this.refundRepository = refundRepository;
    }

    public Refund save(Refund refund) {
        log.debug("Trying to save Refund from the DB");
        Refund response = refundRepository.save(refund);
        log.debug("Finish save method. DB returned [{}]", response);
        return response;
    }

}
