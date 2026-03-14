package com.smartmedical.controller.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartmedical.common.Result;
import com.smartmedical.dto.DoctorScheduleAutoCreateWeekReq;
import com.smartmedical.dto.DoctorScheduleBatchCreateReq;
import com.smartmedical.entity.Appointment;
import com.smartmedical.entity.DoctorProfile;
import com.smartmedical.entity.DoctorSchedule;
import com.smartmedical.entity.SysUser;
import com.smartmedical.service.AppointmentService;
import com.smartmedical.service.DoctorProfileService;
import com.smartmedical.service.DoctorScheduleService;
import jakarta.servlet.http.HttpSession;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/schedule")
public class DoctorScheduleController {

    private final DoctorScheduleService doctorScheduleService;
    private final DoctorProfileService doctorProfileService;
    private final AppointmentService appointmentService;

    public DoctorScheduleController(DoctorScheduleService doctorScheduleService,
                                    DoctorProfileService doctorProfileService,
                                    AppointmentService appointmentService) {
        this.doctorScheduleService = doctorScheduleService;
        this.doctorProfileService = doctorProfileService;
        this.appointmentService = appointmentService;
    }

    // ========================= 1) 患者侧：查询某医生某天可预约时间段 =========================
    // GET /api/schedule/slots?doctorId=7&date=2026-02-28
    @GetMapping("/slots")
    public Result<?> slots(@RequestParam Long doctorId,
                           @RequestParam String date) {

        LocalDate workDate;
        try {
            workDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            return Result.fail("date 格式错误，必须 yyyy-MM-dd（例如 2026-02-28）");
        }

        // 只返回 OPEN 的 timeSlot
        List<DoctorSchedule> list = doctorScheduleService.list(
                new LambdaQueryWrapper<DoctorSchedule>()
                        .eq(DoctorSchedule::getDoctorId, doctorId)
                        .eq(DoctorSchedule::getWorkDate, workDate)
                        .eq(DoctorSchedule::getStatus, "OPEN")
                        .orderByAsc(DoctorSchedule::getTimeSlot)
        );

        List<String> slots = new ArrayList<>();
        for (DoctorSchedule s : list) {
            if (StringUtils.hasText(s.getTimeSlot())) {
                slots.add(s.getTimeSlot());
            }
        }
        return Result.ok(slots);
    }

