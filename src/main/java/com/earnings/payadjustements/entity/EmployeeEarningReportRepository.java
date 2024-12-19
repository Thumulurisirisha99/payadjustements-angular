package com.earnings.payadjustements.entity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EmployeeEarningReportRepository extends JpaRepository<EmployeeEarningReport, IdEntity> {
	// You can define custom queries here if needed
	 @Query(value = "SELECT * FROM (SELECT * FROM tbl_employee_earningreport ORDER BY id DESC LIMIT 10) AS last_records ORDER BY id ASC", nativeQuery = true)
	    List<EmployeeEarningReport> findLast10Records();


}
