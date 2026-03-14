package com.smartmedical.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartmedical.entity.Dept;
import com.smartmedical.mapper.DeptMapper;
import com.smartmedical.service.DeptService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.util.StringUtils;
import java.util.List;


/**
 * 科室服务实现类
 * 实现科室业务逻辑处理
 */
@Service
public class DeptServiceImpl extends ServiceImpl<DeptMapper, Dept> implements DeptService {

    /**
     * 科室名称模糊查询
     * @param keyword 关键字（可为空）
     * @return 科室列表
     */
    @Override
    public List<Dept> search(String keyword) {
        LambdaQueryWrapper<Dept> qw = new LambdaQueryWrapper<>();

        // keyword 不为空时：where name like '%keyword%'
        if (StringUtils.hasText(keyword)) {
            qw.like(Dept::getName, keyword);
        }

        return this.list(qw);
    }
}
