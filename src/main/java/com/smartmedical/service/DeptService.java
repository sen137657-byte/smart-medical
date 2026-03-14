package com.smartmedical.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartmedical.entity.Dept;

import java.util.List;
public interface DeptService extends IService<Dept> {
    List<Dept> search(String keyword);


}
