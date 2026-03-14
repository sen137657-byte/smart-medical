package com.smartmedical.controller.doctor;

import com.smartmedical.common.Result;
import com.smartmedical.vo.DoctorOptionVO;
import com.smartmedical.entity.Dept;
import com.smartmedical.entity.DoctorProfile;
import com.smartmedical.service.DeptService;
import com.smartmedical.service.DoctorProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 医生相关接口（供前端下拉框、展示用）
 */
@RestController
public class DoctorApiController {

    @Resource
    private DoctorProfileService doctorProfileService;

    @Resource
    private DeptService deptService;

    /**
     * 获取医生下拉选项
     * 返回：医生 + 科室名称 + 擅长方向
     */
    @GetMapping("/api/doctor/options")
    public Result<?> options() {

        // 1. 查询所有医生信息
        List<DoctorProfile> doctors = doctorProfileService.list();

        // 2. 收集所有科室ID（避免循环查数据库）
        Set<Long> deptIds = doctors.stream()
                .map(DoctorProfile::getDeptId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 3. 批量查询科室信息
        Map<Long, String> deptNameMap = new HashMap<>();
        if (!deptIds.isEmpty()) {
            List<Dept> deptList = deptService.listByIds(deptIds);
            for (Dept dept : deptList) {
                deptNameMap.put(dept.getId(), dept.getName());
            }
        }

        // 4. 组装前端需要的 VO 对象
        List<DoctorOptionVO> resultList = new ArrayList<>();
        for (DoctorProfile doctor : doctors) {

            DoctorOptionVO vo = new DoctorOptionVO();

            vo.setId(doctor.getId());
            vo.setName(doctor.getName());
            vo.setDeptId(doctor.getDeptId());
            vo.setDeptName(deptNameMap.get(doctor.getDeptId()));
            vo.setSpecialty(doctor.getSpecialty());

            resultList.add(vo);
        }

        return Result.ok(resultList);
    }
}