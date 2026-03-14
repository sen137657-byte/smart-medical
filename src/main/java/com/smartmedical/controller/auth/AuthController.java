package com.smartmedical.controller.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartmedical.dto.LoginRequest;
import com.smartmedical.entity.SysUser;
import com.smartmedical.service.SysUserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final SysUserService sysUserService;

    public AuthController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @PostMapping("/login")
    public String login(LoginRequest req, HttpSession session, Model model) {

        // 0) 判空：username / password
        if (req == null || req.getUsername() == null || req.getPassword() == null
                || req.getUsername().trim().isEmpty() || req.getPassword().trim().isEmpty()) {
            model.addAttribute("error", "请输入账号和密码");
            return "login";
        }

        String username = req.getUsername().trim();

        // 1) 查用户（启用状态）
        SysUser user = sysUserService.getOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
                        .eq(SysUser::getStatus, 1)
        );

        // 2) 校验
        if (user == null || !req.getPassword().equals(user.getPassword())) {
            model.addAttribute("error", "账号或密码错误（或账号已禁用）");
            return "login";
        }

        // 3) 登录成功
        session.setAttribute("loginUser", user);
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}