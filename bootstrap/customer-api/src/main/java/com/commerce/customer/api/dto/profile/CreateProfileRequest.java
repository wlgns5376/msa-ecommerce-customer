package com.commerce.customer.api.dto.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class CreateProfileRequest {
    
    @NotBlank(message = "이름은 필수입니다.")
    private String firstName;
    
    @NotBlank(message = "성은 필수입니다.")
    private String lastName;
    
    private LocalDate birthDate;
    
    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "성별은 MALE, FEMALE, OTHER 중 하나여야 합니다.")
    private String gender;
    
    @Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}$", message = "휴대폰 번호 형식이 올바르지 않습니다.")
    private String phoneNumber;
    
    public CreateProfileRequest(String firstName, String lastName, LocalDate birthDate, 
                               String gender, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
    }
}