package com.jujing.museum.modules.auth.dto;

import lombok.Data;

/**
 * 仪表盘统计DTO
 */
@Data
public class DashboardDTO {

    /** 藏品总数 */
    private Long totalCollections;

    /** 今日新增 */
    private Long todayNew;

    /** 待入库 */
    private Long pendingInbound;

    /** 待出库 */
    private Long pendingOutbound;

    /** 借出中 */
    private Long onLoan;

    /** 修复中 */
    private Long underRepair;

    /** 展出中 */
    private Long onDisplay;

    /** 在库数量 */
    private Long inStorage;

    /** 库房总数 */
    private Long totalStorages;

    /** 告警数量 */
    private Long alertCount;

    /** 待盘点任务 */
    private Long pendingInventory;

    /** 待审批 */
    private Long pendingApproval;

    /** 本月入库 */
    private Long monthlyInbound;

    /** 本月出库 */
    private Long monthlyOutbound;

    /** 藏品分类统计 */
    private java.util.List<CategoryStat> categoryStats;

    /** 近7天入库趋势 */
    private java.util.List<TrendData> inboundTrend;

    /** 近7天出库趋势 */
    private java.util.List<TrendData> outboundTrend;

    @Data
    public static class CategoryStat {
        private String category;
        private Long count;
    }

    @Data
    public static class TrendData {
        private String date;
        private Long count;
    }
}
