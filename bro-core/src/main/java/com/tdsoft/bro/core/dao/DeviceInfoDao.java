package com.tdsoft.bro.core.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tdsoft.bro.core.entity.DeviceInfoEntity;

@Repository
@Transactional(propagation = Propagation.REQUIRED)
public interface DeviceInfoDao extends JpaRepository<DeviceInfoEntity, Long> {
	@Modifying
    @Query("UPDATE DeviceInfoEntity  SET snsToken = :snsToken WHERE deviceNo = :deviceNo")
    void updateSnsToken(@Param("deviceNo") long companyId, @Param("snsToken") String token);
	
	@Query("SELECT e.deviceNo from DeviceInfoEntity e where e.deviceId = :deviceId")
	Long getDeviceNoByDeviceId(@Param("deviceId")String deviceId);
	
	@Query("select e.deviceId from DeviceInfoEntity e")
	Page<String> findAllPaged(Pageable pageable);
}
