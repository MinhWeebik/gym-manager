package com.ringme.cms.service.gym;

import com.ringme.cms.common.Helper;
import com.ringme.cms.dto.AjaxSearchDto;
import com.ringme.cms.dto.gym.MembershipDto;
import com.ringme.cms.model.gym.MemberSubscription;
import com.ringme.cms.model.gym.Membership;
import com.ringme.cms.repository.gym.MemberSubscriptionRepository;
import com.ringme.cms.repository.gym.MembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Log4j2
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final MemberSubscriptionRepository memberSubscriptionRepository;
    private final ModelMapper modelMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    public List<AjaxSearchDto> ajaxSearchMembership(String input, Long id) {
        List<MemberSubscription> memberSubscriptions = memberSubscriptionRepository.findByMemberIdAndType(id, 0);
        Integer type = null;
        for (MemberSubscription memberSubscription : memberSubscriptions) {
            if(memberSubscription.getIsRecurring() == 1){
                type = 1;
            }
        }
        return Helper.listAjax(membershipRepository.ajaxSearchMembership(Helper.processStringSearch(input), type));
    }

    public Page<Membership> getPage(String name,Integer status, Integer type, Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        return membershipRepository.getAll(name, status, type, pageable);
    }

    public void save(MembershipDto formDto) throws Exception {
        Membership membership = new Membership();
        if (formDto.getId() == null) {
            membership = modelMapper.map(formDto, Membership.class);
            membership.setCreatedAt(LocalDateTime.now());
            membership.setUpdatedAt(LocalDateTime.now());
            membership.setStatus(1);
            if(membership.getTotalVisit() != null && membership.getTotalVisit() == 0) membership.setTotalVisit(null);
            if(formDto.getType() == 0)
            {
                String url = "http://localhost:8086/nexia-cms/paypal/create-plan";

                RestTemplate restTemplate = new RestTemplate();

                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                        .queryParam("planName", formDto.getName())
                        .queryParam("price", formDto.getPrice())
                        .queryParam("duration", formDto.getDuration());

                ResponseEntity<Map<String, Object>> response =
                        restTemplate.exchange(
                                builder.toUriString(), // Use the built URL
                                HttpMethod.POST,
                                null,
                                new ParameterizedTypeReference<Map<String, Object>>() {});
                if(response.getStatusCode() == HttpStatus.OK && response.getBody() != null)
                {
                    Map<String,Object> result = response.getBody();
                    String planId = (String) result.get("planId");
                    membership.setPaypalPlanId(planId);
                }
                else throw new Exception("Không thể tạo gói mới trên Paypal");
            }
        } else {
            boolean isPriceChanged = false;
            membership = membershipRepository.findById(formDto.getId()).orElseThrow();
            if(!Objects.equals(membership.getPrice(), formDto.getPrice())) isPriceChanged = true;
            modelMapper.map(formDto, membership);
            membership.setUpdatedAt(LocalDateTime.now());
            if(membership.getTotalVisit() != null && membership.getTotalVisit() == 0 && membership.getPaypalPlanId() != null && !membership.getPaypalPlanId().isEmpty()) membership.setTotalVisit(null);
            if(membership.getType() == 0 && isPriceChanged)
            {
                String url = "http://localhost:8086/nexia-cms/paypal/update-plan-price";

                RestTemplate restTemplate = new RestTemplate();

                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                        .queryParam("planId", membership.getPaypalPlanId())
                        .queryParam("price", formDto.getPrice());

                ResponseEntity<Map<String, Object>> response =
                        restTemplate.exchange(
                                builder.toUriString(),
                                HttpMethod.POST,
                                null,
                                new ParameterizedTypeReference<Map<String, Object>>() {});
                if(response.getStatusCode() != HttpStatus.OK)
                {
                    throw new Exception("Không thể cập nhật giá gói trên Paypal");
                }
            }
        }
        membershipRepository.save(membership);
    }

    public void softDelete(Long id) throws Exception{
        Membership membership = membershipRepository.findById(id).orElseThrow(() -> new Exception("Không tìm thấy thành viên"));
        List<MemberSubscription> memberSubscriptions = memberSubscriptionRepository.findByMembershipId(membership.getId());
        if(!memberSubscriptions.isEmpty())
        {
            throw new Exception("Đang có thành viên sử dụng gói này");
        }
        if(membership.getType() == 0 && membership.getPaypalPlanId() != null && !membership.getPaypalPlanId().isEmpty())
        {
            String url = "http://localhost:8086/nexia-cms/paypal/deactivate-plan";

            RestTemplate restTemplate = new RestTemplate();

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("planId", membership.getPaypalPlanId());

            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            builder.toUriString(),
                            HttpMethod.POST,
                            null,
                            new ParameterizedTypeReference<Map<String, Object>>() {});
            if(response.getStatusCode() != HttpStatus.OK)
            {
                throw new Exception("Không thể xóa gói trên Paypal");
            }
        }
        membership.setStatus(0);
        membershipRepository.save(membership);
    }
}
