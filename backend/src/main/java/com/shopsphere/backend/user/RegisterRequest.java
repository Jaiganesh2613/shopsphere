package com.shopsphere.backend.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    public RegisterRequest() {
    }

    @NotBlank(message = "User name is required")
    @Size(min = 3, max = 150,
            message = "User name must be between 3 and 150 characters")
    private String name;

    @NotBlank(message = "User email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 150,
            message = "Email must not exceed 150 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 150,
            message = "Password must be between 8 and 150 characters")
    private String password;

}
