package com.ringme.cms.repository.gym;

import com.ringme.cms.model.gym.GeneralSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GeneralSettingsRepository extends JpaRepository<GeneralSettings, Long> {
    GeneralSettings findBySettingKey(String settingKey);
}
