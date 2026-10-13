package com.saasexam.organization;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrganizationRepository extends JpaRepository<Organization, String> {

    List<Organization> findByStatusNotOrderByName(String status);
}
