package com.example.demo.dto;


import com.example.demo.constants.UserValidationConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserDto {
    @NotNull(message = UserValidationConstants.EMAIL_REQUIRED)
    @Email(message = UserValidationConstants.INVALID_EMAIL)
    private String email;

    @NotNull(message = UserValidationConstants.FIRST_NAME_REQUIRED)
    private String firstName;

    @NotNull(message = UserValidationConstants.LAST_NAME_REQUIRED)
    private String lastName;

    @NotNull(message = UserValidationConstants.BIRTH_DATE_REQUIRED)
    @Past(message = UserValidationConstants.BIRTH_DATE_PAST)
    private LocalDate birthDate;

    private String address;
    private String phoneNumber;
}
