package com.commerce.customer.api.dto.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "프로필 수정 응답")
public class UpdateProfileResponse {
    
    @Schema(description = "성공 메시지", example = "프로필이 성공적으로 수정되었습니다.")
    private String message;
    
    public static UpdateProfileResponse success() {
        return new UpdateProfileResponse("프로필이 성공적으로 수정되었습니다.");
    }
}