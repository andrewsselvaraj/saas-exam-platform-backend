package com.saasexam.schoolclass;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SchoolClassRepository extends JpaRepository<SchoolClass, String> {

    List<SchoolClass> findByOrgIdAndStatusNotOrderByName(String orgId, String status);
}
