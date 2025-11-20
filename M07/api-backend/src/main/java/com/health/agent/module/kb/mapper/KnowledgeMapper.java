package com.health.agent.module.kb.mapper;

import com.health.agent.module.kb.entity.Knowledge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KnowledgeMapper {
    int insert(Knowledge knowledge);

    List<Knowledge> fulltextSearch(@Param("query") String query,
                                   @Param("category") String category,
                                   @Param("limit") int limit);

    List<Knowledge> likeSearch(@Param("query") String query,
                               @Param("category") String category,
                               @Param("limit") int limit);
}