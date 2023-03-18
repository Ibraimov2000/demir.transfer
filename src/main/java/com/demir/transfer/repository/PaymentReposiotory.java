package com.demir.transfer.repository;

import com.demir.transfer.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentReposiotory extends JpaRepository<Payment, Long> {
}
