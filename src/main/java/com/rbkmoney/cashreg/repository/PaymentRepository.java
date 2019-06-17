package com.rbkmoney.cashreg.repository;


import com.rbkmoney.cashreg.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

}
