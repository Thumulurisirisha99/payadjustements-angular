package com.earnings.payadjustements.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.earnings.payadjustements.ErrorResponse;
import com.earnings.payadjustements.Exception.NoDataFoundException;
import com.earnings.payadjustements.entity.Component;
import com.earnings.payadjustements.entity.Earning;
import com.earnings.payadjustements.entity.EarningRepository;
import com.earnings.payadjustements.entity.EarningsRequestDto1;
import com.earnings.payadjustements.entity.EmployeeEarningReport;
import com.earnings.payadjustements.entity.EmployeeEarningReportRepository;
import com.earnings.payadjustements.entity.KeyEntity;

@Service
public class EarningService {
	private JdbcTemplate jdbcTemplate;
	private EarningRepository earningRepository;
	private EmployeeEarningReportRepository employeeEarningReportRepository;

	public EarningService(JdbcTemplate jdbcTemplate, EarningRepository earningRepository,
			EmployeeEarningReportRepository employeeEarningReportRepository) {
		this.jdbcTemplate = jdbcTemplate;
		this.earningRepository = earningRepository;
		this.employeeEarningReportRepository = employeeEarningReportRepository;
	}

	private static final Logger logger = LoggerFactory.getLogger(EarningService.class);

	public Page<Map<String, Object>> getData(String payPeriod, String bu, String department, String employeeName,
			String status, String employeeId, Pageable pageable) {
		if (payPeriod == null || payPeriod.isEmpty() || bu == null || bu.isEmpty()) {
			logger.debug("No data found: PayPeriod or Business Unit is missing.");
			throw new NoDataFoundException("No data found: PayPeriod or Business Unit is missing.");
		}
		List<Object> params = new ArrayList<>();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT  a.employeesequenceno AS employeeid, ")
				.append("a.callname AS employeeName, a.employeeid AS empId, ")
				.append("f.businessunitid AS businessUnitId, ").append("COALESCE(c.name, 'NA') AS department, ")
				.append("COALESCE(d.name, 'NA') AS designation, ").append("COALESCE(e.name, 'NA') AS status, ")
				.append("l.transactionduration AS payperioddate, ").append("COALESCE(f.name, 'NA') AS businessUnit ")
				.append("FROM hclhrm_prod.tbl_employee_primary a ")
				.append("LEFT JOIN hclhrm_prod.tbl_employee_professional_details b ON a.employeeid = b.employeeid ")
				.append("LEFT JOIN hcladm_prod.tbl_department c ON c.departmentid = b.departmentid ")
				.append("LEFT JOIN hcladm_prod.tbl_designation d ON d.designationid = b.designationid ")
				.append("LEFT JOIN hcladm_prod.tbl_status_codes e ON e.status = a.status ")
				.append("LEFT JOIN hcladm_prod.tbl_businessunit f ON a.companyid = f.businessunitid ")
				.append("LEFT JOIN hcladm_prod.tbl_transaction_dates l ON l.businessunitid = f.businessunitid AND l.TRANSACTIONTYPEID = 1 ")
				.append("WHERE f.businessunitid = ? AND e.status IN (1001, 1092, 1401) AND l.transactionduration = ? ");

		params.add(bu);
		params.add(payPeriod);
		addOptionalConditions(sql, params, department, employeeName, status, employeeId);

		sql.append(" UNION ALL ");

// Build the second part of the UNION query
		sql.append("SELECT  a.employeesequenceno AS employeeid, ")
				.append("a.callname AS employeeName, a.employeeid AS empId, ")
				.append("f.businessunitid AS businessUnitId, ").append("COALESCE(c.name, 'NA') AS department, ")
				.append("COALESCE(d.name, 'NA') AS designation, ").append("COALESCE(e.name, 'NA') AS status, ")
				.append("l.transactionduration AS payperioddate, ").append("COALESCE(f.name, 'NA') AS businessUnit ")
				.append("FROM hclhrm_prod.tbl_employee_primary a ")
				.append("LEFT JOIN hclhrm_prod.tbl_employee_professional_details b ON a.employeeid = b.employeeid ")
				.append("LEFT JOIN hcladm_prod.tbl_department c ON c.departmentid = b.departmentid ")
				.append("LEFT JOIN hcladm_prod.tbl_designation d ON d.designationid = b.designationid ")
				.append("LEFT JOIN hcladm_prod.tbl_status_codes e ON e.status = a.status ")
				.append("LEFT JOIN hcladm_prod.tbl_businessunit f ON a.companyid = f.businessunitid ")
				.append("LEFT JOIN hclhrm_prod.tbl_employee_hractions k ON k.employeeid = a.employeeid ")
				.append("LEFT JOIN hcladm_prod.tbl_transaction_dates l ON l.businessunitid = f.businessunitid AND l.TRANSACTIONTYPEID = 1 ")
				.append("WHERE l.transactionduration = ? AND f.businessunitid = ? AND a.status = 1061 ")
				.append("AND k.LASTWORKINGDATE >= fromdate AND k.LASTWORKINGDATE <= todate");

		params.add(payPeriod);
		params.add(bu);
		addOptionalConditions(sql, params, department, employeeName, status, employeeId);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         
// Wrap the UNION ALL query in a subquery for pagination
		
		int offset = pageable.getPageNumber() * pageable.getPageSize();
		params.add(pageable.getPageSize());
		
		//department, employeeName, status, employeeId
		params.add(offset);
		
		
		String finalQuery = "SELECT * FROM (" + sql.toString() + ") AS combined_query LIMIT ? OFFSET ?";
		
		//System.err.println(finalQuery);
		//System.err.println(params);
		logger.debug("Final SQL Query: {}", finalQuery);
		logger.debug("Parameters: {}", params);

		List<Map<String, Object>> results = jdbcTemplate.queryForList(finalQuery, params.toArray());
		results.forEach(result -> {
			result.putIfAbsent("department", "NA");
			result.putIfAbsent("designation", "NA");
		});

		String countQuery = "SELECT COUNT(*) FROM (" + sql.toString() + ") AS combined_query";
		int totalCount = jdbcTemplate.queryForObject(countQuery, Integer.class,
				params.subList(0, params.size() - 2).toArray());

		return new PageImpl<>(results, pageable, totalCount);
	}

