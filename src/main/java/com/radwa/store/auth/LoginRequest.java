package com.radwa.store.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Email must be provide")
    @Email
    private String email;

    @NotBlank(message = "Password must be provide")
    private String password;

}
