package com.earnings.payadjustements.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.earnings.payadjustements.Exception.NoDataFoundException;
import com.earnings.payadjustements.repository.EarningDataRepository;

@Service
public class EarningExcelDataServices {

	private static final Logger logger = LoggerFactory.getLogger(EarningExcelDataServices.class);

	@Autowired
	private EarningDataRepository earningDataRepository;


	public byte[] generateExcelFile(String payPeriod, int businessUnitId, String empCode, String componentId)
	        throws  Exception {

	    List<Object[]> empBasicDetails = earningDataRepository.fetchEmployeeData(payPeriod, businessUnitId);
	    List<Object[]> payComponents = earningDataRepository.fetchPayComponentData(businessUnitId, empCode, componentId);

	    if (empBasicDetails.isEmpty()) {
	    	logger.debug("No data found for the given criteria");
	        throw new NoDataFoundException("No data found for the given criteria.");
	    }
	    Map<Integer, String> payComponentMap = new TreeMap<>();
	    for (Object[] payComponent : payComponents) {
	        payComponentMap.put((Integer) payComponent[0], (String) payComponent[1]);
	    }

	    try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
	        Sheet sheet = workbook.createSheet("EmployeeData");
	        String[] headers = earningDataRepository.fetchColumnNames(payPeriod, businessUnitId);
	        
	        Row headerRow = sheet.createRow(0);
	        int headerIndex = 0;

	        // Write the employee data headers
	        for (String header : headers) {
	            Cell cell = headerRow.createCell(headerIndex++);
	            cell.setCellValue(header);
	        }

	        // Write the pay component headers in the order of componentId
	        for (String payComponentName : payComponentMap.values()) {
	            Cell cell = headerRow.createCell(headerIndex++);
	            cell.setCellValue(payComponentName);
	        }

	        int rowNum = 1;
	        for (Object[] row : empBasicDetails) {
	            Row dataRow = sheet.createRow(rowNum++);
	            int cellIndex = 0;
	            int employeeId = (Integer) row[0];
	            for (Object cellValue : row) {
	                Cell cell = dataRow.createCell(cellIndex++);
	                cell.setCellValue(cellValue != null ? cellValue.toString() : "NA");
	            }
	            List<Object[]> earningsData = earningDataRepository.earningsQueryForEmployee(employeeId, payPeriod, businessUnitId);
	            Map<Integer, String> earningsMap = new HashMap<>();
	            for (Object[] earningData : earningsData) {
	                earningsMap.put((Integer) earningData[0], (String) earningData[1]);
	            }
	            for (Integer payComponentId : payComponentMap.keySet()) {
	                Cell cell = dataRow.createCell(cellIndex++);
	                String earningValue = earningsMap.getOrDefault(payComponentId, "0");
	                cell.setCellValue(earningValue);
	            }
	        }
	        
	        workbook.write(out);
	        return out.toByteArray();
	    } catch (IOException e) {
	        logger.error("Error creating Excel workbook: {}", e.getMessage());
	        throw e;
	    }
	}

}