	private void addOptionalConditions(StringBuilder sql, List<Object> params, String department, String employeeName,
			String status, String employeeId) {
		if (department != null && !department.isEmpty()) {
			sql.append(" AND c.name = ?");
			params.add(department);
		}
		if (employeeName != null && !employeeName.isEmpty()) {
			sql.append(" AND a.callname LIKE ?");
			params.add("%" + employeeName + "%");
		}
		if (status != null && !status.isEmpty()) {
			sql.append(" AND e.name = ?");
			params.add(status);
		}
		if (employeeId != null && !employeeId.isEmpty()) {
			sql.append(" AND a.employeesequenceno = ?");
			params.add(Integer.parseInt(employeeId));
		}
	}

	public List<Map<String, Object>> businessunitComponent(String bu, String empCode, String componentid) {
		//System.err.print(componentid);
		String sql = "SELECT PAYCOMPONENTID AS paycomponentid, name FROM hcladm_prod.tbl_pay_component\r\n"
				+ "WHERE COMPONENTTYPEID = ? AND STATUS = 1001 AND PAYCOMPONENTID IN (SELECT PAYCOMPONENTID FROM hcladm_prod.tbl_businessunit_pay_component\r\n"
				+ "WHERE BUSINESSUNITID = ? AND STATUS = 1001 AND PAYCOMPONENTID NOT IN (SELECT PAYCOMPONENTID FROM hclhrm_prod_others.tbl_businessunit_pay_component_rights\r\n"
				+ "WHERE BUSINESSUNITID = ? AND STATUS = 1002 AND EMPLOYEEID = ?)) ORDER BY PAYCOMPONENTID";
		return jdbcTemplate.queryForList(sql, componentid, bu, bu, empCode);
	}

