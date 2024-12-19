package com.earnings.payadjustements.repository;

import java.util.List;

public interface EarningDataRepository {
	   List<Object[]> fetchEmployeeData(String payPeriod, int businessUnitId);
	    String[] fetchColumnNames(String payPeriod, int businessUnitId);
	    List<Object[]> fetchPayComponentData(int businessUnitId, String empCode, String componentId);
	    List<Object[]> earningsQueryForEmployee(int employeeId, String payPeriod, int businessUnitId);
}