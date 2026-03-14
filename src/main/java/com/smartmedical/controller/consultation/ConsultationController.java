package com.smartmedical.controller.consultation;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartmedical.common.ConsultationStatus;
import com.smartmedical.common.Result;
import com.smartmedical.dto.ConsultationCreateReq;
import com.smartmedical.dto.ConsultationReplyReq;
import com.smartmedical.entity.Consultation;
import com.smartmedical.entity.ConsultationReply;
import com.smartmedical.entity.DoctorProfile;
import com.smartmedical.entity.PatientProfile;
import com.smartmedical.entity.SysUser;
import com.smartmedical.service.ConsultationReplyService;
import com.smartmedical.service.ConsultationService;
import com.smartmedical.service.DoctorProfileService;
import com.smartmedical.service.PatientProfileService;
import com.smartmedical.vo.ConsultationDoctorVO;
import com.smartmedical.vo.ConsultationPatientVO;
import com.smartmedical.vo.ConsultationReplyVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 在线问诊 REST API
 * - 患者：发起问诊、查看我的问诊列表（VO：带医生姓名+状态中文）
 * - 医生：查看问诊列表（VO：带患者姓名+状态中文）、回复问诊、查看回复列表（VO）
 */
@RestController
@RequestMapping("/api/consultation")
public class ConsultationController {

    private final ConsultationService consultationService;
    private final ConsultationReplyService consultationReplyService;
    private final PatientProfileService patientProfileService;
    private final DoctorProfileService doctorProfileService;

    public ConsultationController(ConsultationService consultationService,
                                  ConsultationReplyService consultationReplyService,
                                  PatientProfileService patientProfileService,
                                  DoctorProfileService doctorProfileService) {
        this.consultationService = consultationService;
        this.consultationReplyService = consultationReplyService;
        this.patientProfileService = patientProfileService;
        this.doctorProfileService = doctorProfileService;
    }

    /**
     * 1) 患者发起问诊
     * POST /api/consultation
     */
    @PostMapping
    public Result<?> create(@RequestBody ConsultationCreateReq req, HttpSession session) {
        SysUser loginUser = (SysUser) session.getAttribute("loginUser");
        if (loginUser == null) return Result.fail("未登录");
        if (!"PATIENT".equals(loginUser.getRole())) return Result.fail("无权限：仅患者可发起问诊");

        if (req.getDoctorId() == null) return Result.fail("doctorId 不能为空");
        if (req.getContent() == null || req.getContent().trim().isEmpty()) return Result.fail("病情描述不能为空");
        if (req.getContent().length() > 1000) return Result.fail("病情描述过长（最多1000字）");

        PatientProfile patient = patientProfileService.getOne(
                new LambdaQueryWrapper<PatientProfile>()
                        .eq(PatientProfile::getUserId, loginUser.getId())
                        .last("limit 1")
        );
        if (patient == null) return Result.fail("未绑定患者档案，无法问诊");

        DoctorProfile doctor = doctorProfileService.getById(req.getDoctorId());
        if (doctor == null) return Result.fail("医生不存在");

        Consultation c = new Consultation();
        c.setPatientId(patient.getId());        // patient_profile.id
        c.setDoctorId(req.getDoctorId());       // doctor_profile.id
        c.setContent(req.getContent().trim());
        c.setStatus("WAITING");
        c.setCreateTime(LocalDateTime.now());

        consultationService.save(c);
        return Result.ok(c);
    }

