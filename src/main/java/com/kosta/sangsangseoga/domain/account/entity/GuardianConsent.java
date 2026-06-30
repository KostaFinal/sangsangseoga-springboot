package com.kosta.sangsangseoga.domain.account.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "guardian_consent")
public class GuardianConsent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    asdasdsad;
}
