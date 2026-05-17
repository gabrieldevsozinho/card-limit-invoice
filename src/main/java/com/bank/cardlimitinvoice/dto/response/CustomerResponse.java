package com.bank.cardlimitinvoice.dto.response;

import com.bank.cardlimitinvoice.domain.entity.Customer;

import java.time.LocalDateTime;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String name,
        String cpf,
        String email,
        LocalDateTime createdAt
) {
    public static CustomerResponse from(Customer c) {
        return new CustomerResponse(c.getId(), c.getName(), c.getCpf(), c.getEmail(), c.getCreatedAt());
    }
}
