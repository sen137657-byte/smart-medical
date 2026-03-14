package com.smartmedical.controller.doctor;

import com.smartmedical.entity.DoctorProfile;
import com.smartmedical.entity.SysUser;
import com.smartmedical.service.DoctorProfileService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    private final DoctorProfileService doctorProfileService;

    public DoctorController(DoctorProfileService doctorProfileService) {
        this.doctorProfileService = doctorProfileService;
    }

    @PostMapping("/add")
    public String add(DoctorProfile doctor) {
        doctorProfileService.save(doctor);
        String msg = URLEncoder.encode("新增成功", StandardCharsets.UTF_8);
        return "redirect:/doctor/list-page?msg=" + msg;
    }

    @PostMapping("/update")
    public String update(DoctorProfile doctor) {
        doctorProfileService.updateById(doctor);
        String msg = URLEncoder.encode("修改成功", StandardCharsets.UTF_8);
        return "redirect:/doctor/list-page?msg=" + msg;
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        doctorProfileService.removeById(id);
        String msg = URLEncoder.encode("删除成功", StandardCharsets.UTF_8);
        return "redirect:/doctor/list-page?msg=" + msg;
    }

    @GetMapping("/my-page")
    public String myPage(HttpSession session, Model model) {

        SysUser user = (SysUser) session.getAttribute("loginUser");

        if (user == null) {
            session.setAttribute("msg", "请先登录");
            return "redirect:/login";
        }

        if (!"DOCTOR".equalsIgnoreCase(user.getRole())) {
            session.setAttribute("msg", "无权限访问医生个人页面");
            return "redirect:/login";
        }

        DoctorProfile doctor = doctorProfileService.lambdaQuery()
                .eq(DoctorProfile::getUserId, user.getId())
                .one();

        if (doctor == null) {
            model.addAttribute("msg", "当前账号未绑定医生档案，请联系管理员创建医生档案");
            return "doctor/my-page";
        }

        model.addAttribute("doctor", doctor);
        return "doctor/my-page";
    }
}