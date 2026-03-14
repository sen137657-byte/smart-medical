package com.smartmedical.vo;

/**
 * 医生下拉框展示对象
 */
public class DoctorOptionVO {

    private Long id;            // 医生ID
    private String name;        // 医生姓名
    private Long deptId;        // 科室ID
    private String deptName;    // 科室名称
    private String specialty;   // 擅长方向

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }

    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
}