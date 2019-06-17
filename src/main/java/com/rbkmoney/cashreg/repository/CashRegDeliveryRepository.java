package com.rbkmoney.cashreg.repository;


import com.rbkmoney.cashreg.entity.CashRegDelivery;
import com.rbkmoney.cashreg.entity.InvoicePayer;
import com.rbkmoney.cashreg.entity.Payment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CashRegDeliveryRepository extends JpaRepository<CashRegDelivery, Long> {

    List<CashRegDelivery> findByTypeOperationAndCashregStatus(String typeOperation, String cashregStatus);

    List<CashRegDelivery> findByTypeOperationAndCashregStatus(String typeOperation, String cashregStatus, Pageable pageable);

    List<CashRegDelivery> findByCashregStatus(String cashregStatus);

    List<CashRegDelivery> findByCashregStatus(String cashregStatus, Pageable pageable);

    CashRegDelivery findByInvoiceIdAndPaymentIdAndTypeOperationAndPaymentIdNotNull(InvoicePayer InvoiceId, Payment paymentId, String typeOperation);
}
