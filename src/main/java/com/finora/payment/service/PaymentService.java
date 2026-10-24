package com.finora.payment.service;
import com.finora.payment.domain.Payment;
import com.finora.payment.domain.enums.PaymentStatus;
import com.finora.payment.dto.request.CreatePaymentRequest;
import com.finora.payment.dto.response.PaymentResponse;
import com.finora.payment.exception.InvalidPaymentStatusException;
import com.finora.payment.exception.PaymentNotFoundException;
import com.finora.payment.mapper.PaymentMapper;
import com.finora.payment.repository.PaymentRepository;
import constants.AppConstants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    public PaymentService(
            PaymentRepository paymentRepository,
            PaymentMapper paymentMapper) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
    }



    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        Payment payment = new Payment();
        payment.setPaymentReference(
                AppConstants.PAYMENT_REFERENCE_PREFIX + UUID.randomUUID().toString().replace("-", "")
        );

        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setPaymentMethod(request.getPaymentMethod());
        Payment savedPayment = paymentRepository.save(payment);
        return paymentMapper.toResponse(savedPayment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long paymentId) {
    Payment payment= paymentRepository.findById(paymentId)
            .orElseThrow(()-> new PaymentNotFoundException("Payment not found with ID: "+paymentId));
    return mapToResponse(payment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getAllPayments(int page, int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return paymentRepository.findAll(pageable)
                .map(paymentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPaymentsByStatus(PaymentStatus paymentStatus,
                                                     int page,
                                                     int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return paymentRepository.findByPaymentStatus(paymentStatus,pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public PaymentResponse updatePaymentStatus(
            Long paymentId,
            PaymentStatus newStatus) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new PaymentNotFoundException(
                                "Payment not found with ID: " + paymentId));
        validateStatusTransition(payment.getPaymentStatus(),newStatus);
        payment.setPaymentStatus(newStatus);
        Payment updatedPayment = paymentRepository.save(payment);
        return paymentMapper.toResponse(updatedPayment);
    }

    private void validateStatusTransition(
            PaymentStatus currentStatus,
            PaymentStatus newStatus) {

        boolean isValid =
                currentStatus == PaymentStatus.CREATED
                        && newStatus == PaymentStatus.PROCESSING
                        || currentStatus == PaymentStatus.PROCESSING
                        && (newStatus == PaymentStatus.SUCCESS
                        || newStatus == PaymentStatus.FAILED);

        if (!isValid) {
            throw new InvalidPaymentStatusException(
                    "Cannot change payment status from "
                            + currentStatus + " to " + newStatus);
        }
    }

    private PaymentResponse mapToResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(payment.getPaymentId());
        response.setPaymentReference(payment.getPaymentReference());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setPaymentStatus(payment.getPaymentStatus());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());
        return response;
    }
}
