package com.earnings.payadjustements.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EarningRepository extends JpaRepository<Earning, KeyEntity> {
	@Query(value = "SELECT employeeId FROM hclhrm_prod.tbl_employee_primary WHERE employeeSequenceNo = ?1", nativeQuery = true)
	Integer findEmployeeIdBySequenceNo(String employeeSequenceNo);


}
