package com.backend.ecommerce.payments.thewallet.payload;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
public class DisbursementRequest extends TheWalletBaseRequest {

    @NotBlank(message = "Currency is required")
    private String currency = "TZS"; // As per doc, typically "TZS"
}