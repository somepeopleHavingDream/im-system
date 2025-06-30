package org.yangxin.im.service.conversation.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;
import org.yangxin.im.service.conversation.dao.ImConversationSetEntity;

@SuppressWarnings("SqlDialectInspection")
@Repository
public interface ImConversationSetMapper extends BaseMapper<ImConversationSetEntity> {
    @Update(" update im_conversation_set set readed_sequence = #{readedSequence},sequence = #{sequence} " +
            " where conversation_id = #{conversationId} and app_id = #{appId} AND readed_sequence < #{readedSequence}")
    void readMark(ImConversationSetEntity imConversationSetEntity);
}