    /**
     * 2) 患者查看我的问诊列表（VO：带 doctorName + statusText）
     * GET /api/consultation/patient
     */
    @GetMapping("/patient")
    public Result<?> listForPatient(HttpSession session) {
        SysUser loginUser = (SysUser) session.getAttribute("loginUser");
        if (loginUser == null) return Result.fail("未登录");
        if (!"PATIENT".equals(loginUser.getRole())) return Result.fail("无权限：仅患者可查看");

        PatientProfile patient = patientProfileService.getOne(
                new LambdaQueryWrapper<PatientProfile>()
                        .eq(PatientProfile::getUserId, loginUser.getId())
                        .last("limit 1")
        );
        if (patient == null) return Result.fail("未绑定患者档案");

        // 1) 查我的问诊列表
        List<Consultation> list = consultationService.list(
                new LambdaQueryWrapper<Consultation>()
                        .eq(Consultation::getPatientId, patient.getId())
                        .orderByDesc(Consultation::getCreateTime)
        );
        if (list == null || list.isEmpty()) {
            return Result.ok(new ArrayList<>());
        }

        // 2) 批量取 doctorId（doctor_profile.id）
        List<Long> doctorIds = list.stream()
                .map(Consultation::getDoctorId)
                .distinct()
                .toList();

        // 3) 批量查 doctor_profile，组装 doctorId -> doctorName
        List<DoctorProfile> doctors = doctorProfileService.list(
                new LambdaQueryWrapper<DoctorProfile>().in(DoctorProfile::getId, doctorIds)
        );
        Map<Long, String> doctorNameMap = doctors.stream()
                .collect(Collectors.toMap(
                        DoctorProfile::getId,
                        DoctorProfile::getName,
                        (a, b) -> a
                ));

        // 4) 转 VO
        List<ConsultationPatientVO> voList = new ArrayList<>();
        for (Consultation c : list) {
            ConsultationPatientVO vo = new ConsultationPatientVO();
            vo.setId(c.getId());
            vo.setDoctorId(c.getDoctorId());
            vo.setDoctorName(doctorNameMap.getOrDefault(c.getDoctorId(), "未知医生"));
            vo.setContent(c.getContent());
            vo.setStatus(c.getStatus());
            vo.setStatusText(ConsultationStatus.textOf(c.getStatus()));
            vo.setCreateTime(c.getCreateTime());
            voList.add(vo);
        }

        return Result.ok(voList);
    }

    /**
     * 3) 医生查看我的问诊列表（实体版，可选保留）
     * GET /api/consultation/doctor?status=WAITING
     */
    @GetMapping("/doctor")
    public Result<?> listForDoctor(@RequestParam(required = false) String status, HttpSession session) {
        SysUser loginUser = (SysUser) session.getAttribute("loginUser");
        if (loginUser == null) return Result.fail("未登录");
        if (!"DOCTOR".equals(loginUser.getRole())) return Result.fail("无权限：仅医生可查看");

        DoctorProfile doctor = doctorProfileService.getOne(
                new LambdaQueryWrapper<DoctorProfile>()
                        .eq(DoctorProfile::getUserId, loginUser.getId())
                        .last("limit 1")
        );
        if (doctor == null) return Result.fail("未绑定医生档案");

        LambdaQueryWrapper<Consultation> qw = new LambdaQueryWrapper<Consultation>()
                .eq(Consultation::getDoctorId, doctor.getId())
                .orderByDesc(Consultation::getCreateTime);

        if (status != null && !status.trim().isEmpty()) {
            qw.eq(Consultation::getStatus, status.trim());
        }

        List<Consultation> list = consultationService.list(qw);
        return Result.ok(list);
    }

    /**
     * 4) 医生回复问诊
     * POST /api/consultation/{id}/reply
     */
    @PostMapping("/{id}/reply")
    public Result<?> reply(@PathVariable Long id,
                           @RequestBody ConsultationReplyReq req,
                           HttpSession session) {
        SysUser loginUser = (SysUser) session.getAttribute("loginUser");
        if (loginUser == null) return Result.fail("未登录");
        if (!"DOCTOR".equals(loginUser.getRole())) return Result.fail("无权限：仅医生可回复");

        if (req.getReplyContent() == null || req.getReplyContent().trim().isEmpty()) {
            return Result.fail("回复内容不能为空");
        }
        if (req.getReplyContent().length() > 1000) return Result.fail("回复内容过长（最多1000字）");

        DoctorProfile doctor = doctorProfileService.getOne(
                new LambdaQueryWrapper<DoctorProfile>()
                        .eq(DoctorProfile::getUserId, loginUser.getId())
                        .last("limit 1")
        );
        if (doctor == null) return Result.fail("未绑定医生档案");

        Consultation c = consultationService.getById(id);
        if (c == null) return Result.fail("问诊不存在");
        if (!doctor.getId().equals(c.getDoctorId())) return Result.fail("不能回复他人的问诊");

        if (!"WAITING".equals(c.getStatus())) return Result.fail("仅 WAITING 状态可回复");

        ConsultationReply reply = new ConsultationReply();
        reply.setConsultationId(c.getId());
        reply.setDoctorId(doctor.getId()); // doctor_profile.id
        reply.setReplyContent(req.getReplyContent().trim());
        reply.setCreateTime(LocalDateTime.now());
        consultationReplyService.save(reply);

        c.setStatus("REPLIED");
        consultationService.updateById(c);

        return Result.ok(reply);
    }

