package com.earnings.payadjustements.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DeductionRepository extends JpaRepository<Deduction, KeyEntity>{

	@Query(value = "SELECT employeeId FROM hclhrm_prod.tbl_employee_primary WHERE employeeSequenceNo = ?1", nativeQuery = true)
	int findEmployeeIdBySequenceNo(String employeeSequenceNo);

	@Query(value = "SELECT COUNT(*) FROM hclhrm_prod.tbl_employee_primary a "
			+ "LEFT JOIN hclhrm_prod.tbl_employee_payperiod_details g ON a.employeesequenceno = g.employeesequenceno "
			+ "LEFT JOIN hcladm_prod.tbl_businessunit f ON a.companyid = f.businessunitid "
			+ "WHERE g.payperiod = ?1 AND f.businessunitid = ?2 AND a.employeeId = ?3 AND f.status = 1001", nativeQuery = true)
	Long countByEmployeeSequenceNoAndBusinessUnitIdAndTransactionId(int payPeriod, int bu, Integer employeeId);
	  
}