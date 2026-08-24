package com.payverse.paymentservice.client;

import com.payverse.paymentservice.dto.AddMoneyRequest;
import com.payverse.paymentservice.dto.WalletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;

@Component
public class WalletClient {

        private final RestTemplate restTemplate;
        private final String baseUrl;

        public WalletClient(
                        @Value("${WALLET_SERVICE_URL:http://localhost:8081}") String baseUrl) {

                this.baseUrl = baseUrl;
                this.restTemplate = new RestTemplate();
        }

    public WalletResponse debit(
            Long userId,
            BigDecimal amount,
            String idempotencyKey) {

        AddMoneyRequest request = new AddMoneyRequest();

        request.setUserId(userId);
        request.setAmount(amount);
        request.setIdempotencyKey(idempotencyKey);

        return restTemplate.postForObject(
                baseUrl + "/wallets/debit",
                request,
                WalletResponse.class
        );
    }

    public WalletResponse addMoney(
            Long userId,
            BigDecimal amount,
            String idempotencyKey) {

        AddMoneyRequest request = new AddMoneyRequest();

        request.setUserId(userId);
        request.setAmount(amount);
        request.setIdempotencyKey(idempotencyKey);

        return restTemplate.postForObject(
                baseUrl + "/wallets/add-money",
                request,
                WalletResponse.class
        );
    }

    public WalletResponse credit(
            Long userId,
            BigDecimal amount,
            String idempotencyKey) {

        AddMoneyRequest request = new AddMoneyRequest();

        request.setUserId(userId);
        request.setAmount(amount);
        request.setIdempotencyKey(idempotencyKey);

        return restTemplate.postForObject(
                baseUrl + "/wallets/credit",
                request,
                WalletResponse.class
        );
    }
}