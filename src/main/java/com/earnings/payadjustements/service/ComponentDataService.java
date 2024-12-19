package com.earnings.payadjustements.service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.earnings.payadjustements.Exception.MissingHeaderException;
import com.earnings.payadjustements.entity.Earning;
import com.earnings.payadjustements.entity.EarningRepository;
import com.earnings.payadjustements.entity.EmployeeEarningReport;
import com.earnings.payadjustements.entity.EmployeeEarningReportRepository;
import com.earnings.payadjustements.entity.EmployeeRepository;
import com.earnings.payadjustements.entity.FailedEmployee;
import com.earnings.payadjustements.entity.IdEntity;
import com.earnings.payadjustements.entity.KeyEntity;

@Service
public class ComponentDataService {

	private final EmployeeRepository employeeRepository;
	private final EarningRepository earningRepository;
	private final EmployeeEarningReportRepository employeeEarningReportRepository;
	private final JdbcTemplate jdbcTemplate;
	private static final Logger logger = LoggerFactory.getLogger(ComponentDataService.class);

	public ComponentDataService(EmployeeRepository employeeRepository, EarningRepository earningRepository,
			EmployeeEarningReportRepository employeeEarningReportRepository, JdbcTemplate jdbcTemplate) {
		this.employeeRepository = employeeRepository;
		this.earningRepository = earningRepository;
		this.employeeEarningReportRepository = employeeEarningReportRepository;
		this.jdbcTemplate = jdbcTemplate;
	}

	@Transactional(rollbackFor = Throwable.class)
	public CompletableFuture<List<FailedEmployee>> processExcelFile(InputStream inputStream, int componentTypeId,
			int payPeriod, int bu, List<FailedEmployee> failedList, MultipartFile file, int empCode, int count)
			throws IOException {
		
		String fileName = file.getOriginalFilename();
		List<CompletableFuture<String>> futures = new ArrayList<>();
		int uniqueEmployeeCount = 0;  // Counter for unique employees
		try (Workbook workbook = WorkbookFactory.create(inputStream)) {
			Sheet sheet = workbook.getSheetAt(0);
			List<String> headerNames = extractHeaderNames(sheet);
			List<String> expectedColumnNames = fetchColumns(componentTypeId, bu);
			List<String> missingHeaders = expectedColumnNames.stream().filter(header -> !headerNames.contains(header))
					.collect(Collectors.toList());

			List<String> extraHeaders = headerNames.stream().filter(header -> !expectedColumnNames.contains(header))
					.collect(Collectors.toList());
			if (!missingHeaders.isEmpty() || !extraHeaders.isEmpty()) {
				StringBuilder errorMessage = new StringBuilder("Header name mismatches found:");
				if (!missingHeaders.isEmpty()) {
					errorMessage.append(" Missing: ").append(missingHeaders);
				}
				if (!extraHeaders.isEmpty()) {
					errorMessage.append(" Extra headers: ").append(extraHeaders);
				}
				throw new MissingHeaderException(errorMessage.toString());
			}

			// Process rows, check unique employee count
			Set<Integer> processedEmployeeIds = new HashSet<>();
			processRows(sheet, componentTypeId, payPeriod, bu, failedList, futures, headerNames, fileName, empCode,
						processedEmployeeIds, uniqueEmployeeCount,count);
			
			

			// Await completion of all futures
			CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
			try {
				allFutures.get(2, TimeUnit.MINUTES);
			} catch (TimeoutException e) {
				logger.warn("Processing timed out. Returning current failedList: {}", failedList);
				return CompletableFuture.completedFuture(failedList);
			}

			// Check if there were any failures
			boolean hasFailures = !failedList.isEmpty() || futures.stream().anyMatch(future -> future.join() != null);
			int reportStatus = hasFailures ? 1003 : 1002;

			// Save report and return failed list
			
			saveReport(fileName, failedList, empCode, reportStatus);
			
			return CompletableFuture.completedFuture(failedList);
		} catch (Exception e) {
			logger.error("Failed to process Excel file", e);
			throw new RuntimeException("Failed to process Excel file", e);
		}
	}

