package com.earnings.payadjustements.service;
 
import java.sql.ResultSetMetaData;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.earnings.payadjustements.repository.EarningDataRepository;
 
@Repository
public class EarningDataRepositoryImpl implements EarningDataRepository {
 
	@Autowired
	private JdbcTemplate jdbcTemplate;
 
//	private static final String EMPLOYEE_QUERY = "SELECT DISTINCT a.employeesequenceno AS employeeid, "
//			+ "a.callname AS employeeName, c.name AS department, d.name AS designation, "
//			+ "e.name AS status, g.payperiod AS payperioddate, f.name AS businessUnit "
//			+ "FROM hclhrm_prod.tbl_employee_primary a "
//			+ "LEFT JOIN hclhrm_prod.tbl_employee_professional_details b ON a.employeeid = b.employeeid "
//			+ "LEFT JOIN hcladm_prod.tbl_department c ON c.departmentid = b.departmentid "
//			+ "LEFT JOIN hcladm_prod.tbl_designation d ON d.designationid = b.designationid "
//			+ "LEFT JOIN hcladm_prod.tbl_status_codes e ON e.status = a.status "
//			+ "LEFT JOIN hcladm_prod.tbl_businessunit f ON a.companyid = f.businessunitid "
//			+ "JOIN hclhrm_prod.tbl_employee_payperiod_details g ON a.employeesequenceno = g.employeesequenceno "
//			+ "WHERE g.payperiod = ? AND f.businessunitid = ? AND f.status = 1001";
	
	
	private static final String EMPLOYEE_QUERY = 
	        "SELECT  a.employeesequenceno AS employeeid, "
	        + "a.callname AS employeeName, c.name AS department, "
	        + "d.name AS designation, e.name AS status, l.transactionduration as payperioddate, f.name AS businessUnit "
	        + "FROM hclhrm_prod.tbl_employee_primary a "
	        + "LEFT JOIN hclhrm_prod.tbl_employee_professional_details b ON a.employeeid = b.employeeid "
	        + "LEFT JOIN hcladm_prod.tbl_department c ON c.departmentid = b.departmentid "
	        + "LEFT JOIN hcladm_prod.tbl_designation d ON d.designationid = b.designationid "
	        + "LEFT JOIN hcladm_prod.tbl_status_codes e ON e.status = a.status "
	        + "LEFT JOIN hcladm_prod.tbl_businessunit f ON a.companyid = f.businessunitid "
	        + "LEFT JOIN hcladm_prod.tbl_transaction_dates l ON l.businessunitid = f.businessunitid AND l.TRANSACTIONTYPEID = 1 "
	        + "WHERE f.businessunitid = ? AND e.status IN (1001, 1092, 1401) AND l.transactionduration = ? "
	        + "UNION ALL "
	        + "SELECT  a.employeesequenceno AS employeeid, "
	        + "a.callname AS employeeName, c.name AS department, "
	        + "d.name AS designation, e.name AS status, l.transactionduration as payperioddate, f.name AS businessUnit "
	        + "FROM hclhrm_prod.tbl_employee_primary a "
	        + "LEFT JOIN hclhrm_prod.tbl_employee_professional_details b ON a.employeeid = b.employeeid "
	        + "LEFT JOIN hcladm_prod.tbl_department c ON c.departmentid = b.departmentid "
	        + "LEFT JOIN hcladm_prod.tbl_designation d ON d.designationid = b.designationid "
	        + "LEFT JOIN hcladm_prod.tbl_status_codes e ON e.status = a.status "
	        + "LEFT JOIN hcladm_prod.tbl_businessunit f ON a.companyid = f.businessunitid "
	        + "LEFT JOIN hclhrm_prod.tbl_employee_hractions k ON k.employeeid = a.employeeid "
	        + "LEFT JOIN hcladm_prod.tbl_transaction_dates l ON l.businessunitid = f.businessunitid AND l.TRANSACTIONTYPEID = 1 "
	        + "WHERE l.transactionduration = ? AND f.businessunitid = ? "
	        + "AND a.status = 1061 AND k.LASTWORKINGDATE >= l.fromdate "
	        + "AND k.LASTWORKINGDATE <= l.todate";

	

 
	private static final String PAY_COMPONENT_QUERY = "SELECT PAYCOMPONENTID AS paycomponentid, name "
			+ "FROM hcladm_prod.tbl_pay_component " + "WHERE COMPONENTTYPEID = ? AND STATUS = 1001 "
			+ "AND PAYCOMPONENTID IN (" + "SELECT PAYCOMPONENTID FROM hcladm_prod.tbl_businessunit_pay_component "
			+ "WHERE BUSINESSUNITID = ? AND STATUS = 1001 " + "AND PAYCOMPONENTID NOT IN ("
			+ "SELECT PAYCOMPONENTID FROM hclhrm_prod_others.tbl_businessunit_pay_component_rights "
			+ "WHERE BUSINESSUNITID = ? AND STATUS = 1002 AND EMPLOYEEID = ?)) " + "ORDER BY PAYCOMPONENTID";
 