    /**
     * 5) 查看某个问诊的回复列表（VO版：包含 doctorName/content/replyTime）
     * GET /api/consultation/{id}/replies
     */
    @GetMapping("/{id}/replies")
    public Result<?> replies(@PathVariable Long id, HttpSession session) {

        SysUser loginUser = (SysUser) session.getAttribute("loginUser");
        if (loginUser == null) return Result.fail("未登录");

        Consultation c = consultationService.getById(id);
        if (c == null) return Result.fail("问诊不存在");

        // 权限校验
        if ("PATIENT".equals(loginUser.getRole())) {

            PatientProfile patient = patientProfileService.getOne(
                    new LambdaQueryWrapper<PatientProfile>()
                            .eq(PatientProfile::getUserId, loginUser.getId())
                            .last("limit 1")
            );
            if (patient == null) return Result.fail("未绑定患者档案");
            if (!patient.getId().equals(c.getPatientId()))
                return Result.fail("无权限查看该问诊");

        } else if ("DOCTOR".equals(loginUser.getRole())) {

            DoctorProfile doctor = doctorProfileService.getOne(
                    new LambdaQueryWrapper<DoctorProfile>()
                            .eq(DoctorProfile::getUserId, loginUser.getId())
                            .last("limit 1")
            );
            if (doctor == null) return Result.fail("未绑定医生档案");
            if (!doctor.getId().equals(c.getDoctorId()))
                return Result.fail("无权限查看该问诊");

        } else {
            return Result.fail("无权限");
        }

        List<ConsultationReplyVO> voList = consultationReplyService.listReplyVO(id);
        return Result.ok(voList);
    }

    /**
     * 6) 医生查看我的问诊列表（VO：带患者姓名 + 状态中文），并批量查患者名避免 N+1
     * GET /api/consultation/doctor/vo?status=WAITING
     */
    @GetMapping("/doctor/vo")
    public Result<?> listForDoctorVO(@RequestParam(required = false) String status, HttpSession session) {
        SysUser loginUser = (SysUser) session.getAttribute("loginUser");
        if (loginUser == null) return Result.fail("未登录");
        if (!"DOCTOR".equals(loginUser.getRole())) return Result.fail("无权限：仅医生可查看");

        DoctorProfile doctor = doctorProfileService.getOne(
                new LambdaQueryWrapper<DoctorProfile>()
                        .eq(DoctorProfile::getUserId, loginUser.getId())
                        .last("limit 1")
        );
        if (doctor == null) return Result.fail("未绑定医生档案");

        // 1) 先查问诊列表
        LambdaQueryWrapper<Consultation> qw = new LambdaQueryWrapper<Consultation>()
                .eq(Consultation::getDoctorId, doctor.getId())
                .orderByDesc(Consultation::getCreateTime);

        if (status != null && !status.trim().isEmpty()) {
            qw.eq(Consultation::getStatus, status.trim());
        }

        List<Consultation> list = consultationService.list(qw);
        if (list == null || list.isEmpty()) {
            return Result.ok(new ArrayList<>());
        }

        // 2) 批量取 patientId（patient_profile.id）
        List<Long> patientIds = list.stream()
                .map(Consultation::getPatientId)
                .distinct()
                .toList();

        // 3) 批量查患者档案，组装 patientId -> patientName
        List<PatientProfile> patients = patientProfileService.list(
                new LambdaQueryWrapper<PatientProfile>().in(PatientProfile::getId, patientIds)
        );

        Map<Long, String> patientNameMap = patients.stream()
                .collect(Collectors.toMap(
                        PatientProfile::getId,
                        PatientProfile::getName,
                        (a, b) -> a
                ));

        // 4) 转 VO
        List<ConsultationDoctorVO> voList = new ArrayList<>();
        for (Consultation c : list) {
            ConsultationDoctorVO vo = new ConsultationDoctorVO();
            vo.setId(c.getId());
            vo.setPatientId(c.getPatientId());
            vo.setPatientName(patientNameMap.getOrDefault(c.getPatientId(), "未知患者"));

            vo.setDoctorId(c.getDoctorId());
            vo.setContent(c.getContent());
            vo.setStatus(c.getStatus());
            vo.setStatusText(ConsultationStatus.textOf(c.getStatus()));
            vo.setCreateTime(c.getCreateTime());

            voList.add(vo);
        }

        return Result.ok(voList);
    }
}