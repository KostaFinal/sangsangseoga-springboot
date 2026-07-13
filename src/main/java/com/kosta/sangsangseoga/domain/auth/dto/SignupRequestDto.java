package com.kosta.sangsangseoga.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class SignupRequestDto {

    @Schema(description = "이메일", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "이메일을 입력해 주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @Schema(description = "비밀번호. 영문/숫자/특수문자 조합 8자 이상", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "비밀번호를 입력해 주세요.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
            message = "비밀번호는 영문, 숫자, 특수문자를 조합해 8자 이상이어야 합니다."
    )
    private String password;

    @Schema(description = "닉네임. 특수문자 제외 2~10자", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "닉네임을 입력해 주세요.")
    @Pattern(regexp = "^[0-9A-Za-z가-힣]{2,10}$", message = "닉네임은 특수문자를 제외한 2~10자여야 합니다.")
    private String nickname;

    @Schema(description = "프로필 이미지 URL", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String profileImageUrl;

    @Schema(description = "생년월일. 만 14세 미만이면 PENDING(보호자 동의 대기) 상태로 가입되며 토큰이 발급되지 않는다.",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "생년월일을 입력해 주세요.")
    @Past(message = "생년월일이 올바르지 않습니다.") // 미래 날짜 막음
    private LocalDate birthDate;
}
