package com.bank.cardlimitinvoice.controller;

import com.bank.cardlimitinvoice.dto.request.CreateCustomerRequest;
import com.bank.cardlimitinvoice.dto.response.CustomerResponse;
import com.bank.cardlimitinvoice.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Gerenciamento de clientes")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar cliente")
    public CustomerResponse create(@Valid @RequestBody CreateCustomerRequest request) {
        return customerService.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cliente por ID")
    public CustomerResponse findById(@PathVariable UUID id) {
        return customerService.findById(id);
    }

    @GetMapping
    @Operation(summary = "Listar todos os clientes")
    public List<CustomerResponse> findAll() {
        return customerService.findAll();
    }
}
