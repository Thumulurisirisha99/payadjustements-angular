package com.earnings.payadjustements.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<DummyEntity, Long> {

	@Query(value = "SELECT businessunitid FROM hcladm_prod.tbl_businessunit  where name= :name", nativeQuery = true)
	Integer findBusinessUnitIdByName(String name);

	@Query(value = "SELECT paycomponentid FROM hcladm_prod.tbl_pay_component WHERE componenttypeid = :componenttypeid AND status = 1001 AND name = :name", nativeQuery = true)
	Integer getPayComponentId(int componenttypeid, String name);

	@Query(value = " select employeeid from hclhrm_prod.tbl_employee_primary where employeesequenceno= :empNo", nativeQuery = true)
	Integer getEmployeeId(int empNo);

	@Query(value = "SELECT paycomponentid FROM hcladm_prod.tbl_pay_component WHERE componenttypeid = :componenttypeid AND status = 1001 AND name = :name", nativeQuery = true)
	Integer getPaycomponentId(@Param("componenttypeid") int componenttypeid, @Param("name") String name);

	@Query(value = " select employeesequenceno from hclhrm_prod.tbl_employee_primary where employeeid= :empNo", nativeQuery = true)
	Integer getEmployeeSeq(int empNo);

}
