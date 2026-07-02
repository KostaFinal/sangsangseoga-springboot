package com.kosta.sangsangseoga.domain.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PasswordResetCompleteDto {

    private String token;
    private String newPassword;
}
