package com.finora.payment.dto.request;

import com.finora.payment.domain.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public class UpdatePaymentStatusRequest {

    @NotNull(message = "Payment status is required")
    private PaymentStatus paymentStatus;

    public UpdatePaymentStatusRequest() {
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}
