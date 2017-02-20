package com.tdsoft.bro.core.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tdsoft.bro.core.entity.DeviceTagEntity;

@Repository
@Transactional(propagation = Propagation.REQUIRED)
public interface DeviceTagDao extends JpaRepository<DeviceTagEntity, Long> {

}
