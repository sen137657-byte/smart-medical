package com.smartmedical.controller.api;

import com.smartmedical.common.RoleConst;
import com.smartmedical.common.Result;
import com.smartmedical.dto.MedicalRecordCreateReq;
import com.smartmedical.dto.MedicalRecordUpdateReq;
import com.smartmedical.entity.SysUser;
import com.smartmedical.service.MedicalRecordService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/medicalRecord")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    private SysUser mustLogin(HttpSession session) {
        SysUser u = (SysUser) session.getAttribute("loginUser");
        if (u == null) throw new RuntimeException("未登录");
        return u;
    }

    @PostMapping
    public Result<Long> create(@RequestBody MedicalRecordCreateReq req, HttpSession session) {
        SysUser u = mustLogin(session);
        if (!RoleConst.DOCTOR.equals(u.getRole())) {
            return Result.fail("仅医生可创建病历");
        }
        Long id = medicalRecordService.createByDoctor(u.getId(), req);
        return Result.ok(id);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody MedicalRecordUpdateReq req,
                               HttpSession session) {
        SysUser u = mustLogin(session);
        if (!RoleConst.DOCTOR.equals(u.getRole())) {
            return Result.fail("仅医生可修改病历");
        }
        medicalRecordService.updateByDoctor(u.getId(), id, req);
        return Result.ok();
    }

    @PostMapping("/{id}/archive")
    public Result<Void> archive(@PathVariable Long id, HttpSession session) {
        SysUser u = mustLogin(session);
        if (!RoleConst.DOCTOR.equals(u.getRole())) {
            return Result.fail("仅医生可归档病历");
        }
        medicalRecordService.archiveByDoctor(u.getId(), id);
        return Result.ok();
    }

    @GetMapping("/doctor")
    public Result<?> listDoctor(HttpSession session) {
        SysUser u = mustLogin(session);
        if (!RoleConst.DOCTOR.equals(u.getRole())) {
            return Result.fail("仅医生可访问");
        }
        return Result.ok(medicalRecordService.listForDoctor(u.getId()));
    }

    @GetMapping("/patient")
    public Result<?> listPatient(HttpSession session) {
        SysUser u = mustLogin(session);
        if (!RoleConst.PATIENT.equals(u.getRole())) {
            return Result.fail("仅患者可访问");
        }
        return Result.ok(medicalRecordService.listForPatient(u.getId()));
    }

    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id, HttpSession session) {
        SysUser u = mustLogin(session);
        return Result.ok(medicalRecordService.detail(u.getId(), u.getRole(), id));
    }
}