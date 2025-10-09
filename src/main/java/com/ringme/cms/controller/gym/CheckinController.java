package com.ringme.cms.controller.gym;

import com.ringme.cms.config.RabbitConfig;
import com.ringme.cms.dto.gym.CheckinDto;
import com.ringme.cms.dto.gym.PaymentDto;
import com.ringme.cms.model.gym.Member;
import com.ringme.cms.model.gym.MemberSubscription;
import com.ringme.cms.model.gym.RawCheckInLog;
import com.ringme.cms.repository.gym.MemberRepository;
import com.ringme.cms.repository.gym.MemberSubscriptionRepository;
import com.ringme.cms.repository.gym.RawCheckInLogRepository;
import com.ringme.cms.service.gym.CheckInService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.naming.directory.InvalidAttributesException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Log4j2
@RequestMapping("/check-in")
@RequiredArgsConstructor
public class CheckinController {

    private final RabbitTemplate rabbitTemplate;

    private final MemberSubscriptionRepository memberSubscriptionRepository;
    private final MemberRepository memberRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final RawCheckInLogRepository rawCheckInLogRepository;
    private final CheckInService checkInService;

    @Value("${queue.checkin}")
    private String queueCheckin;

    @Value("${domain.nginx}")
    private String imageDomain;

    @Value("${domain.app}")
    private String webDomain;

    @RequestMapping(value = "/index")
    private String index() {
        return "gym/qr-scanner/index";
    }

    @PostMapping(value = "/manual")
    private ResponseEntity<Map<String, Object>> manual(@RequestParam("id") Long id) {
        try
        {
            Map<String, Object> map = new HashMap<>();
            List<MemberSubscription> memberSubscriptions = memberSubscriptionRepository.findActiveByMemberIdAndType(id, 0);
            if(!memberSubscriptions.isEmpty())
            {
                Member member = memberRepository.findById(id).orElseThrow();
                CheckinDto  checkinDto = new CheckinDto();
                checkinDto.setFirstName(member.getFirstName());
                checkinDto.setLastName(member.getLastName());
                if(member.getLastCheckedIn()!=null){
                    checkinDto.setLastCheckedIn(member.getLastCheckedIn().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                }
                else {
                    checkinDto.setLastCheckedIn(null);
                }
                String imageUrl;
                if(member.getImageUrl() == null || member.getImageUrl().isEmpty())
                {
                    if(member.getGender() == 1)
                    {
                        imageUrl = webDomain + "/nexia-cms/img/no_pfp_male.jpg";
                    }
                    else {
                        imageUrl = webDomain + "/nexia-cms/img/no_pfp_female.jpg";
                    }
                }
                else {
                    imageUrl = imageDomain + member.getImageUrl().replace("\\", "/");
                }
                checkinDto.setImageUrl(imageUrl);
                checkinDto.setMembershipName(memberSubscriptions.get(0).getMembership().getName());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                checkinDto.setStartAt(memberSubscriptions.get(0).getStartAt().format(formatter));
                checkinDto.setEndAt(memberSubscriptions.get(0).getEndAt().format(formatter));

                map.put("code", 200);
                map.put("data", checkinDto);
                map.put("status", "success");
                rabbitTemplate.convertAndSend(queueCheckin, id);
                return ResponseEntity.ok(map);
            }
            else {
                map.put("code", 403);
                map.put("status", "forbidden");
                return ResponseEntity.status(403).build();
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(value = "/auto")
    private ResponseEntity<Map<String, Object>> auto(@RequestParam("uuid") String uuid) {
        try
        {
            String UUID_REGEX =
                    "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
            if(uuid == null || !uuid.matches(UUID_REGEX))
            {
                throw new InvalidAttributesException("invalid uuid");
            }
            Map<String, Object> map = new HashMap<>();
            List<MemberSubscription> memberSubscriptions = memberSubscriptionRepository.findActiveByMemberUUIDAndType(uuid, 0);
            if(!memberSubscriptions.isEmpty())
            {
                Member member = memberRepository.findByUuid(uuid).orElseThrow();
                CheckinDto checkinDto = new CheckinDto();
                checkinDto.setFirstName(member.getFirstName());
                checkinDto.setLastName(member.getLastName());
                if(member.getLastCheckedIn()!=null){
                    checkinDto.setLastCheckedIn(member.getLastCheckedIn().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                }
                else {
                    checkinDto.setLastCheckedIn(null);
                }
                String imageUrl;
                if(member.getImageUrl() == null || member.getImageUrl().isEmpty())
                {
                    if(member.getGender() == 1)
                    {
                        imageUrl = webDomain + "/nexia-cms/img/no_pfp_male.jpg";
                    }
                    else {
                        imageUrl = webDomain + "/nexia-cms/img/no_pfp_female.jpg";
                    }
                }
                else {
                    imageUrl = imageDomain + member.getImageUrl().replace("\\", "/");
                }
                checkinDto.setImageUrl(imageUrl);
                checkinDto.setMembershipName(memberSubscriptions.get(0).getMembership().getName());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                checkinDto.setStartAt(memberSubscriptions.get(0).getStartAt().format(formatter));
                checkinDto.setEndAt(memberSubscriptions.get(0).getEndAt().format(formatter));

                map.put("code", 200);
                map.put("data", checkinDto);
                map.put("status", "success");
                rabbitTemplate.convertAndSend(queueCheckin, member.getId());
                messagingTemplate.convertAndSend("/topic/check-ins", map);
                return ResponseEntity.ok(map);
            }
            else {
                map.put("code", 403);
                map.put("data", null);
                map.put("status", "forbidden");
                messagingTemplate.convertAndSend("/topic/check-ins", map);
                return ResponseEntity.status(403).body(map);
            }
        }
        catch (InvalidAttributesException e)
        {
            Map<String, Object> map = new HashMap<>();
            map.put("code", 400);
            map.put("data", null);
            map.put("status", "bad request");
            log.error(e.getMessage());
            messagingTemplate.convertAndSend("/topic/check-ins", map);
            return ResponseEntity.badRequest().body(map);
        }
        catch (Exception e)
        {
            Map<String, Object> map = new HashMap<>();
            map.put("code", 500);
            map.put("data", null);
            map.put("status", "internal server error");
            log.error(e.getMessage());
            messagingTemplate.convertAndSend("/topic/check-ins", map);
            return ResponseEntity.internalServerError().body(map);
        }
    }

    @GetMapping("/history")
    public String update(@RequestParam(value = "id") Long memberId, ModelMap model) {
        log.info("member id: {}", memberId);
        try {
            List<RawCheckInLog> logHistory = rawCheckInLogRepository.getLogByMemberId(memberId);
            model.put("model", logHistory);
            return "gym/fragment/member :: checkinDetail";
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return "404";
        }
    }

    @GetMapping("/graph-data")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> graphData()
    {
        try
        {
            Map<String, Object> data = checkInService.getGraphData();
            return ResponseEntity.ok(data);
        }
        catch (Exception e)
        {
            log.error("Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
