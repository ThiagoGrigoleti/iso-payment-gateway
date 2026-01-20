package com.example.isogateway.api.dto;

import com.example.isogateway.core.domain.TransactionStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionResponse {

    private Long transactionId;
    private String stan;
    private TransactionStatus status;
    private String responseCode;
    private String responseDescription;
    private String cardNumberMasked;
    private BigDecimal amount;
    private String currency;
    private String authorizationCode;
    private Long processingTimeMs;
    private LocalDateTime timestamp;
    private String errorMessage;

    public static TransactionResponse success(Long id, String stan, String responseCode, 
                                               String cardMasked, BigDecimal amount, 
                                               String authCode, Long processingTime) {
        return TransactionResponse.builder()
                .transactionId(id)
                .stan(stan)
                .status(TransactionStatus.APPROVED)
                .responseCode(responseCode)
                .responseDescription(getResponseDescription(responseCode))
                .cardNumberMasked(cardMasked)
                .amount(amount)
                .currency("BRL")
                .authorizationCode(authCode)
                .processingTimeMs(processingTime)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static TransactionResponse declined(Long id, String stan, String responseCode,
                                                String cardMasked, BigDecimal amount) {
        return TransactionResponse.builder()
                .transactionId(id)
                .stan(stan)
                .status(TransactionStatus.DECLINED)
                .responseCode(responseCode)
                .responseDescription(getResponseDescription(responseCode))
                .cardNumberMasked(cardMasked)
                .amount(amount)
                .currency("BRL")
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static TransactionResponse error(String stan, String errorMessage) {
        return TransactionResponse.builder()
                .stan(stan)
                .status(TransactionStatus.ERROR)
                .errorMessage(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static TransactionResponse timeout(String stan) {
        return TransactionResponse.builder()
                .stan(stan)
                .status(TransactionStatus.TIMEOUT)
                .errorMessage("Bank did not respond within timeout period")
                .timestamp(LocalDateTime.now())
                .build();
    }

    private static String getResponseDescription(String code) {
        if (code == null) return "Unknown";
        return switch (code) {
            case "00" -> "Approved";
            case "01" -> "Refer to card issuer";
            case "03" -> "Invalid merchant";
            case "04" -> "Capture card";
            case "05" -> "Do not honor";
            case "12" -> "Invalid transaction";
            case "13" -> "Invalid amount";
            case "14" -> "Invalid card number";
            case "30" -> "Format error";
            case "41" -> "Lost card";
            case "43" -> "Stolen card";
            case "51" -> "Insufficient funds";
            case "54" -> "Expired card";
            case "55" -> "Incorrect PIN";
            case "57" -> "Transaction not allowed";
            case "58" -> "Transaction not allowed for terminal";
            case "61" -> "Exceeds withdrawal limit";
            case "62" -> "Restricted card";
            case "65" -> "Exceeds withdrawal frequency";
            case "75" -> "PIN tries exceeded";
            case "91" -> "Issuer unavailable";
            case "96" -> "System malfunction";
            default -> "Unknown response code: " + code;
        };
    }
}
