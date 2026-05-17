package com.bank.cardlimitinvoice.service;

import com.bank.cardlimitinvoice.domain.entity.Customer;
import com.bank.cardlimitinvoice.dto.request.CreateCustomerRequest;
import com.bank.cardlimitinvoice.dto.response.CustomerResponse;
import com.bank.cardlimitinvoice.exception.BusinessException;
import com.bank.cardlimitinvoice.repository.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    @DisplayName("Deve criar cliente com sucesso")
    void create_success() {
        CreateCustomerRequest request = new CreateCustomerRequest("Maria Silva", "12345678901", "maria@email.com");

        when(customerRepository.existsByCpf(request.cpf())).thenReturn(false);
        when(customerRepository.existsByEmail(request.email())).thenReturn(false);

        Customer saved = Customer.builder()
                .id(UUID.randomUUID())
                .name(request.name())
                .cpf(request.cpf())
                .email(request.email())
                .createdAt(LocalDateTime.now())
                .build();
        when(customerRepository.save(any())).thenReturn(saved);

        CustomerResponse response = customerService.create(request);

        assertThat(response.cpf()).isEqualTo(request.cpf());
        assertThat(response.email()).isEqualTo(request.email());
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando CPF já está cadastrado")
    void create_duplicateCpf() {
        CreateCustomerRequest request = new CreateCustomerRequest("João", "12345678901", "joao@email.com");
        when(customerRepository.existsByCpf(request.cpf())).thenReturn(true);

        assertThatThrownBy(() -> customerService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CPF já cadastrado");

        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando e-mail já está cadastrado")
    void create_duplicateEmail() {
        CreateCustomerRequest request = new CreateCustomerRequest("Ana", "98765432100", "ana@email.com");
        when(customerRepository.existsByCpf(request.cpf())).thenReturn(false);
        when(customerRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> customerService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("E-mail já cadastrado");

        verify(customerRepository, never()).save(any());
    }
}
