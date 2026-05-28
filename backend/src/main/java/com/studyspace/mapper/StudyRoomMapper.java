package com.studyspace.mapper;

import com.studyspace.entity.StudyRoom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 自习室Mapper接口
 */
@Mapper
public interface StudyRoomMapper {
    
    /**
     * 根据ID查询自习室
     */
    StudyRoom selectById(@Param("id") Long id);

    /**
     * 根据自习室编号查询（唯一约束校验）
     */
    StudyRoom selectByRoomNumber(@Param("roomNumber") String roomNumber);
    
    /**
     * 查询所有自习室
     */
    List<StudyRoom> selectAll();
    
    /**
     * 根据状态查询自习室
     */
    List<StudyRoom> selectByStatus(@Param("status") Integer status);
    
    /**
     * 插入自习室
     */
    int insert(StudyRoom studyRoom);
    
    /**
     * 更新自习室
     */
    int update(StudyRoom studyRoom);
}

