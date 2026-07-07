package com.kosta.sangsangseoga.domain.subscription.repository;

import com.kosta.sangsangseoga.domain.subscription.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Page<Payment> findByMember_IdOrderByCreatedAtDesc(Long memberId, Pageable pageable);
}
