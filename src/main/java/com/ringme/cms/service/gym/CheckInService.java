package com.ringme.cms.service.gym;

import com.ringme.cms.dto.gym.MonthlyPaymentDto;
import com.ringme.cms.dto.gym.MonthlyPaymentResultDto;
import com.ringme.cms.dto.gym.MonthlyVisitResultDto;
import com.ringme.cms.repository.gym.RawCheckInLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Log4j2
@RequiredArgsConstructor
public class CheckInService {

    private final RawCheckInLogRepository  rawCheckInLogRepository;

    public Map<String, Object> getGraphData()
    {
        Map<String ,Object> map = new HashMap<>();
        map.put("labels", List.of("Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6", "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"));
        List<MonthlyVisitResultDto> dataset = new ArrayList<>();
        MonthlyVisitResultDto dataSetData = new MonthlyVisitResultDto();
        dataSetData.setLabel("Số lần");
        List<Integer> visitData = new ArrayList<>();
        dataSetData.setBackgroundColor("rgba(54, 162, 235, 0.6)");
        for(int i = 1; i<= 12; i++)
        {
            int monthData = rawCheckInLogRepository.getDataByMonth(i);
            visitData.add(monthData);
        }
        dataSetData.setData(visitData);
        dataset.add(dataSetData);
        map.put("datasets", dataset);
        return map;
    }
}
