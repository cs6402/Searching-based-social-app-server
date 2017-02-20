package com.tdsoft.bro.core.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tdsoft.bro.core.entity.BroMessageEntity;

@Repository
public interface MessageDao extends JpaRepository<BroMessageEntity, Long> {
}