	public List<Map<String, Object>> getFetch(String payPeriod, String bu, String employeeid) {
		String sql = "SELECT TRANSACTIONID, BUSINESSUNITID, EMPLOYEEID, COMPONENTID, COMPONENTVALUE "
				+ "FROM hclhrm_prod.tbl_employee_earnings "
				+ "WHERE transactionid = ? AND businessunitId = ? AND EMPLOYEEID = ? " + "AND employeestatus = 1001";
		List<Object> params = Arrays.asList(payPeriod, bu, employeeid);
		List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, params.toArray());
		if (results.isEmpty()) {
			//System.out.println("No data found for the provided parameters.");
			logger.error("Earning  No data found for the provided parameters.");
		}
		logger.error("Earning " + results);
		return results;
	}

	public void insertMultipleEarnings(Integer employeeSequenceNo, String transactionId, Integer businessUnitId,
			List<Component> components) {
		String employeeIdQuery = "SELECT employeeid FROM hclhrm_prod.tbl_employee_primary WHERE employeesequenceno = ?";
		String sql = "INSERT INTO hclhrm_prod.tbl_employee_earnings ("
				+ "TRANSACTIONID, BUSINESSUNITID, EMPLOYEEID, COMPONENTID, COMPONENTVALUE, STATUS, "
				+ "EMPLOYEESTATUS, CREATEDBY, DATECREATED, MODIFIEDBY, DATEMODIFIED, VERIFIEDBY, "
				+ "DATEVERIFIED, LOGID, LUDATE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
				+ "ON DUPLICATE KEY UPDATE COMPONENTID = VALUES(COMPONENTID), "
				+ "COMPONENTVALUE = VALUES(COMPONENTVALUE), STATUS = VALUES(STATUS), "
				+ "EMPLOYEESTATUS = VALUES(EMPLOYEESTATUS), CREATEDBY = VALUES(CREATEDBY), "
				+ "DATECREATED = VALUES(DATECREATED), MODIFIEDBY = VALUES(MODIFIEDBY), "
				+ "DATEMODIFIED = VALUES(DATEMODIFIED), VERIFIEDBY = VALUES(VERIFIEDBY), "
				+ "DATEVERIFIED = VALUES(DATEVERIFIED), LOGID = VALUES(LOGID), LUDATE = VALUES(LUDATE)";

		try {
			Integer employeeId = jdbcTemplate.queryForObject(employeeIdQuery, Integer.class, employeeSequenceNo);
			for (Component component : components) {
				jdbcTemplate.update(sql, transactionId, businessUnitId, employeeId, component.getComponentId(),
						component.getComponentValue(), "C", 1001, 0, LocalDate.now(), 0, LocalDate.now(), 0,
						LocalDate.now(), 0L, new java.sql.Timestamp(System.currentTimeMillis()));
				logger.info("Successfully inserted or updated component ID: {} for employee ID: {}",
						component.getComponentId(), employeeId);
			}
		} catch (DataAccessException e) {
			String errorMessage = "Database error occurred: " + e.getMessage();
			logger.error(errorMessage, e);
			throw new RuntimeException(errorMessage);
		} catch (Exception e) {
			String unexpectedErrorMessage = "Unexpected error: " + e.getMessage();
			logger.error(unexpectedErrorMessage, e);
			throw new RuntimeException(unexpectedErrorMessage);
		}
	}

	public List<Map<String, Object>> getDistinctPayPeriod() {
		String sql = "select distinct(payperiod) as payPeriodDate from HCLHRM_PROD.TBL_EMPLOYEE_PAYPERIOD_DETAILS ORDER BY payperiod DESC  LIMIT 15";
		return jdbcTemplate.queryForList(sql);
	}

