package com.hoatv.controllers;

import com.hoatv.models.EndpointExecutionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface ExtExecutionResultRepository extends JpaRepository<EndpointExecutionResult, Long> {
}
