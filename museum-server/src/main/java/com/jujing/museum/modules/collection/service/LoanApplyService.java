package com.jujing.museum.modules.collection.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jujing.museum.common.exception.BizException;
import com.jujing.museum.modules.auth.entity.SysUser;
import com.jujing.museum.modules.auth.mapper.SysUserMapper;
import com.jujing.museum.modules.collection.dto.LoanApplyDTO;
import com.jujing.museum.modules.collection.entity.Collection;
import com.jujing.museum.modules.collection.entity.LoanApply;
import com.jujing.museum.modules.collection.mapper.CollectionMapper;
import com.jujing.museum.modules.collection.mapper.LoanApplyMapper;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 外借申请服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoanApplyService {

    private final LoanApplyMapper applyMapper;
    private final CollectionMapper collectionMapper;
    private final SysUserMapper userMapper;

    private static final DateTimeFormatter APPLY_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * 分页查询申请列表
     */
    public IPage<LoanApply> page(int current, int size, String keyword, Integer status) {
        Page<LoanApply> page = new Page<>(current, size);
        QueryWrapper<LoanApply> wrapper = new QueryWrapper<>();
        
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like("apply_no", keyword)
                    .or().like("collection_name", keyword)
                    .or().like("borrower_org", keyword));
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        
        wrapper.orderByDesc("create_time");
        return applyMapper.selectPage(page, wrapper);
    }

    /**
     * 根据ID查询
     */
    public LoanApply getById(Long id) {
        return applyMapper.selectById(id);
    }

    /**
     * 创建申请
     */
    @Transactional
    public void add(LoanApplyDTO dto) {
        String applyNo = "WJ" + LocalDateTime.now().format(APPLY_NO_FORMAT);
        
        // 获取藏品信息
        Collection collection = null;
        if (dto.getCollectionId() != null) {
            collection = collectionMapper.selectById(dto.getCollectionId());
        }
        
        LoanApply apply = new LoanApply();
        BeanUtil.copyProperties(dto, apply);
        apply.setApplyNo(applyNo);
        apply.setStatus(0); // 待审批
        apply.setReturned(0); // 未归还
        
        if (collection != null) {
            apply.setCollectionName(collection.getName());
            apply.setCollectionNo(collection.getCollectionNo());
        }
        
        // 设置申请人信息
        long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userMapper.selectById(userId);
        if (user != null) {
            apply.setApplicantId(userId);
            apply.setApplicantName(user.getRealName());
        }
        
        applyMapper.insert(apply);
        log.info("创建外借申请: {}", applyNo);
    }

    /**
     * 审批通过
     */
    @Transactional
    public void approve(Long id, String opinion) {
        LoanApply apply = applyMapper.selectById(id);
        if (apply == null) {
            throw new BizException("申请不存在");
        }
        if (apply.getStatus() != 0) {
            throw new BizException("该申请无法审批");
        }
        
        long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userMapper.selectById(userId);
        
        apply.setStatus(1); // 已通过
        apply.setApproverId(userId);
        apply.setApproverName(user != null ? user.getRealName() : "");
        apply.setApprovalTime(LocalDateTime.now());
        apply.setApprovalOpinion(opinion);
        applyMapper.updateById(apply);
        
        // 更新藏品状态为外借中
        if (apply.getCollectionId() != null) {
            Collection collection = collectionMapper.selectById(apply.getCollectionId());
            if (collection != null) {
                collection.setStatus(2); // 外借中
                collectionMapper.updateById(collection);
            }
        }
        
        log.info("审批通过外借申请: {}", apply.getApplyNo());
    }

    /**
     * 审批拒绝
     */
    @Transactional
    public void reject(Long id, String opinion) {
        LoanApply apply = applyMapper.selectById(id);
        if (apply == null) {
            throw new BizException("申请不存在");
        }
        if (apply.getStatus() != 0) {
            throw new BizException("该申请无法审批");
        }
        
        long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userMapper.selectById(userId);
        
        apply.setStatus(2); // 已拒绝
        apply.setApproverId(userId);
        apply.setApproverName(user != null ? user.getRealName() : "");
        apply.setApprovalTime(LocalDateTime.now());
        apply.setApprovalOpinion(opinion);
        applyMapper.updateById(apply);
        
        log.info("审批拒绝外借申请: {}", apply.getApplyNo());
    }

    /**
     * 撤回申请
     */
    @Transactional
    public void cancel(Long id) {
        LoanApply apply = applyMapper.selectById(id);
        if (apply == null) {
            throw new BizException("申请不存在");
        }
        if (apply.getStatus() != 0) {
            throw new BizException("只有待审批的申请可以撤回");
        }
        
        apply.setStatus(3); // 已撤回
        applyMapper.updateById(apply);
        
        log.info("撤回外借申请: {}", apply.getApplyNo());
    }

    /**
     * 归还
     */
    @Transactional
    public void returnApply(Long id, String returner, String remark) {
        LoanApply apply = applyMapper.selectById(id);
        if (apply == null) {
            throw new BizException("申请不存在");
        }
        if (apply.getStatus() != 1) {
            throw new BizException("只有已通过的申请可以归还");
        }
        if (apply.getReturned() == 1) {
            throw new BizException("该藏品已归还");
        }
        
        apply.setReturned(1);
        apply.setReturnTime(LocalDateTime.now());
        apply.setReturner(returner);
        apply.setReturnRemark(remark);
        applyMapper.updateById(apply);
        
        // 恢复藏品状态为正常
        if (apply.getCollectionId() != null) {
            Collection collection = collectionMapper.selectById(apply.getCollectionId());
            if (collection != null) {
                collection.setStatus(1); // 正常
                collectionMapper.updateById(collection);
            }
        }
        
        log.info("外借藏品已归还: {}", apply.getApplyNo());
    }

    /**
     * 删除申请
     */
    @Transactional
    public void delete(Long id) {
        LoanApply apply = applyMapper.selectById(id);
        if (apply == null) {
            throw new BizException("申请不存在");
        }
        if (apply.getStatus() == 1) {
            throw new BizException("已通过的申请无法删除");
        }
        
        applyMapper.deleteById(id);
        log.info("删除外借申请: {}", apply.getApplyNo());
    }
}
