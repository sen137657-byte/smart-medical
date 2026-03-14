package com.smartmedical.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartmedical.common.Result;
import com.smartmedical.entity.Dept;
import com.smartmedical.service.DeptService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dept")
public class DeptController {

    private final DeptService deptService;

    public DeptController(DeptService deptService) {
        this.deptService = deptService;
    }

    /**
     * 查询所有科室列表
     */
    @GetMapping("/list")
    public Result<List<Dept>> list() {
        return Result.ok(deptService.list());
    }

    /**
     * 新增科室
     */
    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody Dept dept) {
        boolean flag = deptService.save(dept);
        return flag ? Result.ok(true) : Result.fail("新增失败");
    }

    /**
     * 科室分页查询（支持 name 模糊查询）
     */
    @GetMapping("/page")
    public Result<IPage<Dept>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "5") Integer pageSize,
            @RequestParam(required = false) String name
    ) {
        Page<Dept> page = new Page<>(pageNum, pageSize);

        QueryWrapper<Dept> wrapper = new QueryWrapper<>();
        if (name != null && !name.trim().isEmpty()) {
            wrapper.like("name", name);
        }

        return Result.ok(deptService.page(page, wrapper));
    }





    /**
     * 删除科室（根据id）
     */
    @DeleteMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable Integer id) {
        boolean flag = deptService.removeById(id);
        return flag ? Result.ok(true) : Result.fail("删除失败（可能id不存在）");
    }

    /**
     * 修改科室
     */
    @PutMapping("/update")
    public Result<Boolean> update(@RequestBody Dept dept) {
        boolean flag = deptService.updateById(dept);
        return flag ? Result.ok(true) : Result.fail("修改失败（可能id不存在）");
    }

    /**
     * 查询科室详情（根据id）
     */
    @GetMapping("/detail/{id}")
    public Result<Dept> detail(@PathVariable Integer id) {
        Dept dept = deptService.getById(id);
        return dept != null ? Result.ok(dept) : Result.fail("未找到该科室");
    }

}
