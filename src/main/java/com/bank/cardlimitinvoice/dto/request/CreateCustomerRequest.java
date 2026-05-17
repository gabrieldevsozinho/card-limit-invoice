package com.bank.cardlimitinvoice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCustomerRequest(
        @NotBlank @Size(min = 2, max = 150) String name,
        @NotBlank @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 dígitos") String cpf,
        @NotBlank @Email String email
) {}
