package com.hoatv.controllers;

import com.hoatv.models.EndpointSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public interface ExtEndpointSettingRepository extends JpaRepository<EndpointSetting, Long> {

    List<EndpointSetting> findEndpointConfigsByApplication(String application);
}
