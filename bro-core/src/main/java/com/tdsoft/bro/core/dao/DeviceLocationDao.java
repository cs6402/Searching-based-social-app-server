package com.tdsoft.bro.core.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tdsoft.bro.core.entity.DeviceLocationEntity;

@Repository
public interface DeviceLocationDao extends JpaRepository<DeviceLocationEntity, Long> {
	@Deprecated
	@Modifying
	@Query("update DeviceLocationEntity set LastReportTime = CURRENT_DATE where DeviceNo = :DeviceNo")
	int updateLastReportTime(@Param("DeviceNo") long deviceNo);
}
