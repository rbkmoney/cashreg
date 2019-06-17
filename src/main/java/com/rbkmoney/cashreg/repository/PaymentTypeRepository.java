package com.rbkmoney.cashreg.repository;


import com.rbkmoney.cashreg.entity.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PaymentTypeRepository extends JpaRepository<PaymentType, Long> {

    PaymentType findByType(String paymentType);

}