//	public List<Map<String, Object>> getDistinctBusinessUnits() {
//		String sql = "SELECT DISTINCT businessunitid AS Id, name AS Name FROM HCLADM_PROD.TBL_BUSINESSUNIT ORDER BY ID ASC";
//		return jdbcTemplate.queryForList(sql);
//	}
	public List<Map<String, Object>> getDistinctBusinessUnits(int empCode) {
		String sql = "SELECT BU.BUSINESSUNITID, BU.NAME AS BUNAME " + "FROM hclhrm_prod.tbl_employee_primary a "
				+ "LEFT JOIN hclhrm_prod.tbl_employee_businessunit b ON a.employeeid = b.employeeid  "
				+ "LEFT JOIN hcladm_prod.tbl_businessunit bu ON bu.BUSINESSUNITID = b.BUSINESSUNITID "
				+ "WHERE a.employeesequenceno = ?;";

		return jdbcTemplate.queryForList(sql, empCode);
	}

	public List<Map<String, Object>> getDepartment() {
		String sql = "SELECT departmentid,name FROM hcladm_prod.tbl_department";
		return jdbcTemplate.queryForList(sql);
	}

	public List<Map<String, Object>> getStatus() {
		String sql = "select status,name from hcladm_prod.tbl_status_codes where status in(1001,1092,1401,1061)";
		return jdbcTemplate.queryForList(sql);
	}

	public ResponseEntity<Object> getLogin(String empCode, String password) {
		try {
			String sql = "SELECT employeeid FROM hclhrm_prod.tbl_employee_login WHERE employeecode=? AND password=MD5(?)";
			List<Map<String, Object>> employeeData = jdbcTemplate.queryForList(sql, empCode, password);
			if (employeeData.isEmpty()) {
				ErrorResponse errorResponse = new ErrorResponse("true", "Employee not found or wrong password", "404");
				logger.debug(errorResponse + "" + "Internal server error", "500");
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
			}
			return ResponseEntity.ok(employeeData);
		} catch (Exception ex) {
			ErrorResponse errorResponse = new ErrorResponse("true", "Internal server error", "500");
			logger.debug(errorResponse + "" + "error", "Internal server error", "500");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}

	@Transactional
	public ResponseEntity<ErrorResponse> insertEarnings(EarningsRequestDto1 obj) {
		if (obj == null || obj.getComponents() == null || obj.getComponents().isEmpty()) {
			logger.debug(obj + "" + "error", "Invalid request data.", "400");
			return ResponseEntity.badRequest().body(new ErrorResponse("error", "Invalid request data.", "400"));
		}
		//System.err.println("sirisha---" + obj);
		
		List<Earning> earningsToInsert = obj.getComponents().stream().map(component -> {
			Earning earning = new Earning();
			KeyEntity key = new KeyEntity();
			int createdBy = obj.getCreatedBy();
			key.setBusinessUnitId(obj.getBusinessUnitId());
			key.setEmployeeId(obj.getEmployeeId());
			key.setTransactionId(obj.getTransactionId());
			key.setComponentId(component.getComponentId());
			earning.setId(key);
			 earning.setCreatedBy(createdBy) ;
//			earning.setCreatedBy(12779);
			LocalDateTime now = LocalDateTime.now();
			earning.setDateCreated(Timestamp.valueOf(now));
			earning.setDateModified(Timestamp.valueOf(now));
			earning.setDateVerified(Timestamp.valueOf(now));
			earning.setLuDate(Timestamp.valueOf(now));
			earning.setStatus('C');
			earning.setVerifiedBy(9);
			earning.setEmployeeStatus(1001);
			earning.setLogId(0);
			BigDecimal componentValue = new BigDecimal(component.getComponentValue());
			earning.setComponentValue(componentValue);
			return earning;
		}).collect(Collectors.toList());

		try {
			earningRepository.saveAll(earningsToInsert);
			logger.error("success", "Earnings inserted successfully.", "200");
			return ResponseEntity.ok(new ErrorResponse("success", "Earnings inserted successfully.", "200"));
		} catch (Exception e) {
			e.printStackTrace();
			String errorMessage = "Unexpected error: " + e.getMessage();
			logger.debug("No data found: PayPeriod or Business Unit is missing.");
			return ResponseEntity.status(500).body(new ErrorResponse("error", errorMessage, "500"));
		}
	}

	@Transactional
	public ResponseEntity<ErrorResponse> MultipleInsertEarning(List<EarningsRequestDto1> earningsRequests) {
		List<Earning> allEarningsToInsert = earningsRequests.stream()
				.flatMap(obj -> obj.getComponents().stream().map(component -> {
					Earning earning = new Earning();
					KeyEntity key = new KeyEntity();
					int createdBy = obj.getCreatedBy();
					key.setBusinessUnitId(obj.getBusinessUnitId());
					key.setEmployeeId(obj.getEmployeeId());
					key.setTransactionId(obj.getTransactionId());
					key.setComponentId(component.getComponentId());
					earning.setId(key);
					LocalDateTime now = LocalDateTime.now();
					earning.setCreatedBy(createdBy) ;
					earning.setDateCreated(Timestamp.valueOf(now));
					earning.setDateModified(Timestamp.valueOf(now));
					earning.setDateVerified(Timestamp.valueOf(now));
					earning.setLuDate(Timestamp.valueOf(now));
					earning.setStatus('C');
					earning.setVerifiedBy(9);
					earning.setEmployeeStatus(1001);
					earning.setLogId(0);
					try {
						BigDecimal componentValue = new BigDecimal(component.getComponentValue());
						earning.setComponentValue(componentValue);
					} catch (NumberFormatException e) {
						logger.error("Invalid component value: " + component.getComponentValue());
						throw new IllegalArgumentException("Invalid component value: " + component.getComponentValue());
					}

					return earning;
				})).collect(Collectors.toList());

		try {
			earningRepository.saveAll(allEarningsToInsert);
			logger.error("success", "Earnings inserted successfully.", "200");
			return ResponseEntity.ok(new ErrorResponse("success", "Earnings inserted successfully.", "200"));
		} catch (Exception e) {
			e.printStackTrace();
			String errorMessage = "Unexpected error: " + e.getMessage();
			logger.error(errorMessage);
			return ResponseEntity.status(500).body(new ErrorResponse("error", errorMessage, "500"));
		}
	}

	public Object getPayPeriodStatus(String payPeriod, int bu) {
		String sql = "SELECT payperiod, businessunitid,status FROM hclhrm_prod.tbl_businessunit_payroll_process t WHERE payperiod = ? AND businessunitid = ?";
		return jdbcTemplate.queryForList(sql, payPeriod, bu);
	}

	public List<Map<String, Object>> getComponentValueCountWithEmployeeCountFilter(int transactionId,
			int businessUnitId, int COMPONENTTYPEID, int EMPLOYEEID) {
		String sql = "SELECT b.componentid, a.paycomponentid, a.name, "
				+ "SUM(COALESCE(b.componentvalue, 0)) AS total_componentvalue, "
				+ "COUNT(DISTINCT CASE WHEN b.componentvalue > 0 THEN b.employeeid END) AS employee_count "
				+ "FROM hclhrm_prod.tbl_employee_earnings b " + "LEFT JOIN ( "
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

//	public List<EmployeeEarningReport> reportStatus() {
//		return employeeEarningReportRepository.findAll();
//	}
	public List<EmployeeEarningReport> reportStatus() {
		List<EmployeeEarningReport> lastRecords = employeeEarningReportRepository.findLast10Records();
		return lastRecords;
	}
}
