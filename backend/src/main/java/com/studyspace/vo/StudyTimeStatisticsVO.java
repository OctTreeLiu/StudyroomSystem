package com.studyspace.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 学习时长统计视图对象
 */
@Data
public class StudyTimeStatisticsVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 过去24小时的学习时长（分钟）
     */
    private Integer last24Hours;
    
    /**
     * 过去7天的学习时长（分钟）
     */
    private Integer last7Days;
    
    /**
     * 过去30天的学习时长（分钟）
     */
    private Integer last30Days;
    
    /**
     * 每日统计详情（用于图表展示）
     */
    private List<DailyStatistics> dailyStatistics;
    
    /**
     * 每日统计数据
     */
    @Data
    public static class DailyStatistics implements Serializable {
        private static final long serialVersionUID = 1L;
        
        /**
         * 日期（格式：YYYY-MM-DD）
         */
        private String date;
        
        /**
         * 该日学习时长（分钟）
         */
        private Integer minutes;
        
        /**
         * 该日学习时长（小时，保留1位小数）
         */
        private Double hours;
    }
}

