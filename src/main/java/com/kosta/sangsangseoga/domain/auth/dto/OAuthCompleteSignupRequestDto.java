package com.kosta.sangsangseoga.domain.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class OAuthCompleteSignupRequestDto {

    @NotBlank(message = "oauthSignupToken이 필요합니다.")
    private String oauthSignupToken;

    @NotBlank(message = "닉네임을 입력해 주세요.")
    @Pattern(regexp = "^[0-9A-Za-z가-힣]{2,10}$", message = "닉네임은 특수문자를 제외한 2~10자여야 합니다.")
    private String nickname;

    @NotNull(message = "생년월일을 입력해 주세요.")
    @Past(message = "생년월일이 올바르지 않습니다.")
    private LocalDate birthDate;
}
