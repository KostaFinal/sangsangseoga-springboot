package com.kosta.sangsangseoga.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "콜백 응답의 oauthSignupToken을 그대로 전달(30분 유효, 1회용)",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "oauthSignupToken이 필요합니다.")
    private String oauthSignupToken;

    @Schema(description = "닉네임. 특수문자 제외 2~10자", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "닉네임을 입력해 주세요.")
    @Pattern(regexp = "^[0-9A-Za-z가-힣]{2,10}$", message = "닉네임은 특수문자를 제외한 2~10자여야 합니다.")
    private String nickname;

    @Schema(description = "생년월일. 만 14세 미만이면 PENDING(보호자 동의 대기) 상태로 가입되며 토큰이 발급되지 않는다.",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "생년월일을 입력해 주세요.")
    @Past(message = "생년월일이 올바르지 않습니다.")
    private LocalDate birthDate;
}
