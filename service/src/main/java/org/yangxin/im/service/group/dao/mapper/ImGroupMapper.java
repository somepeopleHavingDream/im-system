package org.yangxin.im.service.group.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import org.yangxin.im.service.group.dao.ImGroupEntity;

import java.util.Collection;

@SuppressWarnings("SqlDialectInspection")
@Repository
public interface ImGroupMapper extends BaseMapper<ImGroupEntity> {

    @Select(" <script> " +
            " select max(sequence) from im_group where app_id = #{appId} and group_id in " +
            "<foreach collection='groupId' index='index' item='id' separator=',' close=')' open='('>" +
            " #{id} " +
            "</foreach>" +
            " </script> ")
    Long getGroupMaxSeq(Collection<String> groupId, Integer appId);
}
