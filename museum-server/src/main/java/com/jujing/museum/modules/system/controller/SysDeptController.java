package com.jujing.museum.modules.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jujing.museum.annotation.OperLog;
import com.jujing.museum.common.exception.BizException;
import com.jujing.museum.common.result.R;
import com.jujing.museum.modules.auth.entity.SysDept;
import com.jujing.museum.modules.auth.mapper.SysDeptMapper;
import com.jujing.museum.modules.system.dto.DeptDTO;
import com.jujing.museum.modules.system.vo.DeptTreeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 部门管理
 */
@Tag(name = "部门管理")
@RestController
@RequestMapping("/api/v1/system/dept")
@RequiredArgsConstructor
public class SysDeptController {

    private final SysDeptMapper deptMapper;

    @Operation(summary = "查询部门列表（树形）")
    @SaCheckPermission("system:dept:list")
    @GetMapping("/list")
    public R<List<DeptTreeVO>> list() {
        List<SysDept> depts = deptMapper.selectList(
                new LambdaQueryWrapper<SysDept>().orderByAsc(SysDept::getSort));
        List<DeptTreeVO> tree = buildDeptTree(depts, 0L);
        return R.ok(tree);
    }

    @Operation(summary = "查询部门详情")
    @SaCheckPermission("system:dept:query")
    @GetMapping("/{id}")
    public R<SysDept> getInfo(@PathVariable Long id) {
        SysDept dept = deptMapper.selectById(id);
        if (dept == null) {
            throw new BizException("部门不存在");
        }
        return R.ok(dept);
    }

    @Operation(summary = "新增部门")
    @SaCheckPermission("system:dept:add")
    @OperLog(title = "部门管理", type = 1)
    @PostMapping
    public R<Void> add(@Valid @RequestBody DeptDTO dto) {
        SysDept dept = new SysDept();
        dept.setParentId(dto.getParentId());
        dept.setDeptName(dto.getDeptName());
        dept.setSort(dto.getSort() != null ? dto.getSort() : 0);
        dept.setLeader(dto.getLeader());
        dept.setPhone(dto.getPhone());
        dept.setEmail(dto.getEmail());
        dept.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        deptMapper.insert(dept);
        return R.ok();
    }

    @Operation(summary = "修改部门")
    @SaCheckPermission("system:dept:edit")
    @OperLog(title = "部门管理", type = 2)
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody DeptDTO dto) {
        SysDept dept = deptMapper.selectById(id);
        if (dept == null) {
            throw new BizException("部门不存在");
        }
        dept.setParentId(dto.getParentId());
        dept.setDeptName(dto.getDeptName());
        dept.setSort(dto.getSort());
        dept.setLeader(dto.getLeader());
        dept.setPhone(dto.getPhone());
        dept.setEmail(dto.getEmail());
        dept.setStatus(dto.getStatus());
        deptMapper.updateById(dept);
        return R.ok();
    }

    @Operation(summary = "删除部门")
    @SaCheckPermission("system:dept:delete")
    @OperLog(title = "部门管理", type = 3)
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        Long childCount = deptMapper.selectCount(
                new LambdaQueryWrapper<SysDept>().eq(SysDept::getParentId, id));
        if (childCount > 0) {
            throw new BizException("存在子部门，不允许删除");
        }
        deptMapper.deleteById(id);
        return R.ok();
    }

    private List<DeptTreeVO> buildDeptTree(List<SysDept> depts, Long parentId) {
        List<DeptTreeVO> tree = new ArrayList<>();
        for (SysDept dept : depts) {
            if (parentId.equals(dept.getParentId())) {
                DeptTreeVO vo = new DeptTreeVO();
                vo.setId(dept.getId());
                vo.setParentId(dept.getParentId());
                vo.setDeptName(dept.getDeptName());
                vo.setSort(dept.getSort());
                vo.setLeader(dept.getLeader());
                vo.setPhone(dept.getPhone());
                vo.setEmail(dept.getEmail());
                vo.setStatus(dept.getStatus());
                vo.setChildren(buildDeptTree(depts, dept.getId()));
                tree.add(vo);
            }
        }
        return tree;
    }
}
