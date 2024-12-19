package com.earnings.payadjustements.entity;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(schema="hcladm_prod",name = "tbl_pay_component")
public class PayComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PAYCOMPONENTID", nullable = false)
    private Long payComponentId;

    @Column(name = "NAME", length = 45)
    private String name;

    @Column(name = "DISPLAYNAME", length = 45)
    private String displayName;

    @Column(name = "COMPONENTTYPEID", columnDefinition = "int(10) unsigned default 0")
    private Integer componentTypeId;

    @Column(name = "ALTERNATIVECOMPONENTTYPEID", columnDefinition = "int(10) unsigned default 0")
    private Integer alternativeComponentTypeId;

    @Column(name = "INDEXNO", columnDefinition = "int(10) unsigned default 0")
    private Integer indexNo;

    @Column(name = "REPORTINDEXNO", columnDefinition = "int(10) unsigned default 0")
    private Integer reportIndexNo;

    @Column(name = "PAYROLLINDEXNO", columnDefinition = "int(10) unsigned default 0")
    private Integer payrollIndexNo;

    @Column(name = "FORMULA", length = 500)
    private String formula;

    @Column(name = "FORMULACOMPONENTID", length = 250)
    private String formulaComponentId;

    @Column(name = "CODE", length = 45)
    private String code;

    @Column(name = "`GROUP`", length = 45, columnDefinition = "varchar(45) default '0'")
    private String group;

    @Column(name = "GLCODE", length = 20)
    private String glCode;

    @Column(name = "STATUS", columnDefinition = "int(10) unsigned default 1002")
    private Integer status;

    @Column(name = "CREATEDBY", columnDefinition = "int(10) unsigned default 0")
    private Integer createdBy;

    @Column(name = "DATECREATED", columnDefinition = "datetime default '0000-00-00 00:00:00'")
    private LocalDateTime dateCreated;

    @Column(name = "MODIFIEDBY", columnDefinition = "int(10) unsigned default 0")
    private Integer modifiedBy;

    @Column(name = "DATEMODIFIED", columnDefinition = "datetime default '0000-00-00 00:00:00'")
    private LocalDateTime dateModified;

    @Column(name = "LOGID", columnDefinition = "bigint(20) unsigned default 0")
    private Long logId;

    @Column(name = "LUPDATE", nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP")
    private LocalDateTime lastUpdate;

    // Getters and Setters
    public Long getPayComponentId() {
        return payComponentId;
    }

    public void setPayComponentId(Long payComponentId) {
        this.payComponentId = payComponentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Integer getComponentTypeId() {
        return componentTypeId;
    }

    public void setComponentTypeId(Integer componentTypeId) {
        this.componentTypeId = componentTypeId;
    }

    public Integer getAlternativeComponentTypeId() {
        return alternativeComponentTypeId;
    }

    public void setAlternativeComponentTypeId(Integer alternativeComponentTypeId) {
        this.alternativeComponentTypeId = alternativeComponentTypeId;
    }

    public Integer getIndexNo() {
        return indexNo;
    }

    public void setIndexNo(Integer indexNo) {
        this.indexNo = indexNo;
    }

    public Integer getReportIndexNo() {
        return reportIndexNo;
    }

    public void setReportIndexNo(Integer reportIndexNo) {
        this.reportIndexNo = reportIndexNo;
    }

    public Integer getPayrollIndexNo() {
        return payrollIndexNo;
    }

    public void setPayrollIndexNo(Integer payrollIndexNo) {
        this.payrollIndexNo = payrollIndexNo;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public String getFormulaComponentId() {
        return formulaComponentId;
    }

    public void setFormulaComponentId(String formulaComponentId) {
        this.formulaComponentId = formulaComponentId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getGlCode() {
        return glCode;
    }

    public void setGlCode(String glCode) {
        this.glCode = glCode;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Integer getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(Integer modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public LocalDateTime getDateModified() {
        return dateModified;
    }

    public void setDateModified(LocalDateTime dateModified) {
        this.dateModified = dateModified;
    }

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
