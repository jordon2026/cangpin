package com.jujing.museum.modules.collection.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jujing.museum.common.exception.BizException;
import com.jujing.museum.modules.collection.dto.RfidTagDTO;
import com.jujing.museum.modules.collection.entity.RfidTag;
import com.jujing.museum.modules.collection.mapper.RfidTagMapper;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RFID标签服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RfidTagService {

    private final RfidTagMapper tagMapper;

    /**
     * 分页查询标签列表
     */
    public IPage<RfidTag> page(int current, int size, String keyword, Integer status) {
        Page<RfidTag> page = new Page<>(current, size);
        QueryWrapper<RfidTag> wrapper = new QueryWrapper<>();
        
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like("tag_code", keyword)
                    .or().like("epc", keyword)
                    .or().like("collection_name", keyword));
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        
        wrapper.orderByDesc("create_time");
        return tagMapper.selectPage(page, wrapper);
    }

    /**
     * 查询所有未绑定的标签
     */
    public List<RfidTag> getUnboundTags() {
        QueryWrapper<RfidTag> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0).orderByDesc("create_time");
        return tagMapper.selectList(wrapper);
    }

    /**
     * 根据ID查询
     */
    public RfidTag getById(Long id) {
        return tagMapper.selectById(id);
    }

    /**
     * 新增标签
     */
    @Transactional
    public void add(RfidTagDTO dto) {
        // 检查标签编码是否已存在
        QueryWrapper<RfidTag> wrapper = new QueryWrapper<>();
        wrapper.eq("tag_code", dto.getTagCode());
        if (tagMapper.selectCount(wrapper) > 0) {
            throw new BizException("标签编码已存在");
        }
        
        RfidTag tag = new RfidTag();
        BeanUtil.copyProperties(dto, tag);
        tag.setStatus(0); // 默认未绑定
        tagMapper.insert(tag);
        log.info("新增RFID标签: {}", dto.getTagCode());
    }

    /**
     * 更新标签
     */
    @Transactional
    public void update(RfidTagDTO dto) {
        RfidTag tag = tagMapper.selectById(dto.getId());
        if (tag == null) {
            throw new BizException("标签不存在");
        }
        
        // 检查标签编码是否被其他标签使用
        if (StrUtil.isNotBlank(dto.getTagCode()) && !dto.getTagCode().equals(tag.getTagCode())) {
            QueryWrapper<RfidTag> wrapper = new QueryWrapper<>();
            wrapper.eq("tag_code", dto.getTagCode()).ne("id", dto.getId());
            if (tagMapper.selectCount(wrapper) > 0) {
                throw new BizException("标签编码已被其他标签使用");
            }
        }
        
        tag.setTagCode(dto.getTagCode());
        tag.setEpc(dto.getEpc());
        tag.setRemark(dto.getRemark());
        tagMapper.updateById(tag);
        log.info("更新RFID标签: {}", dto.getTagCode());
    }

    /**
     * 解绑标签
     */
    @Transactional
    public void unbind(Long id) {
        RfidTag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BizException("标签不存在");
        }
        if (tag.getStatus() != 1) {
            throw new BizException("该标签未绑定，无需解绑");
        }
        
        tag.setStatus(0);
        tag.setCollectionId(null);
        tag.setCollectionName(null);
        tag.setBindTime(null);
        tagMapper.updateById(tag);
        log.info("解绑RFID标签: {}", tag.getTagCode());
    }

    /**
     * 删除标签
     */
    @Transactional
    public void delete(Long id) {
        RfidTag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BizException("标签不存在");
        }
        if (tag.getStatus() == 1) {
            throw new BizException("该标签已绑定藏品，无法删除");
        }
        
        tagMapper.deleteById(id);
        log.info("删除RFID标签: {}", tag.getTagCode());
    }
}
