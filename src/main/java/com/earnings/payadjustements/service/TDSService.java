package com.earnings.payadjustements.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.earnings.payadjustements.ErrorResponse;
import com.earnings.payadjustements.entity.Tds;
import com.earnings.payadjustements.entity.TdsDto1;
import com.earnings.payadjustements.entity.TdsKeyEntity;
import com.earnings.payadjustements.entity.TdsRepository;

@Service
public class TDSService {

	private JdbcTemplate jdbcTemplate;
	private TdsRepository tdsRepository;
	private static final Logger logger = LoggerFactory.getLogger(TDSService.class);

	public TDSService(JdbcTemplate jdbcTemplate, TdsRepository tdsRepository) {
		this.jdbcTemplate = jdbcTemplate;
		this.tdsRepository = tdsRepository;
	}

	public List<Map<String, Object>> getTds(String payPeriod, String bu, String employeeid) {
		String sql = "SELECT TRANSACTIONID, BUSINESSUNITID, EMPLOYEEID, TDSVALUE "
				+ "FROM hclhrm_prod.tbl_employee_tds "
				+ "WHERE transactionid = ? AND businessunitId = ? AND EMPLOYEEID = ? " + "AND employeestatus = 1001";
		List<Object> params = Arrays.asList(payPeriod, bu, employeeid);
		List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, params.toArray());
		if (results.isEmpty()) {
			logger.debug("No data found for the provided parameters");
			System.out.println("No data found for the provided parameters.");
		}
		return results;
	}


	private static final String EMPLOYEE_QUERY = "SELECT DISTINCT a.employeesequenceno AS employeeid, "
			+ "a.callname AS employeeName, c.name AS department, d.name AS designation, "
			+ "e.name AS status, g.payperiod AS payperioddate, f.name AS businessUnit, "
			+ "COALESCE(t.tdsvalue, 0) AS tdsvalue " + "FROM hclhrm_prod.tbl_employee_primary a "
			+ "LEFT JOIN hclhrm_prod.tbl_employee_professional_details b ON a.employeeid = b.employeeid "
			+ "LEFT JOIN hcladm_prod.tbl_department c ON c.departmentid = b.departmentid "
			+ "LEFT JOIN hcladm_prod.tbl_designation d ON d.designationid = b.designationid "
			+ "LEFT JOIN hcladm_prod.tbl_status_codes e ON e.status = a.status "
			+ "LEFT JOIN hcladm_prod.tbl_businessunit f ON a.companyid = f.businessunitid "
			+ "LEFT JOIN hclhrm_prod.tbl_employee_tds t ON a.employeeid = t.employeeid "
			+ "AND t.businessunitid = ? AND t.transactionid = ? "
			+ "JOIN hclhrm_prod.tbl_employee_payperiod_details g ON a.employeesequenceno = g.employeesequenceno "
			+ "WHERE g.payperiod = ? AND f.businessunitid = ? AND f.status = 1001";

	public List<Object[]> fetchEmployeeData(String payPeriod, int businessUnitId) {
		return jdbcTemplate.query(EMPLOYEE_QUERY, new Object[] { businessUnitId, payPeriod, payPeriod, businessUnitId },
				(rs, rowNum) -> new Object[] { rs.getInt("employeeid"), rs.getString("employeeName"),
						rs.getString("department"), rs.getString("designation"), rs.getString("status"),
						rs.getString("payperioddate"), rs.getString("businessUnit"), rs.getBigDecimal("tdsvalue") 																						// here
				});
	}

	public byte[] generateExcelFile(String payPeriod, int businessUnitId, String empCode) throws IOException {
		List<Object[]> employeeData = fetchEmployeeData(payPeriod, businessUnitId);

		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("Employee TDS Data");

		String[] headers = fetchColumnNames();
		Row headerRow = sheet.createRow(0);
		for (int i = 0; i < headers.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(headers[i]);
			cell.setCellStyle(getHeaderCellStyle(workbook));
		}

		int rowNum = 1;
		for (Object[] employee : employeeData) {
			Row row = sheet.createRow(rowNum++);
			for (int col = 0; col < employee.length; col++) {
				Cell cell = row.createCell(col);
				if (employee[col] instanceof Integer) {
					cell.setCellValue((Integer) employee[col]);
				} else if (employee[col] instanceof String) {
					cell.setCellValue((String) employee[col]);
				} else if (employee[col] instanceof BigDecimal) {
					cell.setCellValue(((BigDecimal) employee[col]).toString());
				}
			}
		}

		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			workbook.write(out);
			return out.toByteArray();
		}
	}

	private String[] fetchColumnNames() {
		return new String[] { "Employee ID", "Employee Name", "Department", "Designation", "Status", "Pay Period",
				"Business Unit", "TDS Value" };
	}

	private CellStyle getHeaderCellStyle(Workbook workbook) {
		CellStyle style = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setBold(true);
		style.setFont(font);
		return style;
	}

	public void uploadExcelData(MultipartFile file) throws IOException {
		List<Object[]> batchArgs = new ArrayList<>();
		Map<String, Long> businessUnitIdMap = new HashMap<>();
		final int BATCH_SIZE = 500;
		try (InputStream fis = file.getInputStream(); Workbook workbook = WorkbookFactory.create(fis)) {
			Sheet sheet = workbook.getSheetAt(0);
			int lastRowNum = sheet.getLastRowNum();
			List<String> businessUnitNames = new ArrayList<>();
			for (int i = 1; i <= lastRowNum; i++) {
				Row row = sheet.getRow(i);
				if (row != null) {
					String businessUnitString = getCellValueAsString(row.getCell(6));
					if (!businessUnitString.isEmpty() && !businessUnitIdMap.containsKey(businessUnitString)) {
						businessUnitNames.add(businessUnitString);
					}
				}
			}
			System.out.println("Business Unit Names: " + businessUnitNames);
			if (!businessUnitNames.isEmpty()) {
				List<Map<String, Object>> businessUnits = getBusinessUnitIds(businessUnitNames);
				for (Map<String, Object> unit : businessUnits) {
					businessUnitIdMap.put((String) unit.get("name"), ((Number) unit.get("businessunitid")).longValue());
				}
			}
			for (int i = 1; i <= lastRowNum; i++) {
				Row row = sheet.getRow(i);
				if (row == null)
					continue;

				try {
					String businessUnitString = getCellValueAsString(row.getCell(6));
					Long businessUnitId = businessUnitIdMap.get(businessUnitString);
					if (businessUnitId == null) {
						System.err.println("Business unit ID not found for: " + businessUnitString);
						continue;
					}

					int employeeId = (int) Double.parseDouble(getCellValueAsString(row.getCell(0)));
					int transactionId = (int) Double.parseDouble(getCellValueAsString(row.getCell(5)));
					double tdsValue = Double.parseDouble(getCellValueAsString(row.getCell(7)));
					System.out.println("Adding to batch: Transaction ID: " + transactionId + ", Business Unit ID: "
							+ businessUnitId + ", Employee ID: " + employeeId + ", TDS Value: " + tdsValue);

					batchArgs.add(new Object[] { transactionId, businessUnitId, employeeId, tdsValue });
					if (batchArgs.size() >= BATCH_SIZE) {
						saveBatch(batchArgs);
						batchArgs.clear();
					}
				} catch (NumberFormatException e) {
					System.err.println("Skipping row " + (i + 1) + " due to number format error: " + e.getMessage());
				} catch (Exception e) {
					System.err.println("Skipping row " + (i + 1) + " due to error: " + e.getMessage());
					e.printStackTrace();
				}
			}
			if (!batchArgs.isEmpty()) {
				saveBatch(batchArgs);
			}
		}
	}

	private List<Map<String, Object>> getBusinessUnitIds(List<String> businessUnitNames) {
		if (businessUnitNames.isEmpty()) {
			return Collections.emptyList();
		}

		String sql = "SELECT name, businessunitid FROM hcladm_prod.tbl_businessunit WHERE name IN ("
				+ String.join(",", Collections.nCopies(businessUnitNames.size(), "?")) + ")";

		try {
			return jdbcTemplate.queryForList(sql, businessUnitNames.toArray());
		} catch (DataAccessException e) {
			System.err.println("Error fetching business unit IDs: " + e.getMessage());
			return Collections.emptyList();
		}
	}

	private void saveBatch(List<Object[]> batchArgs) {
		String sql = "INSERT INTO hclhrm_prod.tbl_employee_tds ("
				+ "TRANSACTIONID, BUSINESSUNITID, EMPLOYEEID, TDSVALUE, STATUS, EMPLOYEESTATUS, "
				+ "CREATEDBY, DATECREATED, MODIFIEDBY, DATEMODIFIED, VERIFIEDBY, DATEVERIFIED, LOGID, LUDATE) "
				+ "SELECT " + "'?', COMPANYID, EMPLOYEEID, ?, 'C', '1001', "
				+ "CURRENT_TIMESTAMP, '0', CURRENT_TIMESTAMP, '0', CURRENT_TIMESTAMP, '0', CURRENT_TIMESTAMP "
				+ "FROM hclhrm_prod.tbl_employee_primary " + "WHERE employeesquenceno = ?" + "ON DUPLICATE KEY UPDATE "
				+ "STATUS = VALUES(STATUS), " + "EMPLOYEESTATUS = VALUES(EMPLOYEESTATUS), "
				+ "CREATEDBY = VALUES(CREATEDBY), " + "DATECREATED = VALUES(DATECREATED), "
				+ "MODIFIEDBY = VALUES(MODIFIEDBY), " + "DATEMODIFIED = VALUES(DATEMODIFIED), "
				+ "VERIFIEDBY = VALUES(VERIFIEDBY), " + "DATEVERIFIED = VALUES(DATEVERIFIED), "
				+ "LOGID = VALUES(LOGID), " + "LUDATE = VALUES(LUDATE);";

		try {
			jdbcTemplate.batchUpdate(sql, batchArgs);
			System.out.println("Batch update executed successfully.");
			logger.debug("Batch update executed successfully.");
		} catch (DataAccessException e) {
			System.err.println("Error during batch update: " + e.getMessage());
			logger.debug("Error during batch update: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private String getCellValueAsString(Cell cell) {
		if (cell == null) {
			return "";
		}
		switch (cell.getCellType()) {
		case STRING:
			return cell.getStringCellValue().trim();
		case NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				return String.valueOf(cell.getDateCellValue().getTime());
			} else {
				return String.valueOf(cell.getNumericCellValue()).trim();
			}
		case BOOLEAN:
			return String.valueOf(cell.getBooleanCellValue());
		case FORMULA:
			return cell.getCellFormula();
		default:
			return "";
		}
	}

	public ResponseEntity<ErrorResponse> tdsInsert(TdsDto1 obj) {
		Tds tds = new Tds();
		TdsKeyEntity key = new TdsKeyEntity();
		key.setBusinessUnitId(obj.getBusinessUnitId());
		key.setEmployeeId(obj.getEmployeeId());
		key.setTransactionId(obj.getTransactionId());

		tds.setId(key);
		tds.setTdsValue(obj.getTdsValue());
		tds.setCreatedBy(12779);
		LocalDateTime now = LocalDateTime.now();
		tds.setDateCreated(Timestamp.valueOf(now));
		tds.setDateModified(Timestamp.valueOf(now));
		tds.setDateVerified(Timestamp.valueOf(now));
		tds.setLuDate(Timestamp.valueOf(now));
		tds.setStatus('C');
		tds.setVerifiedBy(0);
		tds.setEmployeeStatus(1001);
		tds.setLogId(0);
		tdsRepository.save(tds);
		logger.debug("success", "TDS data inserted successfully.", "200");
		return ResponseEntity.ok(new ErrorResponse("success", "TDS data inserted successfully.", "200"));
	}


	public ResponseEntity<ErrorResponse> multipleInsertTds(List<TdsDto1> tdsDtoList) {
//		if (tdsDtoList == null || tdsDtoList.isEmpty()) {
//			return ResponseEntity.badRequest()
//					.body(new ErrorResponse("error", "Invalid request data: list cannot be null or empty.", "400"));
//		}

		try {
			List<Tds> tdsEntities = tdsDtoList.stream().map(dto -> {
				Tds tds = new Tds();
				TdsKeyEntity key = new TdsKeyEntity();
				key.setBusinessUnitId(dto.getBusinessUnitId());
				key.setEmployeeId(dto.getEmployeeId());
				key.setTransactionId(dto.getTransactionId());
				tds.setId(key);
				tds.setTdsValue(dto.getTdsValue());
				tds.setCreatedBy(12779);
				LocalDateTime now = LocalDateTime.now();
				tds.setDateCreated(Timestamp.valueOf(now));
				tds.setDateModified(Timestamp.valueOf(now));
				tds.setDateVerified(Timestamp.valueOf(now));
				tds.setLuDate(Timestamp.valueOf(now));
				tds.setStatus('C');
				tds.setVerifiedBy(0);
				tds.setEmployeeStatus(1001);
				tds.setLogId(0);

				return tds;
			}).collect(Collectors.toList());
			logger.debug("success", "TDS data inserted successfully.");
			tdsRepository.saveAll(tdsEntities);

			return ResponseEntity.ok(new ErrorResponse("success", "TDS data inserted successfully.", "200"));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(new ErrorResponse("error", e.getMessage(), "400"));
		} catch (Exception e) {
			String errorMessage = "Unexpected error: " + e.getMessage();
			logger.debug(errorMessage);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorResponse("error", errorMessage, "500"));
		}
	}

}