	private void processRows(Sheet sheet, int componentTypeId, int payPeriod, int bu, List<FailedEmployee> failedList,
			List<CompletableFuture<String>> futures, List<String> headerNames, String fileName, int empCode,
			Set<Integer> processedEmployeeIds, int uniqueEmployeeCount,int count) throws MissingHeaderException  {
		
	
		 
		for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
			Row row = sheet.getRow(rowIndex);
			if (row != null) {
				Integer employeeId = parseNumericCellSafely(row.getCell(0));
				Integer fileComponentTypeId = parseNumericCellSafely(row.getCell(5));
				String fileBusinessUnit = getStringCellValue(row.getCell(6));
				Integer businessUnitId = employeeRepository.findBusinessUnitIdByName(fileBusinessUnit);
				int payperioddate = parseNumericCellSafely(row.getCell(5));
				
				if (!isPayPeriodDateValid(payperioddate, payPeriod)) {
					logger.warn("Pay period date mismatch for Employee ID: {}. Expected: {}, Found: {}", employeeId,
							payPeriod, payperioddate);
					if (employeeId != null) {
						addEmployeeToFailedList(employeeId, rowIndex, failedList);
					}
					continue;
				}

				if (fileComponentTypeId != null && fileComponentTypeId.equals(payperioddate) && businessUnitId != null
						&& businessUnitId.equals(bu)) {
					if (employeeId != null) {
						if (processedEmployeeIds.contains(employeeId)) {
							logger.warn("Duplicate Employee ID found: {}", employeeId);
							addEmployeeToFailedList(employeeId, rowIndex, failedList);
							break;
						}
						processedEmployeeIds.add(employeeId);
						uniqueEmployeeCount++;  // Increment unique employee count

						Integer emp = employeeRepository.getEmployeeId(employeeId);
						if (emp == null) {
							addEmployeeToFailedList(employeeId, rowIndex, failedList);
							logger.warn("Employee ID not found: {}", employeeId);
						} else { 
							futures.add(processEmployeeEarnings(employeeId, row, headerNames, payperioddate, bu,
									failedList, componentTypeId, fileName, empCode, rowIndex));
						}
					}
				} 
				
				//uniqueEmployeeCount
				
				else {
					logger.warn("Row skipped due to mismatched conditions. Employee ID: {}", employeeId);
				}
			}
			
			
			
			
			
		}
		//System.err.println(uniqueEmployeeCount+"uniqueEmployeeCount");
		boolean datacheck=true;
		System.err.println(uniqueEmployeeCount+"uniqueEmployeeCount");
		if (uniqueEmployeeCount != count) {
			String errorMessage = "Please check the uploaded file. Employee count is mismatched.\n"
                    + "Expected count: " + count + ", Found: " + uniqueEmployeeCount;

			logger.error(errorMessage);
			System.err.println("coming"+errorMessage);
			//failedList.add(errorMessage);
			datacheck=false;
			
			 
//			    throw new EmployeeCountMismatchException("Mismatch between expected and actual unique employee count. Expected: "
//			            + count + ", Found: " + uniqueEmployeeCount);
			    
			    throw new MissingHeaderException(errorMessage.toString());
			//}

 			// addEmployeeToFailedList(0,'1',failedList);
			//return CompletableFuture.completedFuture(failedList);  // Return with failed employees
			
		}
		
	}

	private List<String> extractHeaderNames(Sheet sheet) {
		Row headerRow = sheet.getRow(0);
		return Optional.ofNullable(headerRow).map(row -> {
			List<String> headerNames = new ArrayList<>();
			for (int colIndex = 7; colIndex < row.getLastCellNum(); colIndex++) {
				String headerName = getStringCellValue(row.getCell(colIndex));
				headerNames.add(headerName);
			}
			return headerNames;
		}).orElse(Collections.emptyList());
	}

	private List<String> fetchColumns(int componentTypeId, int businessUnitId) {
		String sql = "SELECT NAME FROM hcladm_prod.tbl_pay_component " + "WHERE COMPONENTTYPEID = ? AND STATUS = 1001 "
				+ "AND PAYCOMPONENTID IN (SELECT PAYCOMPONENTID FROM hcladm_prod.tbl_businessunit_pay_component "
				+ "WHERE BUSINESSUNITID = ? AND STATUS = 1001) " + "ORDER BY PAYCOMPONENTID";
		return jdbcTemplate.queryForList(sql, String.class, componentTypeId, businessUnitId);
	}

	public boolean isPayPeriodDateValid(int payPeriodDate, int payPeriod) {
		return payPeriodDate == payPeriod;
	}

	private CompletableFuture<String> processEmployeeEarnings(Integer employeeId, Row row, List<String> headerNames,
			int payperioddate, int bu, List<FailedEmployee> failedList, int componentTypeId, String fileName,
			int empCode, int rowIndex) {
		return CompletableFuture.supplyAsync(() -> {
			List<String> values = new ArrayList<>();
			for (int colIndex = 7; colIndex < row.getLastCellNum(); colIndex++) {
				values.add(getStringCellValue(row.getCell(colIndex)));
			}
			return saveEarnings(employeeId, values, headerNames, payperioddate, bu, failedList, componentTypeId,
					fileName, empCode, rowIndex);
		});
	}

	private String saveEarnings(Integer employeeId, List<String> values, List<String> headerNames, int payperioddate,
			int bu, List<FailedEmployee> failedList, int componentTypeId, String fileName, int empCode, int rowIndex) {
		Map<Integer, String> payComponentValueMap = createPayComponentValueMap(values, headerNames, componentTypeId);
		Timestamp currentTime = Timestamp.valueOf(LocalDateTime.now());
		List<Earning> earningsToSave = new ArrayList<>();

		for (Map.Entry<Integer, String> entry : payComponentValueMap.entrySet()) {
			String formatException = validateAndPrepareEarning(employeeId, bu, payperioddate, currentTime, entry,
					failedList, rowIndex);
			if (formatException != null)
				return formatException;
			Integer employeeIds = employeeRepository.getEmployeeId(employeeId);
			earningsToSave.add(createEarning(employeeIds, bu, payperioddate, entry, currentTime,empCode));
		}

		earningRepository.saveAll(earningsToSave);
		return null;
	}

	private Map<Integer, String> createPayComponentValueMap(List<String> values, List<String> headerNames,
			int componentTypeId) {
		return IntStream.range(0, headerNames.size()).boxed().collect(Collectors.toMap(
				i -> employeeRepository.getPaycomponentId(componentTypeId, headerNames.get(i)),
				i -> i < values.size() ? values.get(i) : "", (existing, replacement) -> existing, LinkedHashMap::new));
	}

	private String validateAndPrepareEarning(Integer employeeId, int bu, int payperioddate, Timestamp currentTime,
			Map.Entry<Integer, String> entry, List<FailedEmployee> failedList, int rowIndex) {
		String componentValue = entry.getValue();
		try {
			BigDecimal value = componentValue.isEmpty() ? BigDecimal.ZERO : new BigDecimal(componentValue);
			if (value.signum() < 0) {
				addEmployeeToFailedList(employeeId, rowIndex, failedList);
				return "Negative value entered for employeeId: " + employeeId;
			}
		} catch (NumberFormatException e) {
			addEmployeeToFailedList(employeeId, rowIndex, failedList);
			return "Invalid number format for component value: " + componentValue
					+ ". Stopping processing for employeeId: " + employeeId;
		}
		return null;
	}

	private void addEmployeeToFailedList(Integer employeeId, int rowIndex, List<FailedEmployee> failedList) {
		failedList.add(new FailedEmployee(employeeId, rowIndex));
	}
	
	 

	private Earning createEarning(Integer employeeIds, int bu, int payperioddate, Map.Entry<Integer, String> entry,
			Timestamp currentTime,int empCode) {
		Earning earning = new Earning();
		KeyEntity key = new KeyEntity();
		key.setBusinessUnitId(bu);
		key.setEmployeeId(employeeIds);
		key.setTransactionId(payperioddate);
		key.setComponentId(entry.getKey());

		earning.setId(key);
		earning.setComponentValue(new BigDecimal(entry.getValue()));
		earning.setStatus('C');
		earning.setEmployeeStatus(1001);
		earning.setCreatedBy(empCode);
		earning.setDateCreated(currentTime);
		earning.setModifiedBy(1);
		earning.setDateModified(currentTime);
		earning.setVerifiedBy(9);
		earning.setDateVerified(currentTime);
		earning.setLogId(0);
		earning.setLuDate(currentTime);

		return earning;
	}

	private Integer parseNumericCellSafely(Cell cell) {
		return Optional.ofNullable(cell).map(c -> {
			if (c.getCellType() == CellType.NUMERIC) {
				return (int) c.getNumericCellValue();
			} else if (c.getCellType() == CellType.STRING) {
				try {
					return Integer.parseInt(c.getStringCellValue().trim());
				} catch (NumberFormatException e) {
					logger.warn("Invalid numeric value in string cell: {}", c.getStringCellValue());
				}
			}
			return null;
		}).orElse(null);
	}

	private String getStringCellValue(Cell cell) {
		return Optional.ofNullable(cell).map(c -> c.getCellType() == CellType.STRING ? c.getStringCellValue()
				: String.valueOf((int) c.getNumericCellValue())).orElse("");
	}

	@Transactional
	public EmployeeEarningReport saveReport(String fileName, List<FailedEmployee> failedList, int empCode,
			int reportStatus) {
		EmployeeEarningReport employeeEarningReport = new EmployeeEarningReport();
		employeeEarningReport.setId(new IdEntity());
		employeeEarningReport.setFilename(fileName);
		employeeEarningReport.setFilefailedemployees(failedList.stream()
				.map(f -> f.getEmployeeId() + " (Row: " + f.getRowIndex() + ")").collect(Collectors.joining(", ")));
		employeeEarningReport.setReason(failedList.isEmpty() ? "Inserted" : "Failures occurred");
		employeeEarningReport.setUploadedby(empCode);
		employeeEarningReport.setStatus(reportStatus);

		try {
			return employeeEarningReportRepository.save(employeeEarningReport);
		} catch (Exception e) {
			logger.error("Failed to save the report", e);
			throw new RuntimeException("Failed to save the report: " + e.getMessage(), e);
		}
	}
}
