package org.yangxin.im.service.group.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import org.yangxin.im.service.group.dao.ImGroupMemberEntity;
import org.yangxin.im.service.group.model.req.GroupMemberDto;

import java.util.List;

@SuppressWarnings("SqlDialectInspection")
@Repository
public interface ImGroupMemberMapper extends BaseMapper<ImGroupMemberEntity> {

    @Select("select group_id from im_group_member where app_id = #{appId} AND member_id = #{memberId} ")
    List<String> getJoinedGroupId(Integer appId, String memberId);


    @Results({
            @Result(column = "member_id", property = "memberId"),
//            @Result(column = "speak_flag", property = "speakFlag"),
            @Result(column = "speak_date", property = "speakDate"),
            @Result(column = "role", property = "role"),
            @Result(column = "alias", property = "alias"),
            @Result(column = "join_time", property = "joinTime"),
            @Result(column = "join_type", property = "joinType")
    })
    @Select("select " +
            " member_id, " +
//            " speak_flag,  " +
            " speak_date,  " +
            " role, " +
            " alias, " +
            " join_time ," +
            " join_type " +
            " from im_group_member where app_id = #{appId} AND group_id = #{groupId} ")
    List<GroupMemberDto> getGroupMember(Integer appId, String groupId);

    @Select("select " +
            " member_id " +
            " from im_group_member where app_id = #{appId} AND group_id = #{groupId} and role != 3")
    List<String> getGroupMemberId(Integer appId, String groupId);


    @Results({
            @Result(column = "member_id", property = "memberId"),
//            @Result(column = "speak_flag", property = "speakFlag"),
            @Result(column = "role", property = "role")
//            @Result(column = "alias", property = "alias"),
//            @Result(column = "join_time", property = "joinTime"),
//            @Result(column = "join_type", property = "joinType")
    })
    @Select("select " +
            " member_id, " +
//            " speak_flag,  " +
            " role " +
//            " alias, " +
//            " join_time ," +
//            " join_type " +
            " from im_group_member where app_id = #{appId} AND group_id = #{groupId} and role in (1,2) ")
    List<GroupMemberDto> getGroupManager(String groupId, Integer appId);

    @Select("select group_id from im_group_member where app_id = #{appId} AND member_id = #{memberId} and role != " +
            "#{role}")
    List<String> syncJoinedGroupId(Integer appId, String memberId, int role);
}
