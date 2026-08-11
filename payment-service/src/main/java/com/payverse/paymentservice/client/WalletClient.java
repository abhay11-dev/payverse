package com.payverse.paymentservice.client;

import com.payverse.paymentservice.dto.AddMoneyRequest;
import com.payverse.paymentservice.dto.WalletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Component
public class WalletClient {

    private final RestClient restClient;

    public WalletClient() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:8081")
                .build();
    }

    public WalletResponse debit(
            Long userId,
            BigDecimal amount,
            String idempotencyKey) {

        AddMoneyRequest request = new AddMoneyRequest();

        request.setUserId(userId);
        request.setAmount(amount);
        request.setIdempotencyKey(idempotencyKey);

        return restClient.post()
                .uri("/wallets/debit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(WalletResponse.class);
    }

    public WalletResponse addMoney(
            Long userId,
            BigDecimal amount,
            String idempotencyKey) {

        AddMoneyRequest request = new AddMoneyRequest();

        request.setUserId(userId);
        request.setAmount(amount);
        request.setIdempotencyKey(idempotencyKey);

        return restClient.post()
                .uri("/wallets/add-money")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(WalletResponse.class);
    }

    public WalletResponse credit(
            Long userId,
            BigDecimal amount,
            String idempotencyKey) {

        AddMoneyRequest request = new AddMoneyRequest();

        request.setUserId(userId);
        request.setAmount(amount);
        request.setIdempotencyKey(idempotencyKey);

        return restClient.post()
                .uri("/wallets/credit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(WalletResponse.class);
    }
}