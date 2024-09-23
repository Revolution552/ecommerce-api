package com.backend.ecommerce.payment.service;

import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PayPalService {

    @Autowired
    private APIContext apiContext;

    public Payment createPayment(Double total, String currency, String method, String intent,
                                 String description, String cancelUrl, String successUrl) throws PayPalRESTException {

        if (total == null || currency == null || method == null || intent == null ||
                description == null || cancelUrl == null || successUrl == null) {
            throw new IllegalArgumentException("One or more input parameters are null.");
        }

        // Create the amount object
        Amount amount = new Amount();
        amount.setCurrency(currency);
        amount.setTotal(String.format("%.2f", total));

        // Create a transaction
        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);

        // Add transaction to list (PayPal requires a list of transactions)
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        // Set payer information
        Payer payer = new Payer();
        payer.setPaymentMethod(method);

        // Create the payment object
        Payment payment = new Payment();
        payment.setIntent(intent);
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        // Set redirect URLs for success and cancel
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        redirectUrls.setReturnUrl(successUrl);
        payment.setRedirectUrls(redirectUrls);

        // Create the payment using PayPal API
        try {
            return payment.create(apiContext);
        } catch (PayPalRESTException e) {
            throw new PayPalRESTException("Failed to create PayPal payment: " + e.getMessage(), e);
        }
    }

    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        if (paymentId == null || payerId == null) {
            throw new IllegalArgumentException("Payment ID or Payer ID is null.");
        }

        Payment payment = new Payment();
        payment.setId(paymentId);

        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);

        // Execute the payment using the APIContext
        try {
            return payment.execute(apiContext, paymentExecution);
        } catch (PayPalRESTException e) {
            throw new PayPalRESTException("Failed to execute PayPal payment: " + e.getMessage(), e);
        }
    }
}
