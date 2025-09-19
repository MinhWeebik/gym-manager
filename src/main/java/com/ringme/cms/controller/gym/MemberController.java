package com.ringme.cms.controller.gym;

import com.ringme.cms.dto.AjaxSearchDto;
import com.ringme.cms.dto.gym.MemberDto;
import com.ringme.cms.dto.gym.MemberSubscriptionDto;
import com.ringme.cms.model.gym.Member;
import com.ringme.cms.model.gym.MemberSubscription;
import com.ringme.cms.model.gym.Payment;
import com.ringme.cms.repository.gym.MemberRepository;
import com.ringme.cms.repository.gym.MemberSubscriptionRepository;
import com.ringme.cms.repository.gym.PaymentRepository;
import com.ringme.cms.service.gym.MemberService;
import com.ringme.cms.service.gym.PaymentService;
import com.ringme.cms.utils.AppUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
@Log4j2
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController
{

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;
    private final MemberSubscriptionRepository memberSubscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    @RequestMapping(value = {"/index"})
    public String index(@RequestParam(name = "pageNo", required = false, defaultValue = "1") Integer pageNo,
                        @RequestParam(name = "pageSize", required = false, defaultValue = "15") Integer pageSize,
                        @RequestParam(name = "name", required = false) String name,
                        @RequestParam(name = "status", required = false) Integer status,
                        @RequestParam(name = "gender", required = false) Integer gender,
                        @RequestParam(name = "email", required = false) String email,
                        @RequestParam(name = "phoneNumber", required = false) String phoneNumber,
                        @RequestParam(name = "orderBy",  required = false) String orderBy,
                        //updated_at, created_at, last_name asc(desc)
                        @RequestParam(name = "member", required = false) String member,
                        //all, member, notMember
                        ModelMap model) {
        if(pageNo == null || pageNo <= 0) pageNo = 1;
        if(pageSize == null || pageSize <= 0) pageSize = 15;
        if(orderBy == null || orderBy.isEmpty()) orderBy = "created_at";
        if(member == null || member.isEmpty()) member = "all";
        Page<Member> pageObject = memberService.getPage(name, status,gender, email, phoneNumber, orderBy, member, pageNo, pageSize);
        List<Member> members = memberService.getMembershipData(pageObject.getContent(), member);
        model.put("title", "Thành viên");
        model.put("pageNo", pageNo);
        model.put("pageSize", pageSize);
        model.put("totalPage", pageObject.getTotalPages());
        model.put("models", members);
        model.put("name", name);
        model.put("status", status);
        model.put("gender", gender);
        model.put("email", email);
        model.put("phoneNumber", phoneNumber);
        model.put("orderBy", orderBy);
        model.put("member", member);
        return "gym/member/index";
    }

    @GetMapping("/create")
    public String create(ModelMap model) {
        try {
            MemberDto formDto = (MemberDto) model.getOrDefault("model", new MemberDto());
            if(formDto.getGender() == null)
            {
                formDto.setGender(1);
            }
            model.putIfAbsent("model", formDto);
            return "gym/member/create";
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return "404";
        }
    }

    @GetMapping("/detail")
    public String detail(@RequestParam("id") Long id,
                         @RequestParam(value = "tab", required = false) Integer tab,
                         @RequestParam(value = "paymentPageNo", required = false, defaultValue = "1") Integer paymentPageNo,
                         @RequestParam(value = "paymentPageSize", required = false, defaultValue = "10") Integer paymentPageSize,
                         @RequestParam(value = "paymentDescription", required = false) String paymentDescription,
                         @RequestParam(value = "paymentGatewaySearch", required = false) String paymentGateway,
                         @RequestParam(value = "paymentStatus", required = false) Integer paymentStatus,
                         @RequestParam(value = "paymentType", required = false) Integer paymentType, ModelMap model) throws Exception {
        log.info("id: {}", id);
        try {
            if(tab == null || tab <= 0) tab = 1;
            MemberDto formDto = (MemberDto) model.getOrDefault("model", new MemberDto());
            Member member = memberRepository.findById(id).orElseThrow();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            if (!model.containsAttribute("model")) {
                formDto = modelMapper.map(member, MemberDto.class);
                List<MemberSubscription> allSubscription = memberSubscriptionRepository.getAllSubscription(member.getId());
                for (MemberSubscription memberSubscription : allSubscription) {
                    if(memberSubscription.getStatus() == 1 || memberSubscription.getStatus() == 2 || memberSubscription.getStatus() == 3)
                    {
                        if(memberSubscription.getMembership().getType() == 0)
                        {
                            if(formDto.getNormalSubscription() == null)
                                formDto.setNormalSubscription(new ArrayList<>());
                            formDto.getNormalSubscription().add(memberSubscription);
                        }
                        if(memberSubscription.getMembership().getType() == 1)
                        {
                            if(formDto.getAddonSubscription() == null)
                                formDto.setAddonSubscription(new ArrayList<>());
                            formDto.getAddonSubscription().add(memberSubscription);
                        }
                    }
                    else {
                        if(formDto.getOldSubscription() == null)
                            formDto.setOldSubscription(new ArrayList<>());
                        formDto.getOldSubscription().add(memberSubscription);
                    }
                }
                if(formDto.getNormalSubscription() != null && !formDto.getNormalSubscription().isEmpty())
                {
                    formDto.getNormalSubscription()
                            .sort(Comparator.comparing(MemberSubscription::getStartAt));
                    LocalDate startDate = formDto.getNormalSubscription().get(0).getStartAt();
                    LocalDate endDate  = formDto.getNormalSubscription().get(0).getEndAt();
                    for(MemberSubscription memberSubscription : formDto.getNormalSubscription())
                    {
                        endDate = memberSubscription.getEndAt();
                    }
                    String startEndString = String.format(startDate.format(formatter) + " - " + endDate.format(formatter));
                    model.put("startEndString", startEndString);
                }
                if(formDto.getAddonSubscription() != null && !formDto.getAddonSubscription().isEmpty())
                {
                    formDto.getAddonSubscription()
                            .sort(Comparator.comparing(MemberSubscription::getStartAt));
                }
                if(formDto.getOldSubscription() != null && !formDto.getOldSubscription().isEmpty())
                {
                    formDto.getOldSubscription()
                            .sort(Comparator.comparing(MemberSubscription::getUpdatedAt).reversed());
                }
                formDto.setDateOfBirthString(member.getDateOfBirth().format(formatter));
            }
            if(paymentPageNo == null || paymentPageNo <= 0) paymentPageNo = 1;
            if(paymentPageSize == null || paymentPageSize <= 0) paymentPageSize = 10;
            Page<Payment> paymentObject = paymentService.getAll(id, paymentDescription, paymentGateway, paymentStatus, paymentType, paymentPageNo, paymentPageSize);
            model.put("image", member.getImageUrl());
            model.put("model", formDto);
            model.put("tab", tab);
            model.put("payment", paymentObject.getContent());
            model.put("paymentPageNo", paymentPageNo);
            model.put("paymentPageSize", paymentPageSize);
            model.put("paymentDescription", paymentDescription);
            model.put("paymentGatewaySearch", paymentGateway);
            model.put("paymentStatus", paymentStatus);
            model.put("paymentType", paymentType);
            model.put("paymentTotalPage", paymentObject.getTotalPages());
            return "gym/member/detail";
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            throw new Exception(e);
        }
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("model") MemberDto formDto,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes,
                       HttpServletRequest request) {
        try {
            if (bindingResult.hasErrors()) {
                return AppUtils.goBackWithError(request, redirectAttributes, "model", bindingResult, formDto, null)
                        .orElse("redirect:/member/detail?id=" + formDto.getId());
            }
            memberService.save(formDto);
            if (formDto.getId() == null) {
                redirectAttributes.addFlashAttribute("success", "Tạo thành công!");
                return "redirect:/member/index";
            } else {
                redirectAttributes.addFlashAttribute("success", "Cập nhật thành công!");
            }
            return AppUtils.goBack(request).orElse("redirect:/member/detail?id=" + formDto.getId());
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            return AppUtils.goBackWithError(request, redirectAttributes, "model", bindingResult, formDto, "Error in server!")
                    .orElse("redirect:/member/detail?id=" + formDto.getId());
        }
    }

    @GetMapping(value = {"/render-search"})
    public String search(@RequestParam(name = "pageNo", required = false, defaultValue = "1") Integer pageNo,
                         @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                         @RequestParam(name = "name", required = false) String name,
                         @RequestParam(name = "email", required = false) String email,
                         @RequestParam(name = "phoneNumber", required = false) String phoneNumber,
                         @RequestParam(name = "gender", required = false) Integer gender,
                         @RequestParam(name = "exceptIds", required = false) List<Long> exceptIds,
                         @RequestParam(name = "signUpAmount",  required = false) Integer signUpAmount,
                         ModelMap model) throws Exception {

        if (pageNo == null || pageNo <= 0) pageNo = 1;
        if (pageSize == null || pageSize <= 0) pageSize = 10;

        try {
            Page<Member> models = memberService.search(name, email, phoneNumber, gender,exceptIds, pageNo, pageSize);
            model.put("pageNo", pageNo);
            model.put("pageSize", pageSize);
            model.put("totalPage", models.getTotalPages());
            model.put("models", models.toList());
            model.put("signUpAmount",signUpAmount);
            return "gym/attendance/modal_search :: content-search-song";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/ajax-search/{trainerId}")
    @ResponseBody
    public ResponseEntity<List<AjaxSearchDto>> memberAjaxSearch(
            @RequestParam(name = "input", required = false) String input,
            @PathVariable(name = "trainerId") Long trainerId) {
        try
        {
            return new ResponseEntity<>(memberService.ajaxSearchMember(input, trainerId), HttpStatus.OK);
        }
        catch (Exception e)
        {
            log.error("Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
