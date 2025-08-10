package com.backend.ecommerce.payments.thewallet.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TheWalletInitiateResponse {
    private Boolean success;
    private String reference; // TheWallet's unique transaction ID
    private TheWalletError error;
}