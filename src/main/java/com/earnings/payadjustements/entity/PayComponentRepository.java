package com.earnings.payadjustements.entity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PayComponentRepository extends JpaRepository<PayComponent, Long> {

	@Query(value = "SELECT  c.name, c.paycomponentid FROM hcladm_prod.tbl_pay_component c WHERE c.name IN (:componentNames) and c.componenttypeid=:componenttypeid and c.status=1001", nativeQuery = true)
	List<Object[]> findPayComponentByComponentNames(List<String> componentNames, int componenttypeid);



}
