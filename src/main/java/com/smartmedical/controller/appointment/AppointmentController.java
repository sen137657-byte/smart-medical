package com.smartmedical.controller.appointment;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartmedical.common.Result;
import com.smartmedical.dto.AppointmentCreateReq;
import com.smartmedical.entity.Appointment;
import com.smartmedical.entity.DoctorProfile;
import com.smartmedical.entity.DoctorSchedule;
import com.smartmedical.entity.PatientProfile;
import com.smartmedical.entity.SysUser;
import com.smartmedical.service.AppointmentService;
import com.smartmedical.service.DoctorProfileService;
import com.smartmedical.service.DoctorScheduleService;
import com.smartmedical.service.PatientProfileService;
import jakarta.servlet.http.HttpSession;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointment")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final PatientProfileService patientProfileService;
    private final DoctorProfileService doctorProfileService;
    private final DoctorScheduleService doctorScheduleService;


    public AppointmentController(AppointmentService appointmentService,
                                 PatientProfileService patientProfileService,
                                 DoctorProfileService doctorProfileService,
                                 DoctorScheduleService doctorScheduleService) {
        this.appointmentService = appointmentService;
        this.patientProfileService = patientProfileService;
        this.doctorProfileService = doctorProfileService;
        this.doctorScheduleService = doctorScheduleService;
    }

    // 1) 患者预约：POST /api/appointment
    @PostMapping
    public Result<?> create(@RequestBody AppointmentCreateReq req, HttpSession session) {
        SysUser loginUser = (SysUser) session.getAttribute("loginUser");
        if (loginUser == null) return Result.fail("未登录");
        if (!"PATIENT".equals(loginUser.getRole())) return Result.fail("无权限：仅患者可预约");

        if (req.getDoctorId() == null || req.getAppointmentTime() == null) {
            return Result.fail("doctorId 与 appointmentTime 不能为空");
        }
        if (req.getAppointmentTime().isBefore(LocalDateTime.now())) {
            return Result.fail("预约时间不能早于当前时间");
        }

        // 取患者档案（用 userId 绑定）
        PatientProfile patient = patientProfileService.getOne(
                new LambdaQueryWrapper<PatientProfile>()
                        .eq(PatientProfile::getUserId, loginUser.getId())
                        .last("limit 1")
        );
        if (patient == null) return Result.fail("未绑定患者档案，无法预约");

        // 校验医生存在
        DoctorProfile doctor = doctorProfileService.getById(req.getDoctorId());
        if (doctor == null) return Result.fail("医生不存在");

        // 排班存在性校验（appointment_time 必须来自 doctor_schedule 且为 OPEN）
        // 约定：appointment_time 保存 slot 的开始时间，如 2026-02-26 09:00:00
        LocalDate workDate = req.getAppointmentTime().toLocalDate();
        String startHHmm = req.getAppointmentTime().toLocalTime().toString();
        startHHmm = (startHHmm.length() >= 5) ? startHHmm.substring(0, 5) : startHHmm; // 统一成 HH:mm

        DoctorSchedule schedule = doctorScheduleService.getOne(
                new LambdaQueryWrapper<DoctorSchedule>()
                        .eq(DoctorSchedule::getDoctorId, req.getDoctorId())
                        .eq(DoctorSchedule::getWorkDate, workDate)
                        .eq(DoctorSchedule::getStatus, "OPEN")
                        .likeRight(DoctorSchedule::getTimeSlot, startHHmm) // time_slot 以“09:00”开头
                        .last("limit 1")
        );
        if (schedule == null) {
            return Result.fail("该医生当天无此排班时间段，请从可预约时间段中选择");
        }

        // 防重复：同一医生 + 同一时间 + BOOKED 已存在则拒绝
        long cnt = appointmentService.count(
                new LambdaQueryWrapper<Appointment>()
                        .eq(Appointment::getDoctorId, req.getDoctorId())
                        .eq(Appointment::getAppointmentTime, req.getAppointmentTime())
                        .eq(Appointment::getStatus, Appointment.STATUS_BOOKED)
        );
        if (cnt > 0) return Result.fail("该医生该时间段已被预约，请换个时间");

        // 防重复：同一患者 + 同一时间 + BOOKED 已存在则拒绝
        long patientCnt = appointmentService.count(
                new LambdaQueryWrapper<Appointment>()
                        .eq(Appointment::getPatientId, patient.getId())
                        .eq(Appointment::getAppointmentTime, req.getAppointmentTime())
                        .eq(Appointment::getStatus, Appointment.STATUS_BOOKED)
        );
        if (patientCnt > 0) {
            return Result.fail("您在该时间段已有预约，请勿重复预约");
        }

        Appointment appt = new Appointment();
        appt.setPatientId(patient.getId());
        appt.setDoctorId(req.getDoctorId());
        appt.setAppointmentTime(req.getAppointmentTime());
        appt.setStatus(Appointment.STATUS_BOOKED);
        appt.setCreateTime(LocalDateTime.now());

        appointmentService.save(appt);
        return Result.ok(appt);
    }

    // 2) 患者查看我的预约：GET /api/appointment/patient
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

        List<Appointment> list = appointmentService.list(
                new LambdaQueryWrapper<Appointment>()
                        .eq(Appointment::getPatientId, patient.getId())
                        .orderByDesc(Appointment::getCreateTime)
        );
        return Result.ok(list);
    }

    // 3) 医生查看我的预约：GET /api/appointment/doctor
    @GetMapping("/doctor")
    public Result<?> listForDoctor(HttpSession session) {
        SysUser loginUser = (SysUser) session.getAttribute("loginUser");
        if (loginUser == null) return Result.fail("未登录");
        if (!"DOCTOR".equals(loginUser.getRole())) return Result.fail("无权限：仅医生可查看");

        DoctorProfile doctor = doctorProfileService.getOne(
                new LambdaQueryWrapper<DoctorProfile>()
                        .eq(DoctorProfile::getUserId, loginUser.getId())
                        .last("limit 1")
        );
        if (doctor == null) return Result.fail("未绑定医生档案");

        List<Appointment> list = appointmentService.list(
                new LambdaQueryWrapper<Appointment>()
                        .eq(Appointment::getDoctorId, doctor.getId())
                        .orderByDesc(Appointment::getCreateTime)
        );
        return Result.ok(list);
    }

    // 4) 患者取消预约：PUT /api/appointment/{id}/cancel
    @PutMapping("/{id}/cancel")
    public Result<?> cancel(@PathVariable Long id, HttpSession session) {
        SysUser loginUser = (SysUser) session.getAttribute("loginUser");
        if (loginUser == null) return Result.fail("未登录");
        if (!"PATIENT".equals(loginUser.getRole())) return Result.fail("无权限：仅患者可取消");

        PatientProfile patient = patientProfileService.getOne(
                new LambdaQueryWrapper<PatientProfile>()
                        .eq(PatientProfile::getUserId, loginUser.getId())
                        .last("limit 1")
        );
        if (patient == null) return Result.fail("未绑定患者档案");

        Appointment appt = appointmentService.getById(id);
        if (appt == null) return Result.fail("预约不存在");
        if (!patient.getId().equals(appt.getPatientId())) return Result.fail("不能取消他人的预约");
        if (!Appointment.STATUS_BOOKED.equals(appt.getStatus())) return Result.fail("仅 BOOKED 状态可取消");

        appt.setStatus(Appointment.STATUS_CANCELLED);
        appointmentService.updateById(appt);
        return Result.ok(appt);
    }

    // 5) 医生完成预约：PUT /api/appointment/{id}/finish
    @PutMapping("/{id}/finish")
    public Result<?> finish(@PathVariable Long id, HttpSession session) {
        SysUser loginUser = (SysUser) session.getAttribute("loginUser");
        if (loginUser == null) return Result.fail("未登录");
        if (!"DOCTOR".equals(loginUser.getRole())) return Result.fail("无权限：仅医生可完成");

        DoctorProfile doctor = doctorProfileService.getOne(
                new LambdaQueryWrapper<DoctorProfile>()
                        .eq(DoctorProfile::getUserId, loginUser.getId())
                        .last("limit 1")
        );
        if (doctor == null) return Result.fail("未绑定医生档案");

        Appointment appt = appointmentService.getById(id);
        if (appt == null) return Result.fail("预约不存在");
        if (!doctor.getId().equals(appt.getDoctorId())) return Result.fail("不能完成他人的预约");
        if (!Appointment.STATUS_BOOKED.equals(appt.getStatus())) return Result.fail("仅 BOOKED 状态可完成");

        appt.setStatus(Appointment.STATUS_FINISHED);
        appointmentService.updateById(appt);
        return Result.ok(appt);
    }

    /**
     * 患者分页查看我的预约（支持按状态筛选）
     * GET /api/appointment/patient/page?page=1&size=5&status=BOOKED
     */
    @GetMapping("/patient/page")
    public Result<?> pageForPatient(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "5") Integer size,
            @RequestParam(required = false) String status,
            HttpSession session) {

        SysUser loginUser = (SysUser) session.getAttribute("loginUser");
        if (loginUser == null) return Result.fail("未登录");
        if (!"PATIENT".equals(loginUser.getRole())) return Result.fail("无权限：仅患者可查看");

        PatientProfile patient = patientProfileService.getOne(
                new LambdaQueryWrapper<PatientProfile>()
                        .eq(PatientProfile::getUserId, loginUser.getId())
                        .last("limit 1")
        );
        if (patient == null) return Result.fail("未绑定患者档案");

        LambdaQueryWrapper<Appointment> qw = new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getPatientId, patient.getId())
                .orderByDesc(Appointment::getCreateTime);

        if (StringUtils.hasText(status) && !"ALL".equalsIgnoreCase(status)) {
            qw.eq(Appointment::getStatus, status);
        }

        Page<Appointment> p = new Page<>(page, size);
        Page<Appointment> result = appointmentService.page(p, qw);

        return Result.ok(result);
    }

    /**
     * 医生分页查看自己的预约（支持按状态筛选）
     * GET /api/appointment/doctor/page?page=1&size=5&status=BOOKED
     */
    @GetMapping("/doctor/page")
    public Result<?> pageForDoctor(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "5") Integer size,
            @RequestParam(required = false) String status,
            HttpSession session) {

        SysUser loginUser = (SysUser) session.getAttribute("loginUser");
        if (loginUser == null) return Result.fail("未登录");
        if (!"DOCTOR".equals(loginUser.getRole())) return Result.fail("无权限：仅医生可查看");

        DoctorProfile doctor = doctorProfileService.getOne(
                new LambdaQueryWrapper<DoctorProfile>()
                        .eq(DoctorProfile::getUserId, loginUser.getId())
                        .last("limit 1")
        );
        if (doctor == null) return Result.fail("未绑定医生档案");

        LambdaQueryWrapper<Appointment> qw = new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getDoctorId, doctor.getId())
                .orderByDesc(Appointment::getCreateTime);

        if (StringUtils.hasText(status) && !"ALL".equalsIgnoreCase(status)) {
            qw.eq(Appointment::getStatus, status);
        }

        Page<Appointment> p = new Page<>(page, size);
        Page<Appointment> result = appointmentService.page(p, qw);

        return Result.ok(result);
    }
}