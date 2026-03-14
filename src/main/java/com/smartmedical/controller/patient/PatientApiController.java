package com.smartmedical.controller.patient;

import com.smartmedical.common.Result;
import com.smartmedical.entity.PatientProfile;
import com.smartmedical.service.PatientProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 给前端提供患者下拉/映射数据：[{id,name}]
 */
@RestController
@RequestMapping("/api/patient")
public class PatientApiController {

    private final PatientProfileService patientProfileService;

    public PatientApiController(PatientProfileService patientProfileService) {
        this.patientProfileService = patientProfileService;
    }

    @GetMapping("/options")
    public Result<?> options() {
        List<PatientProfile> patients = patientProfileService.list();

        List<PatientOption> options = new ArrayList<>();
        for (PatientProfile p : patients) {
            PatientOption o = new PatientOption();
            o.setId(p.getId());      // 注意：这里用 patient_profile.id，和 appointment.patient_id 对应
            o.setName(p.getName());
            options.add(o);
        }
        return Result.ok(options);
    }

    public static class PatientOption {
        private Long id;
        private String name;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}