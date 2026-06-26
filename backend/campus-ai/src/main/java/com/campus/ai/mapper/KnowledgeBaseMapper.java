package com.campus.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.ai.entity.KnowledgeBase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface KnowledgeBaseMapper extends BaseMapper<KnowledgeBase> {

    @Select("SELECT * FROM knowledge_base WHERE is_active = 1 AND deleted = 0")
    List<KnowledgeBase> selectActiveList();

    @Select("SELECT * FROM knowledge_base WHERE is_active = 1 AND deleted = 0 " +
            "AND (title LIKE CONCAT('%', #{keyword}, '%') " +
            "OR content LIKE CONCAT('%', #{keyword}, '%') " +
            "OR keywords LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY view_count DESC LIMIT #{limit}")
    List<KnowledgeBase> searchByKeyword(@Param("keyword") String keyword, @Param("limit") int limit);
}
