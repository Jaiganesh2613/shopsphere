package com.shopsphere.backend.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "User email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 150,
            message = "Email must not exceed 150 characters")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
