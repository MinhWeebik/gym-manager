package com.ringme.cms.repository.gym;

import com.ringme.cms.model.gym.AppointmentInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentInstanceRepository extends JpaRepository<AppointmentInstance, Long> {
}