	private static final String EARNINGS_QUERY="SELECT componentid, componentvalue\r\n"
			+ "FROM hclhrm_prod.tbl_employee_earnings a\r\n"
			+ "left join hclhrm_prod.tbl_employee_primary b on b.employeeid=a.employeeid\r\n"
			+ "WHERE businessunitid = ? AND transactionid = ? AND b.employeesequenceno = ?";
 
	public List<Object[]> fetchEmployeeData(String payPeriod, int businessUnitId) {
	    try {
	        List<Object[]> results = jdbcTemplate.query(EMPLOYEE_QUERY, 
	            new Object[] { businessUnitId,payPeriod, payPeriod, businessUnitId }, 
	            (rs, rowNum) -> {
	                Object[] row = new Object[7];
	                row[0] = rs.getInt("employeeid");
	                row[1] = rs.getString("employeeName") != null ? rs.getString("employeeName") : "N/A";
	                row[2] = rs.getString("department") != null ? rs.getString("department") : "N/A";
	                row[3] = rs.getString("designation") != null ? rs.getString("designation") : "N/A";
	                row[4] = rs.getString("status") != null ? rs.getString("status") : "N/A";
	                row[5] = rs.getString("payperioddate") != null ? rs.getString("payperioddate") : "N/A";
	                row[6] = rs.getString("businessUnit") != null ? rs.getString("businessUnit") : "N/A";
	                return row;
	            });
	        return results;
	    } catch (DataAccessException e) {
	        return Collections.emptyList();
	    }
	}

	@Override
	public String[] fetchColumnNames(String payPeriod, int businessUnitId) {
	    String[] headers = jdbcTemplate.query(EMPLOYEE_QUERY, new Object[] {businessUnitId,payPeriod, payPeriod, businessUnitId }, rs -> {
	        ResultSetMetaData metaData = rs.getMetaData();
	        int columnCount = metaData.getColumnCount();
	        String[] columnNames = new String[columnCount];
	        for (int i = 1; i <= columnCount; i++) {
	            columnNames[i - 1] = metaData.getColumnLabel(i);
	        }
	        return columnNames;
	    });
	    return headers;
	}

	@Override
	public List<Object[]> fetchPayComponentData(int businessUnitId, String empCode, String componentId) {
		try {
			return jdbcTemplate.query(PAY_COMPONENT_QUERY,
					new Object[] { componentId, businessUnitId, businessUnitId, empCode },
					(rs, rowNum) -> new Object[] { rs.getInt("paycomponentid"), rs.getString("name") });
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}
 
	@Override
	public List<Object[]> earningsQueryForEmployee(int employeeId, String payPeriod, int businessUnitId) {
		//System.err.println(employeeId);
		return jdbcTemplate.query(EARNINGS_QUERY, new Object[] { businessUnitId, payPeriod, employeeId },
				(rs, rowNum) -> new Object[] { rs.getInt("componentid"), rs.getString("componentvalue") });
	}
}
