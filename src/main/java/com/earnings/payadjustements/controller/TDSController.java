package com.earnings.payadjustements.controller;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

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
import com.earnings.payadjustements.entity.TdsDto1;
import com.earnings.payadjustements.service.TDSService;

@RestController
public class TDSController {
	private TDSService tdsService;

	public TDSController(TDSService tdsService) {
		this.tdsService = tdsService;
	}

	@GetMapping(value = "/tds/{payPeriod}/{bu}/{employeeid}", produces = "application/json")
	public ResponseEntity<List<Map<String, Object>>> getTds(@PathVariable("payPeriod") String payPeriod,
			@PathVariable("bu") String bu, @PathVariable("employeeid") String employeeid) {
		List<Map<String, Object>> data = tdsService.getTds(payPeriod, bu, employeeid);
		return ResponseEntity.ok(data);
	}

//	@PostMapping(value = "/tdsinsert", produces = "application/json")
//	public Map<String, Object> insertTds(@RequestBody TdsDto tdsDto) {
//		return tdsService.insertTds(tdsDto);
//	}
	@PostMapping(value = "/tdsinsert", produces = "application/json")
	public ResponseEntity<ErrorResponse> tdsInsert(@RequestBody TdsDto1 obj) {
		return tdsService.tdsInsert(obj);
	}

	@PostMapping(value = "/tdsinsertMultiple", produces = "application/json")
	public ResponseEntity<ErrorResponse> MultipleInsertTds(@RequestBody List<TdsDto1> obj) {
		return tdsService.multipleInsertTds(obj);
	}

//	@PostMapping(value = "/multipletdsinsert", consumes = "application/json", produces = "application/json")
//	public List<Map<String, Object>> insertMultipleTds(@RequestBody List<TdsDto> tdsDtoList) {
//		return tdsService.insertMultipleTds(tdsDtoList);
//	}
//	@PostMapping(value = "/tdsinsertMultiple", consumes = "application/json", produces = "application/json")
//	public List<Map<String, Object>> insertMultipleTds(@RequestBody List<TdsDto> tdsDtoList) {
//	    return tdsService.insertMultipleTds(tdsDtoList);
//	}

	@PostMapping(value = "/tdsexport", consumes = "multipart/form-data", produces = "application/json")
	public ResponseEntity<byte[]> exportToExcel(@RequestParam("payPeriod") String payPeriod,
			@RequestParam("businessUnitId") int businessUnitId, @RequestParam("empCode") String empCode)
			throws IOException {
		byte[] excelFileBytes = tdsService.generateExcelFile(payPeriod, businessUnitId, empCode);
		String filename = "tds_employee_data_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date(0)) + ".xlsx";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentDispositionFormData("attachment", filename);
		headers.setContentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
		return ResponseEntity.ok().headers(headers).body(excelFileBytes);
	}

	@PostMapping("/tdsupload")
	public ResponseEntity<ErrorResponse> uploadTdsFile(@RequestParam("file") MultipartFile file) throws IOException {
		if (file.isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("true",
					"Please upload a valid Excel file.", String.valueOf(HttpStatus.BAD_REQUEST.value())));
		}
		try {
			tdsService.uploadExcelData(file); // Use MultipartFile directly
			return ResponseEntity.ok(new ErrorResponse("false", "File uploaded and data processed successfully.",
					String.valueOf(HttpStatus.OK.value())));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("true",
					"Unexpected error occurred.", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())));
		}
	}

}
