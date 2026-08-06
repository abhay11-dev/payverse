package com.payverse.walletservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<?> handleWalletNotFound(
            WalletNotFoundException ex
    ) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                    Map.of(
                        "status", 404,
                        "error", "Wallet Not Found",
                        "message", ex.getMessage()
                    )
                );
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
public ResponseEntity<?> handleOptimisticLockException(
        ObjectOptimisticLockingFailureException ex
) {

    return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(
                    Map.of(
                            "status", 409,
                            "error", "Conflict",
                            "message", "Wallet was updated by another transaction. Please retry."
                    )
            );
}
}