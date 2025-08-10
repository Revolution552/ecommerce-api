package com.backend.ecommerce.payments.thewallet.payload;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder // Add this annotation
@NoArgsConstructor // Required for @SuperBuilder with no-arg constructors
public abstract class TheWalletBaseRequest {

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be a positive value")
    private BigDecimal amount; // TheWallet doc says 'int', but BigDecimal is safer for currency

    @NotBlank(message = "Amount type is required")
    private String amounttype = "unit"; // As per doc, typically "unit"

    @NotBlank(message = "Channel is required")
    // Example: TANZANIA.VODACOM, TANZANIA.TIGO, TANZANIA.AIRTEL, TANZANIA.HALOTEL
    private String channel;

    @NotBlank(message = "Source reference is required")
    private String sourcereference; // Your unique reference for this transaction

    @NotBlank(message = "MSISDN is required")
    @Pattern(regexp = "^255[67][0-9]{8}$", message = "MSISDN must be in full international format (e.g., 2557XXXXXXXX)")
    private String msisdn; // Customer's phone number in full international format

    @NotBlank(message = "Transaction type is required")
    private String type; // e.g., "pushussd", "disbursement"

    @NotBlank(message = "MAC code is required")
    private String mac; // Message Authentication Code (HMAC-MD5/SHA1/SHA2 hash of msisdn:date:secret)

    @NotNull(message = "Date is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime date; // LOCAL TIME OF TRANSACTION, formatted as ISO 8601 with 'Z'
}