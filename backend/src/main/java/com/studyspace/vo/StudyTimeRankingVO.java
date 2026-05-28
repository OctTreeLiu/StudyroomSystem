package com.studyspace.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 学习时长排行榜视图对象
 */
@Data
public class StudyTimeRankingVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 排行榜列表
     */
    private List<RankingItem> rankingList;
    
    /**
     * 排行榜项
     */
    @Data
    public static class RankingItem implements Serializable {
        private static final long serialVersionUID = 1L;
        
        /**
         * 用户ID
         */
        private Long userId;
        
        /**
         * 用户名
         */
        private String username;
        
        /**
         * 真实姓名
         */
        private String realName;
        
        /**
         * 头像URL
         */
        private String avatarUrl;
        
        /**
         * 学习时长（分钟）
         */
        private Integer minutes;
        
        /**
         * 学习时长（小时，保留1位小数）
         */
        private Double hours;
        
        /**
         * 排名
         */
        private Integer rank;
    }
}

