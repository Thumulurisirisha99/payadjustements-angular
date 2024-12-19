package com.earnings.payadjustements.controller;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.earnings.payadjustements.ErrorResponse;
import com.earnings.payadjustements.entity.DeductionRequestDto1;
import com.earnings.payadjustements.entity.FailedEmployee;
import com.earnings.payadjustements.entity.Response;
import com.earnings.payadjustements.service.DeductionExcelDataServices;
import com.earnings.payadjustements.service.DeductionExcelService;
import com.earnings.payadjustements.service.DeductionService;

@RestController
public class DeductionController {
	private DeductionService deductionService;
	private DeductionExcelDataServices deductionExcelDataServices;
	private DeductionExcelService deductionExcelService;

	public DeductionController(DeductionService deductionService, DeductionExcelDataServices deductionExcelDataServices,
			DeductionExcelService deductionExcelService) {
		this.deductionService = deductionService;
		this.deductionExcelDataServices = deductionExcelDataServices;
		this.deductionExcelService = deductionExcelService;
	}

	@GetMapping(value = "/deduction/{payPeriod}/{bu}/{employeeid}", produces = "application/json")
	public ResponseEntity<List<Map<String, Object>>> getDeduction(@PathVariable("payPeriod") String payPeriod,
			@PathVariable("bu") String bu, @PathVariable("employeeid") String employeeid) {

		List<Map<String, Object>> data = deductionService.getDeduction(payPeriod, bu, employeeid);
		return ResponseEntity.ok(data);
	}

	@PostMapping(value = "/deductioninsert", produces = "application/json")
	public ResponseEntity<ErrorResponse> insertDeduction(@RequestBody DeductionRequestDto1 obj) {
		return deductionService.insertDeduction(obj);
	}

	@PostMapping("/deductioninsertMultiple")
	public ResponseEntity<ErrorResponse> MultipleInsertDeduction(@Valid @RequestBody List<DeductionRequestDto1> obj) {
		return deductionService.MultipleInsertDeduction(obj);
	}

	@PostMapping("/deductioncomponent/total")
	public List<Map<String, Object>> getComponentCount(@RequestParam("transactionid") int transactionId,
			@RequestParam("businessunitid") int businessUnitId, @RequestParam("COMPONENTTYPEID") int COMPONENTTYPEID,
			@RequestParam("EMPLOYEEID") int EMPLOYEEID) {
		return deductionService.getComponentValueCountWithEmployeeCountFilter(transactionId, businessUnitId,
				COMPONENTTYPEID, EMPLOYEEID);
	}

	@PostMapping(value = "/deductionexport", consumes = "multipart/form-data", produces = "application/json")
	public ResponseEntity<byte[]> exportToExcel(@RequestParam("payPeriod") String payPeriod,
			@RequestParam("businessUnitId") int businessUnitId, @RequestParam("empCode") String empCode,
			@RequestParam("componentId") String componentId) {
		try {
			byte[] excelFileBytes = deductionExcelDataServices.generateExcelFile(payPeriod, businessUnitId, empCode,
					componentId);
			String filename = "component_employee_data_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date(0))
					+ ".xlsx";
			HttpHeaders headers = new HttpHeaders();
			headers.setContentDispositionFormData("attachment", filename);
			headers.setContentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
			return ResponseEntity.ok().headers(headers).body(excelFileBytes);
		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PostMapping(value = "/deductionuploads", produces = "application/json")
	public ResponseEntity<Response> uploadEarningsFile(@RequestParam("file") MultipartFile file,
			@RequestParam("componenttypeid") int componenttypeid, @RequestParam("payPeriod") int payPeriod,
			@RequestParam("bu") int bu, @RequestParam("empCode") int empCode,@RequestParam("count")int count) throws IOException {
		List<FailedEmployee> failedList = new ArrayList<>();
		deductionExcelService.processDeductionExcelFile(file.getInputStream(), componenttypeid, payPeriod, bu,
				failedList, file, empCode,count);
		System.err.println("failedList----------" + failedList);
		String status = failedList.isEmpty() ? "true" : "false";
		String message = failedList.isEmpty() ? "File uploaded and data processed successfully."
				: "File uploaded, but some entries failed to process.";
		return ResponseEntity.ok(new Response(status, message, String.valueOf(HttpStatus.OK.value()), failedList));
	}
}
