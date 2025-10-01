package com.ringme.cms.service.gym;

import com.ringme.cms.dto.gym.ClassDto;
import com.ringme.cms.dto.gym.CoinDiscountTierDto;
import com.ringme.cms.model.gym.*;
import com.ringme.cms.repository.gym.CoinDiscountTierRepository;
import com.ringme.cms.repository.gym.GeneralSettingsRepository;
import com.ringme.cms.repository.gym.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class CoinService {

    private final CoinDiscountTierRepository coinDiscountTierRepository;

    private final GeneralSettingsRepository generalSettingsRepository;

    private final ModelMapper modelMapper;
    private final MemberRepository memberRepository;

    public Page<CoinDiscountTier> getPage(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        return coinDiscountTierRepository.getAll(pageable);
    }

    public void saveCoin(Integer coinPrice) throws Exception
    {
        GeneralSettings generalSettings = generalSettingsRepository.findBySettingKey("coin_price");
        generalSettings.setSettingValue(coinPrice.toString());
        generalSettingsRepository.save(generalSettings);
    }

    public void save(CoinDiscountTierDto formDto) throws Exception {
        try {
            CoinDiscountTier CDT;
            if (formDto.getId() == null) {
                CDT = modelMapper.map(formDto, CoinDiscountTier.class);
                CDT.setCreatedAt(LocalDateTime.now());
                CDT.setUpdatedAt(LocalDateTime.now());
                CDT.setStatus(1);
            } else {
                CDT = coinDiscountTierRepository.findById(formDto.getId()).orElseThrow();
                modelMapper.map(formDto, CDT);
                CDT.setUpdatedAt(LocalDateTime.now());
            }
            coinDiscountTierRepository.save(CDT);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void softDelete(Long id) throws Exception{
        CoinDiscountTier CDT = coinDiscountTierRepository.findById(id).orElseThrow(() -> new Exception("Không tìm thấy"));
        CDT.setStatus(0);
        coinDiscountTierRepository.save(CDT);
    }

    public void updateUserCoin(Long memberId, Integer amount) throws Exception
    {
        Member member = memberRepository.findById(memberId).orElseThrow();
        member.setCoin(member.getCoin() + amount);
        memberRepository.save(member);
    }
}
