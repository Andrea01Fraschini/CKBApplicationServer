package com.example.demo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor(staticName = "build")
@NoArgsConstructor
public class RequestNewAccount {
    @NotNull
    @NotBlank(message = "Username shouldn't be null")
    private String username;
    @NotNull
    @NotBlank(message = "Email shouldn't be null")
    @Email(message = "Invalid structure of the e-mail")
    private String email;
    @NotNull
    @NotBlank(message = "Password shouldn't be null")
    private String password;
}
