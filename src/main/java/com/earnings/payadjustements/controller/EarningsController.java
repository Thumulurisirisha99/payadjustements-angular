package com.earnings.payadjustements.controller;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import com.earnings.payadjustements.entity.EarningsRequestDto1;
import com.earnings.payadjustements.entity.EmployeeEarningReport;
import com.earnings.payadjustements.entity.FailedEmployee;
import com.earnings.payadjustements.entity.Response;
import com.earnings.payadjustements.service.ComponentDataService;
import com.earnings.payadjustements.service.EarningExcelDataServices;
import com.earnings.payadjustements.service.EarningService;

@RestController
public class EarningsController {
	private EarningService earningService;
	private EarningExcelDataServices earningExcelDataServices;
	private ComponentDataService componentDataService;

	public EarningsController(EarningService earningService, EarningExcelDataServices earningExcelDataServices,
			ComponentDataService componentDataService) {
		this.earningService = earningService;
		this.earningExcelDataServices = earningExcelDataServices;
		this.componentDataService = componentDataService;

	}

	@PostMapping(value = "/data/{bu}/{payPeriod}", consumes = "multipart/form-data", produces = "application/json")
	public ResponseEntity<Map<String, Object>> getData(@PathVariable("payPeriod") String payPeriod,
			@PathVariable("bu") String bu, @RequestParam(required = false) String department,
			@RequestParam(required = false) String employeeName, @RequestParam(required = false) String status,
			@RequestParam(required = false) String employeeId,
			@RequestParam(name = "page") @NotNull(message = "Page cannot be null") int page,
			@RequestParam(name = "size") @NotNull(message = "Size cannot be null") int size,
			@RequestParam(name = "sortField") @NotBlank(message = "Sort field cannot be blank") String sortField,
			@RequestParam(name = "sortOrder") @NotBlank(message = "Sort Order cannot be blank") String sortOrder) {
//		if(department!=null || employeeName!=null || status!=null || employeeId!=null)
//			page=0;
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortOrder), sortField));
		System.err.println(page);
		System.err.println(size);
		Page<Map<String, Object>> pageData = earningService.getData(payPeriod, bu, department, employeeName, status,
				employeeId, pageable);
		Map<String, Object> response = new HashMap<>();
		response.put("data", pageData.getContent());
		response.put("totalElements", pageData.getTotalElements());
		response.put("totalPages", pageData.getTotalPages());
		response.put("currentPage", pageData.getNumber());
		response.put("pageSize", pageData.getSize());
		return ResponseEntity.ok(response);
	}

	@GetMapping(value = "/businessUnit/{bu}/{empCode}/{componentid}", produces = "application/json")
	public List<Map<String, Object>> businessunitComponent(@PathVariable("bu") String bu,
			@PathVariable("empCode") String empCode, @PathVariable("componentid") String componentid) {
		return earningService.businessunitComponent(bu, empCode, componentid);
	}

	@PostMapping(value = "/earninginsert", produces = "application/json")
	public ResponseEntity<ErrorResponse> insertEarnings(@RequestBody EarningsRequestDto1 obj) {
		return earningService.insertEarnings(obj);
	}

	@PostMapping(value = "/insertMultiple", produces = "application/json")
	public ResponseEntity<ErrorResponse> MultipleInsertEarning(@RequestBody List<EarningsRequestDto1> obj) {
		return earningService.MultipleInsertEarning(obj);
	}

	@GetMapping(value = "/earning/{payPeriod}/{bu}/{employeeid}", produces = "application/json")
	public ResponseEntity<List<Map<String, Object>>> getFetch(@PathVariable("payPeriod") String payPeriod,
			@PathVariable("bu") String bu, @PathVariable("employeeid") String employeeid) {
		List<Map<String, Object>> data = earningService.getFetch(payPeriod, bu, employeeid);
		return ResponseEntity.ok(data);
	}

	@PostMapping(value = "/export", consumes = "multipart/form-data", produces = "application/json")
	public ResponseEntity<byte[]> exportToExcel(@RequestParam("payPeriod") String payPeriod,
			@RequestParam("businessUnitId") int businessUnitId, @RequestParam("empCode") String empCode,
			@RequestParam("componentId") String componentId) {
		try {
			byte[] excelFileBytes = earningExcelDataServices.generateExcelFile(payPeriod, businessUnitId, empCode,
					componentId);
			String filename = "earning_employee_data_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date(0))
					+ ".xlsx";
			HttpHeaders headers = new HttpHeaders();
			headers.setContentDispositionFormData("attachment", filename);
			headers.setContentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
			return ResponseEntity.ok().headers(headers).body(excelFileBytes); 
		} catch (Exception e) {
			//System.err.println("step 1");
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
  
	@GetMapping(value = "/payperiodDate", produces = "application/json")
	public List<Map<String, Object>> getDistinctPayperiod() {
		return earningService.getDistinctPayPeriod();
	}

	@PostMapping(value = "/businessunit", produces = "application/json")
	public List<Map<String, Object>> getDistinctBusinessUnits(@RequestParam("empCode") int empCode) {
		return earningService.getDistinctBusinessUnits(empCode);
	}

	@GetMapping(value = "/department", produces = "application/json")
	public List<Map<String, Object>> getDepartment() {
		return earningService.getDepartment();
	}

	@GetMapping(value = "/status", produces = "application/json")
	public List<Map<String, Object>> getStatus() {
		return earningService.getStatus();
	}

	@PostMapping(value = "/payperiodstatus", produces = "application/json")
	public Object getPayPeriodStatus(@RequestParam("payPeriod") String payPeriod, @RequestParam("bu") int bu) {
		return earningService.getPayPeriodStatus(payPeriod, bu);
	}

	@PostMapping(value = "/login", consumes = "multipart/form-data", produces = "application/json")
	public ResponseEntity<Object> getLogin(@RequestParam("empCode") String empCode,
			@RequestParam("password") String password) {
		ResponseEntity<Object> employeeData = earningService.getLogin(empCode, password);
		return ResponseEntity.ok(employeeData);
	}

	@PostMapping("/components/total")
	public List<Map<String, Object>> getComponentCount(@RequestParam("transactionid") int transactionId,
			@RequestParam("businessunitid") int businessUnitId, @RequestParam("COMPONENTTYPEID") int COMPONENTTYPEID,
			@RequestParam("EMPLOYEEID") int EMPLOYEEID) {
		return earningService.getComponentValueCountWithEmployeeCountFilter(transactionId, businessUnitId,
				COMPONENTTYPEID, EMPLOYEEID);
	}

	@PostMapping(value = "/uploads", produces = "application/json")
	public ResponseEntity<Response> uploadEarningsFile(@RequestParam("file") MultipartFile file,
	        @RequestParam("componenttypeid") int componenttypeid, @RequestParam("payPeriod") int payPeriod,
	        @RequestParam("bu") int bu, @RequestParam("empCode") int empCode,@RequestParam("count")int count) throws IOException {
System.err.println(count);
	    List<FailedEmployee> failedList = new ArrayList<>();
	    componentDataService.processExcelFile(file.getInputStream(), componenttypeid, payPeriod, bu, failedList, file, empCode,count);
	    
	    String status = failedList.isEmpty() ? "true" : "false";
	    String message = failedList.isEmpty() ? "File uploaded and data processed successfully."
	            : "File uploaded, but some entries failed to process.";

	    return ResponseEntity.ok(new Response(status, message, String.valueOf(HttpStatus.OK.value()), failedList));
	}



	@PostMapping(value = "/fileStatus", produces = "application/json")
	public ResponseEntity<List<EmployeeEarningReport>>reportStatus() {
		return ResponseEntity.ok(earningService.reportStatus());
	}

}
