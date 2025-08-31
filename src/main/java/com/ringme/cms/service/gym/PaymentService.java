package com.ringme.cms.service.gym;

import com.ringme.cms.dto.gym.MemberSubscriptionDto;
import com.ringme.cms.dto.gym.PaymentDto;
import com.ringme.cms.model.gym.MemberSubscription;
import com.ringme.cms.model.gym.Payment;
import com.ringme.cms.repository.gym.MemberRepository;
import com.ringme.cms.repository.gym.PaymentRepository;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Log4j2
public class PaymentService {

    private final ModelMapper modelMapper;

    private final PaymentRepository paymentRepository;

    private final MemberRepository memberRepository;

    public Payment save(PaymentDto formDto) throws Exception {
        try {
            Payment payment;
            payment = modelMapper.map(formDto, Payment.class);
            payment.setCreatedAt(LocalDateTime.now());
            payment.setUpdatedAt(LocalDateTime.now());
            payment.setTransactionDate(LocalDateTime.now());
            if(formDto.getPaymentGateway().equals("cash"))
            {
                payment.setStatus(1);
            }
            else {
                payment.setStatus(2);
                payment.setGatewayTransactionId(formDto.getGatewayTransactionId());
                payment.setPaymentUrl(formDto.getPaymentUrl());
            }
            payment.setMember(memberRepository.findById(formDto.getMemberId()).orElseThrow());

            return paymentRepository.save(payment);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public Integer getPaymentStatus(String paymentId) throws Exception
    {
        Payment payment = paymentRepository.findByGatewayTransactionId(paymentId);
        if(payment == null)
        {
            throw new NotFoundException("No payment found");
        }
        return payment.getStatus();
    }
}
