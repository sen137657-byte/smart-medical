package com.smartmedical.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartmedical.common.Result;
import com.smartmedical.entity.SysUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

public class LoginInterceptor implements HandlerInterceptor {

    private boolean isPublic(String uri) {
        // 登录页 / 登录接口 / 错误页 / 静态资源 放行
        if (uri.equals("/login.html") || uri.equals("/login") || uri.startsWith("/auth/") || uri.equals("/error")) {
            return true;
        }
        if (uri.equals("/favicon.ico")) return true;

        // 静态资源常见路径
        if (uri.startsWith("/css/") || uri.startsWith("/js/") || uri.startsWith("/images/")
                || uri.startsWith("/static/") || uri.startsWith("/webjars/")) {
            return true;
        }
        // 静态资源后缀
        return uri.endsWith(".css") || uri.endsWith(".js") || uri.endsWith(".png")
                || uri.endsWith(".jpg") || uri.endsWith(".jpeg") || uri.endsWith(".gif") || uri.endsWith(".svg");
    }

    private void writeJson401(HttpServletResponse response, String msg) throws IOException {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        Result<?> r = Result.fail(msg);
        response.getWriter().write(new ObjectMapper().writeValueAsString(r));
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String uri = request.getRequestURI();

        // 0) 放行公共资源
        if (isPublic(uri)) return true;

        // 1) 获取登录用户（不要强制创建 session）
        HttpSession session = request.getSession(false);
        SysUser loginUser = (session == null) ? null : (SysUser) session.getAttribute("loginUser");

        // 2) 未登录
        if (loginUser == null) {
            // API 请求：返回 JSON 401（不要重定向）
            if (uri.startsWith("/api/")) {
                writeJson401(response, "未登录");
                return false;
            }
            // 页面请求：跳转登录页（统一 login.html）
            request.getSession(true).setAttribute("msg", "请先登录");
            response.sendRedirect("/login.html");
            return false;
        }

        // 3) 已登录：角色控制
        String role = loginUser.getRole();

        // 医生个人页
        if (uri.equals("/doctor/my-page") && !"DOCTOR".equals(role)) {
            response.sendRedirect("/");
            return false;
        }

        // 患者个人页
        if (uri.equals("/patient/my-page") && !"PATIENT".equals(role)) {
            response.sendRedirect("/");
            return false;
        }

        // 后台管理模块（注意：不要把 /doctor-page 这种医生功能页误判成后台）
        boolean isAdminPage = uri.startsWith("/dept") || uri.startsWith("/system");
        if (isAdminPage && !"ADMIN".equals(role)) {
            response.sendRedirect("/");
            return false;
        }

        return true;
    }
}