package com.smartmedical.controller.page;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartmedical.entity.Dept;
import com.smartmedical.entity.DoctorProfile;
import com.smartmedical.entity.PatientProfile;
import com.smartmedical.entity.SysUser;
import com.smartmedical.service.DeptService;
import com.smartmedical.service.DoctorProfileService;
import com.smartmedical.service.PatientProfileService;
import com.smartmedical.service.SysUserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 页面跳转控制器（Thymeleaf 模板页跳转）
 * 说明：本类只负责返回模板路径，不做业务逻辑（业务逻辑在 ApiController / Service 中）
 */
@Controller
public class PageController {

    private final DeptService deptService;
    private final DoctorProfileService doctorProfileService;
    private final SysUserService sysUserService;
    private final PatientProfileService patientProfileService;

    public PageController(DeptService deptService,
                          DoctorProfileService doctorProfileService,
                          SysUserService sysUserService,
                          PatientProfileService patientProfileService) {
        this.deptService = deptService;
        this.doctorProfileService = doctorProfileService;
        this.sysUserService = sysUserService;
        this.patientProfileService = patientProfileService;
    }

    /* -------------------- 通用页面 -------------------- */

    /**
     * 首页
     */
    @GetMapping("/")
    public String index(HttpSession session, Model model) {
        Object msg = session.getAttribute("msg");
        if (msg != null) {
            model.addAttribute("msg", msg);
            session.removeAttribute("msg"); // 保证只弹一次
        }
        return "index";
    }

    /**
     * 登录页
     */
    @GetMapping("/login")
    public String login(HttpSession session, Model model) {
        Object msg = session.getAttribute("msg");
        if (msg != null) {
            model.addAttribute("msg", msg);
            session.removeAttribute("msg"); // 保证只弹一次
        }
        return "login";
    }

    /* -------------------- 科室管理页面（管理员） -------------------- */

    /**
     * 科室列表页（分页 + 可选名称搜索）
     */
    @GetMapping("/dept/list-page")
    public String deptListPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "5") Integer pageSize,
            @RequestParam(required = false) String name,
            Model model
    ) {
        Page<Dept> page = new Page<>(pageNum, pageSize);

        QueryWrapper<Dept> wrapper = new QueryWrapper<>();
        if (name != null && !name.trim().isEmpty()) {
            wrapper.like("name", name.trim());
        }

        IPage<Dept> result = deptService.page(page, wrapper);

        model.addAttribute("page", result);
        model.addAttribute("deptList", result.getRecords());
        model.addAttribute("name", name);
        model.addAttribute("pageSize", pageSize);

        return "dept/list";
    }

    /* -------------------- 医生管理页面（管理员） -------------------- */

    /**
     * 医生列表页（分页 + 可选名称搜索）
     */
    @GetMapping("/doctor/list-page")
    public String doctorListPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "5") Integer pageSize,
            @RequestParam(required = false) String name,
            Model model
    ) {
        Page<DoctorProfile> page = new Page<>(pageNum, pageSize);

        QueryWrapper<DoctorProfile> wrapper = new QueryWrapper<>();
        if (name != null && !name.trim().isEmpty()) {
            wrapper.like("name", name.trim());
        }

        IPage<DoctorProfile> result = doctorProfileService.page(page, wrapper);

        model.addAttribute("page", result);
        model.addAttribute("doctorList", result.getRecords());
        model.addAttribute("name", name);
        model.addAttribute("pageSize", pageSize);

        // 供页面展示科室下拉（比如新增/编辑时用）
        model.addAttribute("deptList", deptService.list());

        return "doctor/list";
    }

    /**
     * 医生新增页
     */
    @GetMapping("/doctor/add-page")
    public String doctorAddPage(Model model) {
        // 科室下拉
        model.addAttribute("deptList", deptService.list());

        // 只查询角色为 DOCTOR 的系统用户，用于绑定 doctor_profile.user_id
        model.addAttribute("doctorUserList",
                sysUserService.list(
                        new LambdaQueryWrapper<SysUser>()
                                .eq(SysUser::getRole, "DOCTOR")
                )
        );

        return "doctor/add";
    }

    /**
     * 医生编辑页
     */
    @GetMapping("/doctor/edit-page/{id}")
    public String doctorEditPage(@PathVariable Long id, Model model) {
        DoctorProfile d = doctorProfileService.getById(id);
        model.addAttribute("doctor", d);

        model.addAttribute("deptList", deptService.list());
        model.addAttribute("doctorUserList",
                sysUserService.list(
                        new LambdaQueryWrapper<SysUser>()
                                .eq(SysUser::getRole, "DOCTOR")
                )
        );

        return "doctor/edit";
    }

    /* -------------------- 患者管理页面（管理员） -------------------- */

    /**
     * 患者列表页（分页 + 可选名称搜索）
     */
    @GetMapping("/patient/list-page")
    public String patientListPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "5") Integer pageSize,
            @RequestParam(required = false) String name,
            Model model
    ) {
        Page<PatientProfile> page = new Page<>(pageNum, pageSize);

        QueryWrapper<PatientProfile> wrapper = new QueryWrapper<>();
        if (name != null && !name.trim().isEmpty()) {
            wrapper.like("name", name.trim());
        }

        IPage<PatientProfile> result = patientProfileService.page(page, wrapper);

        model.addAttribute("page", result);
        model.addAttribute("patientList", result.getRecords());
        model.addAttribute("name", name);
        model.addAttribute("pageSize", pageSize);

        return "patient/list";
    }

    /**
     * 患者新增页
     */
    @GetMapping("/patient/add-page")
    public String patientAddPage(Model model) {
        // 只查询角色为 PATIENT 的系统用户，用于绑定 patient_profile.user_id
        model.addAttribute("patientUserList",
                sysUserService.list(
                        new LambdaQueryWrapper<SysUser>()
                                .eq(SysUser::getRole, "PATIENT")
                )
        );
        return "patient/add";
    }

    /**
     * 患者编辑页
     */
    @GetMapping("/patient/edit-page/{id}")
    public String patientEditPage(@PathVariable Long id, Model model) {
        PatientProfile p = patientProfileService.getById(id);
        model.addAttribute("patient", p);

        model.addAttribute("patientUserList",
                sysUserService.list(
                        new LambdaQueryWrapper<SysUser>()
                                .eq(SysUser::getRole, "PATIENT")
                )
        );

        return "patient/edit";
    }

    /* -------------------- 预约模块页面跳转 -------------------- */

    /**
     * 患者预约页面
     */
    @GetMapping("/appointment/patient-page")
    public String patientAppointmentPage() {
        return "appointment/patient";
    }

    /**
     * 医生预约页面
     */
    @GetMapping("/appointment/doctor-page")
    public String doctorAppointmentPage() {
        return "appointment/doctor";
    }

    /* -------------------- 在线问诊模块页面跳转 -------------------- */

    /**
     * 患者在线问诊页面
     */
    @GetMapping("/consultation/patient-page")
    public String patientConsultationPage() {
        return "consultation/patient";
    }

    /**
     * 医生在线问诊页面
     */
    @GetMapping("/consultation/doctor-page")
    public String doctorConsultationPage() {
        return "consultation/doctor";
    }
    /* -------------------- 电子病历模块页面跳转 -------------------- */

    /** 医生病历管理页面 */
    @GetMapping("/medicalRecord/doctor-page")
    public String doctorMedicalRecordPage() {
        return "medical_record/doctor";
    }

    /** 患者病历查看页面 */
    @GetMapping("/medicalRecord/patient-page")
    public String patientMedicalRecordPage() {
        return "medical_record/patient";
    }

}