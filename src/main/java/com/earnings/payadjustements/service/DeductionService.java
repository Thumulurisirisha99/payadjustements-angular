package com.earnings.payadjustements.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.earnings.payadjustements.ErrorResponse;
import com.earnings.payadjustements.entity.Deduction;
import com.earnings.payadjustements.entity.DeductionRepository;
import com.earnings.payadjustements.entity.DeductionRequestDto1;
import com.earnings.payadjustements.entity.KeyEntity;

@Service
public class DeductionService {

	private JdbcTemplate jdbcTemplate;
	private DeductionRepository deductionRepository;
	private static final Logger logger = LoggerFactory.getLogger(DeductionService.class);

	public DeductionService(JdbcTemplate jdbcTemplate, DeductionRepository deductionRepository) {
		this.jdbcTemplate = jdbcTemplate;
		this.deductionRepository = deductionRepository;
	}

	public List<Map<String, Object>> getDeduction(String payPeriod, String bu, String employeeid) {
		String sql = "SELECT TRANSACTIONID, BUSINESSUNITID, EMPLOYEEID, COMPONENTID, COMPONENTVALUE "
				+ "FROM hclhrm_prod.tbl_employee_deductions "
				+ "WHERE transactionid = ? AND businessunitId = ? AND EMPLOYEEID = ? " + "AND employeestatus = 1001";
		List<Object> params = Arrays.asList(payPeriod, bu, employeeid);
		List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, params.toArray());
		if (results.isEmpty()) {
			logger.debug("No data found for the provided parameters");
			//System.out.println("No data found for the provided parameters.");
		}
		return results;
	}

	public ResponseEntity<ErrorResponse> insertDeduction(DeductionRequestDto1 obj) {
		List<Deduction> deductionToInsert = obj.getComponents().stream().map(component -> {
			Deduction deduction = new Deduction();
			KeyEntity key = new KeyEntity();
			int createdBy = obj.getCreatedBy();
			key.setBusinessUnitId(obj.getBusinessUnitId());
			key.setEmployeeId(obj.getEmployeeId());
			key.setTransactionId(obj.getTransactionId());
			key.setComponentId(component.getComponentId());
			deduction.setId(key);
			deduction.setCreatedBy(createdBy);
			LocalDateTime now = LocalDateTime.now();
			deduction.setDateCreated(Timestamp.valueOf(now));
			deduction.setDateModified(Timestamp.valueOf(now));
			deduction.setDateVerified(Timestamp.valueOf(now));
			deduction.setLuDate(Timestamp.valueOf(now));
			deduction.setStatus('C');
			deduction.setVerifiedBy(9);
			deduction.setEmployeeStatus(1001);
			deduction.setLogId(0);
			BigDecimal componentValue = new BigDecimal(component.getComponentValue());
			deduction.setComponentValue(componentValue);
			return deduction;
		}).collect(Collectors.toList());

		try {
			deductionRepository.saveAll(deductionToInsert);
			logger.debug("Deduction inserted successfully");
			return ResponseEntity.ok(new ErrorResponse("success", "Deduction inserted successfully.", "200"));
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug(e.getMessage());
			String errorMessage = "Unexpected error: " + e.getMessage();
			return ResponseEntity.status(500).body(new ErrorResponse("error", errorMessage, "500"));
		}
	}

	public ResponseEntity<ErrorResponse> MultipleInsertDeduction(List<DeductionRequestDto1> deductionRequests) {

		LocalDateTime currentDateTime = LocalDateTime.now();
		Timestamp timestamp = Timestamp.valueOf(currentDateTime);

		List<Deduction> allDeductionToInsert = deductionRequests.stream().flatMap(obj -> {
//                if (obj.getComponents() == null || obj.getComponents().isEmpty()) {
//                    throw new IllegalArgumentException("Invalid request data.");
//                }
			return obj.getComponents().stream().map(component -> {
				Deduction deduction = new Deduction();
				KeyEntity key = new KeyEntity();
				int createdBy = obj.getCreatedBy();
				key.setBusinessUnitId(obj.getBusinessUnitId());
				key.setEmployeeId(obj.getEmployeeId());
				key.setTransactionId(obj.getTransactionId());
				key.setComponentId(component.getComponentId());
				deduction.setId(key);
				deduction.setCreatedBy(createdBy);
				// obj.get
				deduction.setDateCreated(timestamp);
				deduction.setDateModified(timestamp);
				deduction.setDateVerified(timestamp);
				deduction.setLuDate(timestamp);
				deduction.setStatus('C');
				deduction.setVerifiedBy(9);
				deduction.setEmployeeStatus(1001);
				deduction.setLogId(0);
				BigDecimal componentValue = new BigDecimal(component.getComponentValue());
				deduction.setComponentValue(componentValue);
				return deduction;
			});
		}).collect(Collectors.toList());

		try {
			deductionRepository.saveAll(allDeductionToInsert);
			logger.debug("Deductions inserted successfully");
			return ResponseEntity.ok(new ErrorResponse("success", "Deductions inserted successfully.", "200"));
		} catch (Exception e) {
			e.printStackTrace();
			String errorMessage = "Unexpected error: " + e.getMessage();
			return ResponseEntity.status(500).body(new ErrorResponse("error", errorMessage, "500"));
		}
	}

	public List<Map<String, Object>> getComponentValueCountWithEmployeeCountFilter(int transactionId,
			int businessUnitId, int COMPONENTTYPEID, int EMPLOYEEID) {
		String sql = "SELECT b.componentid, a.paycomponentid, a.name, "
				+ "SUM(COALESCE(b.componentvalue, 0)) AS total_componentvalue, "
				+ "COUNT(DISTINCT CASE WHEN b.componentvalue > 0 THEN b.employeeid END) AS employee_count "
				+ "FROM hclhrm_prod.tbl_employee_deductions b " + "LEFT JOIN ( "
				+ "    SELECT PAYCOMPONENTID AS paycomponentid, name " + "    FROM hcladm_prod.tbl_pay_component "
				+ "    WHERE COMPONENTTYPEID = ? AND STATUS = 1001 " + "    AND PAYCOMPONENTID IN ( "
				+ "        SELECT PAYCOMPONENTID FROM hcladm_prod.tbl_businessunit_pay_component "
				+ "        WHERE BUSINESSUNITID = ? AND STATUS = 1001 " + "    ) " + "    AND PAYCOMPONENTID NOT IN ( "
				+ "        SELECT PAYCOMPONENTID "
				+ "        FROM hclhrm_prod_others.tbl_businessunit_pay_component_rights "
				+ "        WHERE BUSINESSUNITID = ? AND STATUS = 1002 AND EMPLOYEEID = ? " + "    ) "
				+ ") a ON a.paycomponentid = b.componentid " + "WHERE b.transactionid = ? "
				+ "AND b.businessunitid = ? " + "AND a.paycomponentid IS NOT NULL "
				+ "GROUP BY b.componentid, a.paycomponentid, a.name " + "ORDER BY b.componentid";

		return jdbcTemplate.queryForList(sql, COMPONENTTYPEID, businessUnitId, businessUnitId, EMPLOYEEID,
				transactionId, businessUnitId);
	}

}
