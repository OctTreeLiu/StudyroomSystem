package com.studyspace.mapper;

import com.studyspace.entity.PriceConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 价格配置Mapper接口
 */
@Mapper
public interface PriceConfigMapper {
    
    /**
     * 根据配置键查询配置
     */
    PriceConfig selectByKey(@Param("configKey") String configKey);
    
    /**
     * 查询所有配置
     */
    List<PriceConfig> selectAll();
    
    /**
     * 插入配置
     */
    int insert(PriceConfig priceConfig);
    
    /**
     * 更新配置
     */
    int update(PriceConfig priceConfig);
    
    /**
     * 根据ID删除配置
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 根据配置键删除配置
     */
    int deleteByKey(@Param("configKey") String configKey);
}