    // ========================= 2) 医生侧：批量生成排班（你原来的） =========================
    @PostMapping("/batchCreate")
    public Result<?> batchCreate(@RequestBody DoctorScheduleBatchCreateReq req, HttpSession session) {
        SysUser loginUser = (SysUser) session.getAttribute("loginUser");
        if (loginUser == null) return Result.fail("未登录");
        if (!"DOCTOR".equals(loginUser.getRole())) return Result.fail("无权限：仅医生可生成排班");

        if (req == null) return Result.fail("请求体不能为空");
        if (!StringUtils.hasText(req.getWorkDate())) return Result.fail("workDate 不能为空");
        if (!StringUtils.hasText(req.getStartTime())) return Result.fail("startTime 不能为空");
        if (!StringUtils.hasText(req.getEndTime())) return Result.fail("endTime 不能为空");
        if (req.getSlotMinutes() == null || req.getSlotMinutes() <= 0) return Result.fail("slotMinutes 必须 > 0");

        LocalDate workDate;
        LocalTime start;
        LocalTime end;
        try {
            workDate = LocalDate.parse(req.getWorkDate(), DateTimeFormatter.ISO_LOCAL_DATE);
            start = LocalTime.parse(req.getStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
            end = LocalTime.parse(req.getEndTime(), DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            return Result.fail("日期/时间格式错误：workDate=yyyy-MM-dd, time=HH:mm");
        }

        if (!start.isBefore(end)) return Result.fail("startTime 必须早于 endTime");
        if (req.getSlotMinutes() > 240) return Result.fail("slotMinutes 过大（建议 10~60）");

        Long doctorId = req.getDoctorId();
        if (doctorId == null) {
            DoctorProfile doctor = doctorProfileService.getOne(
                    new LambdaQueryWrapper<DoctorProfile>()
                            .eq(DoctorProfile::getUserId, loginUser.getId())
                            .last("limit 1")
            );
            if (doctor == null) return Result.fail("未绑定医生档案，无法生成排班");
            doctorId = doctor.getId();
        }

        boolean overwrite = Boolean.TRUE.equals(req.getOverwrite());

        int removed = 0;
        if (overwrite) {
            removed = removeUnbookedSchedules(doctorId, workDate);
        }

        int created = createSlots(doctorId, workDate, start, end, req.getSlotMinutes());

        if (created == 0) {
            return Result.fail("没有生成任何排班（可能区间太小或已全部存在）");
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("doctorId", doctorId);
        resp.put("workDate", workDate.toString());
        resp.put("removed", removed);
        resp.put("created", created);
        return Result.ok(resp);
    }

    // ========================= 3) 医生侧：自动生成未来一周（上午+下午） =========================
    /**
     * POST /api/schedule/autoCreateWeek
     *
     * Body 示例：
     * {
     *   "days": 7,
     *   "morningStart": "09:00",
     *   "morningEnd": "11:00",
     *   "afternoonStart": "14:00",
     *   "afternoonEnd": "17:00",
     *   "slotMinutes": 30,
     *   "overwrite": true
     * }
     */
    @PostMapping("/autoCreateWeek")
    public Result<?> autoCreateWeek(@RequestBody DoctorScheduleAutoCreateWeekReq req, HttpSession session) {

        SysUser loginUser = (SysUser) session.getAttribute("loginUser");
        if (loginUser == null) return Result.fail("未登录");
        if (!"DOCTOR".equals(loginUser.getRole())) return Result.fail("无权限：仅医生可生成排班");

        if (req == null) req = new DoctorScheduleAutoCreateWeekReq();
        if (req.getDays() == null || req.getDays() <= 0) req.setDays(7);
        if (req.getSlotMinutes() == null || req.getSlotMinutes() <= 0) req.setSlotMinutes(30);

        // 解析时间
        LocalTime mStart, mEnd, aStart, aEnd;
        try {
            DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm");
            mStart = LocalTime.parse(req.getMorningStart(), tf);
            mEnd   = LocalTime.parse(req.getMorningEnd(), tf);
            aStart = LocalTime.parse(req.getAfternoonStart(), tf);
            aEnd   = LocalTime.parse(req.getAfternoonEnd(), tf);
        } catch (Exception e) {
            return Result.fail("时间格式错误：必须 HH:mm（例如 09:00）");
        }
        if (!mStart.isBefore(mEnd)) return Result.fail("上午开始时间必须早于结束时间");
        if (!aStart.isBefore(aEnd)) return Result.fail("下午开始时间必须早于结束时间");

        // doctorId：默认当前登录医生
        Long doctorId = req.getDoctorId();
        if (doctorId == null) {
            DoctorProfile doctor = doctorProfileService.getOne(
                    new LambdaQueryWrapper<DoctorProfile>()
                            .eq(DoctorProfile::getUserId, loginUser.getId())
                            .last("limit 1")
            );
            if (doctor == null) return Result.fail("未绑定医生档案，无法生成排班");
            doctorId = doctor.getId();
        }

        boolean overwrite = Boolean.TRUE.equals(req.getOverwrite());
        int totalCreated = 0;
        int totalRemoved = 0;

        LocalDate startDate = LocalDate.now(); // 从今天开始
        for (int i = 0; i < req.getDays(); i++) {
            LocalDate d = startDate.plusDays(i);

            if (overwrite) {
                totalRemoved += removeUnbookedSchedules(doctorId, d);
            }

            // 上午
            totalCreated += createSlots(doctorId, d, mStart, mEnd, req.getSlotMinutes());
            // 下午
            totalCreated += createSlots(doctorId, d, aStart, aEnd, req.getSlotMinutes());
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("doctorId", doctorId);
        resp.put("days", req.getDays());
        resp.put("startDate", startDate.toString());
        resp.put("overwrite", overwrite);
        resp.put("removed", totalRemoved);
        resp.put("created", totalCreated);

        return Result.ok(resp);
    }

    // ========================= 工具方法：删除当天未被BOOKED占用的排班 =========================
    private int removeUnbookedSchedules(Long doctorId, LocalDate workDate) {
        List<DoctorSchedule> existing = doctorScheduleService.list(
                new LambdaQueryWrapper<DoctorSchedule>()
                        .eq(DoctorSchedule::getDoctorId, doctorId)
                        .eq(DoctorSchedule::getWorkDate, workDate)
        );

        int removed = 0;
        for (DoctorSchedule s : existing) {
            String slotStart = null;
            if (StringUtils.hasText(s.getTimeSlot()) && s.getTimeSlot().contains("-")) {
                slotStart = s.getTimeSlot().split("-")[0]; // "09:00"
            }
            if (!StringUtils.hasText(slotStart)) continue;

            // 预约时间按 slotStart 拼成 LocalDateTime，比 likeRight 更稳定
            LocalDateTime apptTime = LocalDateTime.of(workDate, LocalTime.parse(slotStart, DateTimeFormatter.ofPattern("HH:mm")));

            long booked = appointmentService.count(
                    new LambdaQueryWrapper<Appointment>()
                            .eq(Appointment::getDoctorId, doctorId)
                            .eq(Appointment::getStatus, Appointment.STATUS_BOOKED)
                            .eq(Appointment::getAppointmentTime, apptTime)
            );

            if (booked == 0) {
                doctorScheduleService.removeById(s.getId());
                removed++;
            }
        }
        return removed;
    }

    // ========================= 工具方法：生成某天某时间段 slots（自动去重） =========================
    private int createSlots(Long doctorId, LocalDate workDate, LocalTime start, LocalTime end, int slotMinutes) {

        DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm");
        List<DoctorSchedule> toSave = new ArrayList<>();

        LocalTime cur = start;
        int guard = 0;

        while (cur.plusMinutes(slotMinutes).compareTo(end) <= 0) {
            LocalTime next = cur.plusMinutes(slotMinutes);
            String timeSlot = cur.format(tf) + "-" + next.format(tf);

            long exists = doctorScheduleService.count(
                    new LambdaQueryWrapper<DoctorSchedule>()
                            .eq(DoctorSchedule::getDoctorId, doctorId)
                            .eq(DoctorSchedule::getWorkDate, workDate)
                            .eq(DoctorSchedule::getTimeSlot, timeSlot)
            );

            if (exists == 0) {
                DoctorSchedule ds = new DoctorSchedule();
                ds.setDoctorId(doctorId);
                ds.setWorkDate(workDate);
                ds.setTimeSlot(timeSlot);
                ds.setStatus("OPEN");
                ds.setCreateTime(LocalDateTime.now());
                toSave.add(ds);
            }

            cur = next;
            if (++guard > 2000) break;
        }

        if (!toSave.isEmpty()) {
            doctorScheduleService.saveBatch(toSave);
        }
        return toSave.size();
    }
}