package com.backend.ecommerce.payments.thewallet.payload;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TheWalletNotification {

    private String accountname; // Optional for some transaction types
    private String msisdn;
    private String channel;
    private String type; // "lodgement", "ussdpush", "notification" (for disbursement confirmation)
    private BigDecimal amount;
    private String amounttype;
    private String currency; // Present for Disbursements, may be for others
    private String target; // Present for USSD Push, may be for Disbursements
    private String mac;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime date;
    private String sourcereference; // Original source reference you sent
    private String reference; // TheWallet's new unique reference for this notification
    private String operatorreference; // Mobile Network Operator's reference
    private String customerreference; // From C2B, or partner provided
    private Boolean success; // "true" for Lodgement Notifications, actual status for others
}