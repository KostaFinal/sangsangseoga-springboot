package com.kosta.sangsangseoga.domain.subscription.enums;

public enum PlanType {
    FREE, PREMIUM_MONTHLY, PREMIUM_YEARLY;

    public boolean isPremium() {
        return this != FREE;
    }
}
