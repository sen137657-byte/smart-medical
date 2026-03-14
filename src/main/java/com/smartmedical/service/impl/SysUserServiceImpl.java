package com.smartmedical.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartmedical.entity.SysUser;
import com.smartmedical.mapper.SysUserMapper;
import com.smartmedical.service.SysUserService;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
        implements SysUserService {

}
