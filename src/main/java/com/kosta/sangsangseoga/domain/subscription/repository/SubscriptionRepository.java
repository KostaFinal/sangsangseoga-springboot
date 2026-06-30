package com.kosta.sangsangseoga.domain.subscription.repository;

import com.kosta.sangsangseoga.domain.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
}
