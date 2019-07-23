package com.rbkmoney.cashreg.service;


import com.rbkmoney.cashreg.entity.CashRegDelivery;
import com.rbkmoney.cashreg.entity.InvoicePayer;
import com.rbkmoney.cashreg.entity.Payment;
import com.rbkmoney.cashreg.repository.CashRegDeliveryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

import static com.rbkmoney.cashreg.utils.constant.CashRegStatus.READY;
import static com.rbkmoney.cashreg.utils.constant.CashRegTypeOperation.REFUND_DEBIT;

@Slf4j
@Service
public class CashRegDeliveryService {

    private CashRegDeliveryRepository cashRegDeliveryRepository;

    @Autowired
    public CashRegDeliveryService(CashRegDeliveryRepository cashRegDeliveryRepository) {
        Assert.notNull(cashRegDeliveryRepository, "CashRegDeliveryRepository must not be null");

        this.cashRegDeliveryRepository = cashRegDeliveryRepository;
    }

    public CashRegDelivery findByTypeOperationAndCashregStatus(InvoicePayer invoiceId, Payment paymentId, String typeOperation) {
        log.debug("Trying to find CashRegDelivery from the DB");
        CashRegDelivery response = cashRegDeliveryRepository.findByInvoiceIdAndPaymentIdAndTypeOperationAndPaymentIdNotNull(invoiceId, paymentId, typeOperation);
        log.debug("Finish save method. DB returned [{}]", response);
        return response;
    }

    public List<CashRegDelivery> findByTypeOperationAndCashregStatus(String typeOperation, String cashregStatus) {
        log.debug("Trying to save CashRegDelivery from the DB");
        List<CashRegDelivery> response = cashRegDeliveryRepository.findByTypeOperationAndCashregStatus(typeOperation, cashregStatus);
        log.debug("Finish save method. DB returned [{}]", response);
        return response;
    }

    public List<CashRegDelivery> findByTypeOperationAndCashregStatusPagination(String typeOperation, String cashregStatus, int page, int size) {
        log.debug("Trying to save CashRegDelivery from the DB");
        List<CashRegDelivery> response = cashRegDeliveryRepository.findByTypeOperationAndCashregStatus(typeOperation, cashregStatus, createPageRequest(page, size));
        log.debug("Finish save method. DB returned [{}]", response);
        return response;
    }

    public List<CashRegDelivery> findByCashregStatus(String cashregStatus) {
        log.debug("Trying to save CashRegDelivery from the DB");
        List<CashRegDelivery> response = cashRegDeliveryRepository.findByCashregStatus(cashregStatus);
        log.debug("Finish save method. DB returned [{}]", response);
        return response;
    }

    public List<CashRegDelivery> findByCashregStatusPagination(String cashregStatus, int page, int size) {
        log.debug("Trying to save CashRegDelivery from the DB");
        List<CashRegDelivery> response = cashRegDeliveryRepository.findByCashregStatus(cashregStatus, createPageRequest(page, size));
        log.debug("Finish save method. DB returned [{}]", response);
        return response;
    }

    public CashRegDelivery save(CashRegDelivery cashRegDelivery) {
        log.debug("Trying to save CashRegDelivery from the DB");
        CashRegDelivery response = cashRegDeliveryRepository.save(cashRegDelivery);
        log.debug("Finish save method. DB returned [{}]", response);
        return response;
    }
    private Pageable createPageRequest(int page, int size) {
        return new PageRequest(page, size, Sort.Direction.ASC, "id");
    }

    public void createRefundCashRegIfExists(InvoicePayer invoicePayer, Payment paymentDB, String typeOperation) {
        CashRegDelivery cashRegDeliveryCheck = findByTypeOperationAndCashregStatus(invoicePayer, paymentDB, typeOperation);

        if (cashRegDeliveryCheck != null) {
            save(new CashRegDelivery(invoicePayer, paymentDB, REFUND_DEBIT, READY));
        } else {
            log.warn("Cashreg for invoicePayer {}, payment {} wasn't found", invoicePayer, paymentDB);
        }

    }

}
