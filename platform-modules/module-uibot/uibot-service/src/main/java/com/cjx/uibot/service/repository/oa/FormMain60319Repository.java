package com.cjx.uibot.service.repository.oa;

import com.cjx.uibot.service.entity.oa.FormMain60319;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FormMain60319Repository extends JpaRepository<FormMain60319, Long> {

    @Query("SELECT f FROM FormMain60319 f WHERE f.id = :id")
    Optional<FormMain60319> findByIdEager(@Param("id") long id);
}