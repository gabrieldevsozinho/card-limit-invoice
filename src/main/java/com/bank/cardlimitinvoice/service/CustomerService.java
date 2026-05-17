package com.bank.cardlimitinvoice.service;

import com.bank.cardlimitinvoice.domain.entity.Customer;
import com.bank.cardlimitinvoice.dto.request.CreateCustomerRequest;
import com.bank.cardlimitinvoice.dto.response.CustomerResponse;
import com.bank.cardlimitinvoice.exception.BusinessException;
import com.bank.cardlimitinvoice.exception.ResourceNotFoundException;
import com.bank.cardlimitinvoice.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional
    public CustomerResponse create(CreateCustomerRequest request) {
        if (customerRepository.existsByCpf(request.cpf())) {
            throw new BusinessException("CPF já cadastrado: " + request.cpf());
        }
        if (customerRepository.existsByEmail(request.email())) {
            throw new BusinessException("E-mail já cadastrado: " + request.email());
        }
        Customer customer = Customer.builder()
                .name(request.name())
                .cpf(request.cpf())
                .email(request.email())
                .build();
        return CustomerResponse.from(customerRepository.save(customer));
    }

    @Transactional(readOnly = true)
    public CustomerResponse findById(UUID id) {
        return CustomerResponse.from(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> findAll() {
        return customerRepository.findAll().stream().map(CustomerResponse::from).toList();
    }

    public Customer getOrThrow(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado: " + id));
    }
}
