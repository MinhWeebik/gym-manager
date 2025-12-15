package com.ringme.cms.service.gym;

import com.github.f4b6a3.uuid.UuidCreator;
import com.ringme.cms.common.Helper;
import com.ringme.cms.common.UploadFile;
import com.ringme.cms.dto.AjaxSearchDto;
import com.ringme.cms.dto.gym.MemberDto;
import com.ringme.cms.model.gym.Member;
import com.ringme.cms.model.gym.MemberSubscription;
import com.ringme.cms.model.gym.VerificationToken;
import com.ringme.cms.repository.gym.MemberRepository;
import com.ringme.cms.repository.gym.MemberSubscriptionRepository;
import com.ringme.cms.repository.gym.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class MemberService {

    private final MemberRepository memberRepository;

    private final MemberSubscriptionRepository memberSubscriptionRepository;

    private final ModelMapper modelMapper;

    private final UploadFile uploadFile;

    private final ImageBBService imageBBService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;

    @Value("${ipv4.address}")
    private String url;

    public Page<Member> getPage(String name, Integer status, Integer gender,String email,
                                String phoneNumber, String orderBy, String member, Integer pageNo,Integer pageSize)
    {
        List<String> orderByList = List.of("updated_at", "created_at", "last_name asc", "last_name desc");
        if(orderBy!= null && !orderByList.contains(orderBy))
        {
            throw new IllegalArgumentException("Order by value not valid");
        }
        Pageable pageable;
        if(orderBy.startsWith("last_name"))
        {
            String orderDirection = orderBy.split(" ")[1];
            if(orderDirection.equals("desc"))
            {
                pageable = PageRequest.of(pageNo-1, pageSize, Sort.by("last_name").descending());
            }
            else
            {
                pageable = PageRequest.of(pageNo-1, pageSize, Sort.by("last_name").ascending());
            }
        }
        else
        {
            pageable = PageRequest.of(pageNo-1, pageSize, Sort.by(orderBy).descending());
        }
        if(member.equals("all"))
        {
            return memberRepository.findAll(name, status, gender, email, phoneNumber, pageable);
        }
        else if(member.equals("member"))
        {
            return memberRepository.findAllWithMembership(name, status, gender, email, phoneNumber, pageable);
        }
        else if(member.equals("notMember"))
        {
            return memberRepository.findAllWithNoMembership(name, status, gender, email, phoneNumber, pageable);
        }
        return memberRepository.findAll(name, status, gender, email, phoneNumber, pageable);
    }

    public List<Member> getMembershipData(List<Member> memberList, String memberKey)
    {
        memberList.forEach(member -> {
            if(!memberKey.equals("notMember"))
            {
                List<MemberSubscription> activeMemberSub = memberSubscriptionRepository.getActiveSubscription(member.getId());
                if(activeMemberSub != null && !activeMemberSub.isEmpty())
                {
                    member.setMemberSubscriptions(List.of(activeMemberSub.get(0)));
                }
                else {
                    member.setMemberSubscriptions(null);
                }
            }
            else {
                member.setMemberSubscriptions(null);
            }
        });
        return memberList;
    }

    public Member getMembershipData(Member member)
    {
        List<MemberSubscription> activeMemberSub = memberSubscriptionRepository.getActiveSubscription(member.getId());
        member.setMemberSubscriptions(activeMemberSub);
        return member;
    }

    @Transactional
    public void save(MemberDto formDto) throws IOException {
            Member member;
            if (formDto.getId() == null) {
                Member emailCheck = memberRepository.findByEmail(formDto.getEmail(), null).orElse(null);
                Member phoneNumberCheck = memberRepository.findByPhoneNumber(formDto.getPhoneNumber(), null).orElse(null);
                if(emailCheck!=null){
                    throw new DuplicateKeyException("Email đã tồn tại");
                }
                if(phoneNumberCheck!=null){
                    throw new DuplicateKeyException("Số điện thoại đã tồn tại");
                }
                member = modelMapper.map(formDto, Member.class);
                member.setStatus(0);
                member.setCreatedAt(LocalDateTime.now());
                member.setUpdatedAt(LocalDateTime.now());
                member.setCoin(0);
                UUID newId = UuidCreator.getTimeOrderedEpoch();
                member.setUuid(newId.toString());
                member.setPassword(null);
            } else {
                Member emailCheck = memberRepository.findByEmail(formDto.getEmail(), formDto.getId()).orElse(null);
                Member phoneNumberCheck = memberRepository.findByPhoneNumber(formDto.getPhoneNumber(), formDto.getId()).orElse(null);
                if(emailCheck!=null){
                    throw new DuplicateKeyException("Email đã tồn tại");
                }
                if(phoneNumberCheck!=null){
                    throw new DuplicateKeyException("Số điện thoại đã tồn tại");
                }
                member = memberRepository.findById(formDto.getId()).orElseThrow();
                if(member.getStatus()==0 && formDto.getStatus()==1 && (member.getPassword()==null  || member.getPassword().isEmpty()))
                {
                    throw new DuplicateKeyException("Người dùng chưa cập nhật mật khẩu");
                }
                modelMapper.map(formDto, member);
                member.setUpdatedAt(LocalDateTime.now());
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            member.setDateOfBirth(LocalDate.parse(formDto.getDateOfBirthString(), formatter));

            if (formDto.getImageUpload() != null && !formDto.getImageUpload().isEmpty()) {
                String fileName = imageBBService.uploadImage(formDto.getImageUpload());
                member.setImageUrl(fileName);
            }
            Member savedMember = memberRepository.save(member);
            if(formDto.getId() == null)
            {
                String tokenString = UUID.randomUUID().toString();
                VerificationToken verificationToken = new VerificationToken();
                verificationToken.setToken(tokenString);
                verificationToken.setExpiryDate(LocalDateTime.now().plusDays(1));
                verificationToken.setMember(savedMember);
                verificationTokenRepository.save(verificationToken);
                String link = url + "/nexia-cms/setup-password?token=" + tokenString;
                String subject = "Kích hoạt tài khoản hội viên Nexia";

                String content = String.format("""
            Chào %s,
            
            Chào mừng bạn gia nhập gym Nexia!
            
            Tài khoản của bạn đã được khởi tạo. Vui lòng nhấn vào đường dẫn bên dưới để thiết lập mật khẩu và bắt đầu sử dụng ứng dụng:
            
            %s
            
            Lưu ý: Đường dẫn này chỉ có hiệu lực trong 24 giờ.
            
            Trân trọng,
            Đội ngũ Nexia
            """, savedMember.getFirstName() + " " + savedMember.getLastName(), link);
                emailService.sendEmail(savedMember.getEmail(),subject, content);
            }
    }

    public Page<Member> search(String name, String email, String phoneNumber, Integer gender, List<Long> exceptIds, Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        boolean isEmpty = false;
        if (exceptIds == null || exceptIds.isEmpty()) {
            isEmpty = true;
        }
        return memberRepository.search(name,gender,email,phoneNumber,isEmpty,exceptIds,pageable);
    }

    public List<AjaxSearchDto> ajaxSearchMember(String input, Long trainerId) {
        return Helper.listAjaxMember(memberRepository.ajaxSearchMember(Helper.processStringSearch(input), trainerId));
    }

    @Transactional
    public void resendToken(Long id, String type)
    {
        Member member = memberRepository.findById(id).orElseThrow();
        verificationTokenRepository.removeOldToken(id);
        String tokenString = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(tokenString);
        verificationToken.setExpiryDate(LocalDateTime.now().plusDays(1));
        verificationToken.setMember(member);
        verificationTokenRepository.save(verificationToken);
        String link = url + "/nexia-cms/setup-password?token=" + tokenString;
        String subject = type.equals("resend") ? "Kích hoạt tài khoản hội viên Nexia" : "[Nexia gym] - Yêu cầu đặt lại mật khẩu";

        String content = type.equals("resend") ? String.format("""
            Chào %s,
            
            Chào mừng bạn gia nhập gym Nexia!
            
            Tài khoản của bạn đã được khởi tạo. Vui lòng nhấn vào đường dẫn bên dưới để thiết lập mật khẩu và bắt đầu sử dụng ứng dụng:
            
            %s
            
            Lưu ý: Đường dẫn này chỉ có hiệu lực trong 24 giờ.
            
            Trân trọng,
            Đội ngũ Nexia
            """, member.getFirstName() + " " + member.getLastName(), link) : String.format("""
            Chào %s,
            
            Chúng tôi vừa nhận được yêu cầu đặt lại mật khẩu của bạn.
            
            Vui lòng nhấn vào đường dẫn bên dưới để tạo mật khẩu mới:
            %s
            
            Đường dẫn này sẽ hết hạn sau 30 phút.
            
            Nếu bạn không yêu cầu thay đổi, vui lòng bỏ qua email này.
            
            Trân trọng,
            Đội ngũ Nexia
            """, member.getFirstName() + " " + member.getLastName(), link);
        emailService.sendEmail(member.getEmail(),subject, content);
    }

}
