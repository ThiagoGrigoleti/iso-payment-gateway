package com.example.isogateway.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransactionRequest {
    
    @NotNull(message = "Cartão obrigatório")
    @Pattern(regexp = "\\d{16}", message = "Cartão deve ter 16 dígitos numéricos")
    private String cardNumber;

    @NotNull
    @Positive(message = "Valor deve ser positivo")
    private BigDecimal amount;
}