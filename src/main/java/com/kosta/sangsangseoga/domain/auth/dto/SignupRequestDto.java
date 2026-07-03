package com.kosta.sangsangseoga.domain.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class SignupRequestDto {

    private String email;
    private String password;
    private String nickname;
    private String profileImageUrl;
    private LocalDate birthDate;
}
