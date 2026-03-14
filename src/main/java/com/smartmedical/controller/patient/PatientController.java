package com.smartmedical.controller.patient;

import com.smartmedical.entity.PatientProfile;
import com.smartmedical.entity.SysUser;
import com.smartmedical.service.PatientProfileService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/patient")
public class PatientController {

    private final PatientProfileService patientProfileService;

    public PatientController(PatientProfileService patientProfileService) {
        this.patientProfileService = patientProfileService;
    }

    @PostMapping("/add")
    public String add(PatientProfile patient) {
        try {
            patientProfileService.save(patient);
            return "redirect:/patient/list-page?msg=" +
                    URLEncoder.encode("新增成功", StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "redirect:/patient/add-page?msg=duplicate_user";
        }
    }

    @PostMapping("/update")
    public String update(PatientProfile patient) {
        patientProfileService.updateById(patient);
        return "redirect:/patient/list-page?msg=" +
                URLEncoder.encode("修改成功", StandardCharsets.UTF_8);
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        patientProfileService.removeById(id);
        return "redirect:/patient/list-page?msg=" +
                URLEncoder.encode("删除成功", StandardCharsets.UTF_8);
    }

    @GetMapping("/my-page")
    public String myPage(HttpSession session, Model model) {

        SysUser user = (SysUser) session.getAttribute("loginUser");

        if (user == null) {
            session.setAttribute("msg", "请先登录");
            return "redirect:/login";
        }

        if (!"PATIENT".equalsIgnoreCase(user.getRole())) {
            session.setAttribute("msg", "无权限访问患者个人页面");
            return "redirect:/login";
        }

        PatientProfile patient = patientProfileService.lambdaQuery()
                .eq(PatientProfile::getUserId, user.getId())
                .one();

        // 关键：无论有没有档案，都把 patient 放进 model（可以为 null）
        model.addAttribute("patient", patient);

        if (patient == null) {
            model.addAttribute("msg", "当前账号未绑定患者档案，请联系管理员创建患者档案");
        }

        return "patient/my-page";
    }
}