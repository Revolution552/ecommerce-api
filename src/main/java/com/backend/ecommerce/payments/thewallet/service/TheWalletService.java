package com.backend.ecommerce.payments.thewallet.service;

import com.backend.ecommerce.payments.thewallet.payload.DisbursementRequest;
import com.backend.ecommerce.payments.thewallet.payload.PushUssdRequest;
import com.backend.ecommerce.payments.thewallet.payload.TheWalletInitiateResponse;
import com.backend.ecommerce.payments.thewallet.security.TheWalletMacGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class TheWalletService {

    private static final Logger log = LoggerFactory.getLogger(TheWalletService.class);

    private final WebClient webClient;
    private final TheWalletMacGenerator macGenerator;

    @Value("${thewallet.partner-sourcereference}")
    private String partnerSourceReference;

    @Value("${thewallet.callback.url}")
    private String callbackUrl;

    // MNO Channels
    @Value("${thewallet.channel.tigo}") private String tigoChannel;
    @Value("${thewallet.channel.vodacom}") private String vodacomChannel;
    @Value("${thewallet.channel.airtel}") private String airtelChannel;
    @Value("${thewallet.channel.halotel}") private String halotelChannel;


    public TheWalletService(@Value("${thewallet.api.base-url}") String baseUrl, TheWalletMacGenerator macGenerator) {
        log.info("Initializing TheWalletService with base URL: {}", baseUrl);
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
        this.macGenerator = macGenerator;
    }

    /**
     * Initiates a Push USSD / Charge payment request to TheWallet.
     * @param msisdn Customer's phone number.
     * @param amount Amount to charge.
     * @param channel MNO channel (e.g., TANZANIA.TIGO).
     * @param target USSD short code or merchant code.
     * @param transactionReference Your unique internal transaction ID.
     * @return Mono of TheWalletInitiateResponse.
     */
    public Mono<TheWalletInitiateResponse> initiatePushUssd(String msisdn, String amount, String channel, String target, String transactionReference) {
        log.info("Starting Push USSD initiation for reference: {}", transactionReference);
        LocalDateTime now = LocalDateTime.now();
        String mac = macGenerator.generateMac(msisdn, now);

        PushUssdRequest request = PushUssdRequest.builder()
                .amount(new java.math.BigDecimal(amount))
                .amounttype("unit")
                .channel(channel)
                .currency("TZS")
                .sourcereference(transactionReference)
                .msisdn(msisdn)
                .type("pushussd")
                .target(target)
                .mac(mac)
                .date(now)
                .build();

        log.debug("Push USSD request payload: {}", request); // Use debug for detailed payload
        return webClient.post()
                .uri("/pushussd")
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .bodyToMono(TheWalletInitiateResponse.class)
                .doOnSuccess(response -> {
                    log.info("Push USSD response for {}: Success={}, TheWalletReference={}",
                            request.getSourcereference(), response.getSuccess(), response.getReference());
                    if (Boolean.FALSE.equals(response.getSuccess())) {
                        String errorMessage = response.getError() != null ? response.getError().getMessage() : "No error message provided.";
                        log.error("Push USSD request failed. Error from TheWallet: Code={}, Message={}",
                                response.getError() != null ? response.getError().getCode() : "N/A",
                                errorMessage);
                    }
                })
                .doOnError(e -> log.error("Push USSD request failed for {}. Exception: {}",
                        request.getSourcereference(), e.getMessage(), e))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)).filter(throwable ->
                        throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException &&
                                ((org.springframework.web.reactive.function.client.WebClientResponseException) throwable).getStatusCode().is5xxServerError()
                ));
    }

    /**
     * Initiates a Disbursement payment request to TheWallet.
     * @param msisdn Customer's phone number.
     * @param amount Amount to disburse.
     * @param channel MNO channel (e.g., TANZANIA.VODACOM).
     * @param transactionReference Your unique internal transaction ID.
     * @return Mono of TheWalletInitiateResponse.
     */
    public Mono<TheWalletInitiateResponse> initiateDisbursement(String msisdn, String amount, String channel, String transactionReference) {
        log.info("Starting Disbursement initiation for reference: {}", transactionReference);
        LocalDateTime now = LocalDateTime.now();
        String mac = macGenerator.generateMac(msisdn, now);

        DisbursementRequest request = DisbursementRequest.builder()
                .amount(new java.math.BigDecimal(amount))
                .amounttype("unit")
                .channel(channel)
                .currency("TZS")
                .sourcereference(transactionReference)
                .msisdn(msisdn)
                .type("disbursement")
                .mac(mac)
                .date(now)
                .build();

        log.debug("Disbursement request payload: {}", request);
        return webClient.post()
                .uri("/disbursement")
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .bodyToMono(TheWalletInitiateResponse.class)
                .doOnSuccess(response -> {
                    log.info("Disbursement response for {}: Success={}, TheWalletReference={}",
                            request.getSourcereference(), response.getSuccess(), response.getReference());
                    if (Boolean.FALSE.equals(response.getSuccess())) {
                        String errorMessage = response.getError() != null ? response.getError().getMessage() : "No error message provided.";
                        log.error("Disbursement request failed. Error from TheWallet: Code={}, Message={}",
                                response.getError() != null ? response.getError().getCode() : "N/A",
                                errorMessage);
                    }
                })
                .doOnError(e -> log.error("Disbursement request failed for {}. Exception: {}",
                        request.getSourcereference(), e.getMessage(), e))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)).filter(throwable ->
                        throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException &&
                                ((org.springframework.web.reactive.function.client.WebClientResponseException) throwable).getStatusCode().is5xxServerError()
                ));
    }

    public String getTigoChannel() {
        log.debug("Tigo channel requested: {}", tigoChannel);
        return tigoChannel;
    }
    public String getVodacomChannel() {
        log.debug("Vodacom channel requested: {}", vodacomChannel);
        return vodacomChannel;
    }
    public String getAirtelChannel() {
        log.debug("Airtel channel requested: {}", airtelChannel);
        return airtelChannel;
    }
    public String getHalotelChannel() {
        log.debug("Halotel channel requested: {}", halotelChannel);
        return halotelChannel;
    }
}