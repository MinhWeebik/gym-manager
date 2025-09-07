package com.ringme.cms.service.gym;

import com.ringme.cms.dto.gym.MemberSubscriptionDto;
import com.ringme.cms.dto.gym.MonthlyPaymentDto;
import com.ringme.cms.dto.gym.MonthlyPaymentResultDto;
import com.ringme.cms.dto.gym.PaymentDto;
import com.ringme.cms.model.gym.MemberSubscription;
import com.ringme.cms.model.gym.Payment;
import com.ringme.cms.repository.gym.MemberRepository;
import com.ringme.cms.repository.gym.PaymentRepository;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    public Map<String ,Object> getMemberPaymentGraphData(Long memberId)
    {
        Map<String ,Object> map = new HashMap<>();
        map.put("labels", List.of("Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6", "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"));
        List<MonthlyPaymentResultDto> dataset = new ArrayList<>();
        MonthlyPaymentResultDto dataSetData = new MonthlyPaymentResultDto();
        dataSetData.setLabel("Doanh thu");
        List<BigDecimal> paymentDataPerMonth = IntStream.range(0, 12)
                .mapToObj(i -> BigDecimal.ZERO)
                .collect(Collectors.toList());
        dataSetData.setBackgroundColor("rgba(54, 162, 235, 0.6)");
        List<MonthlyPaymentDto> monthlyData = paymentRepository.findNetTotalAmountByMonthForCurrentYear(memberId);
        if(!monthlyData.isEmpty())
        {
            for (MonthlyPaymentDto item : monthlyData) {
                String[] time = item.getPaymentMonth().split("-");
                int position = Integer.parseInt(time[1]) - 1;
                paymentDataPerMonth.set(position, item.getNetTotalAmount());
            }
        }
        dataSetData.setData(paymentDataPerMonth);
        dataset.add(dataSetData);
        map.put("datasets", dataset);
        return map;
    }

    public Page<Payment> getAll(Long id,String paymentDescription,String paymentGateway,Integer paymentStatus,Integer paymentType,Integer paymentPageNo,Integer paymentPageSize)
    {
        Pageable pageable = PageRequest.of(paymentPageNo-1, paymentPageSize);
        return paymentRepository.getAll(id, paymentDescription, paymentGateway, paymentStatus, paymentType, pageable);
    }

    public void softDelete(Long id) throws Exception{
        try {
            Payment payment = paymentRepository.findById(id).orElseThrow();
            payment.setStatus(-1);
            paymentRepository.save(payment);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
