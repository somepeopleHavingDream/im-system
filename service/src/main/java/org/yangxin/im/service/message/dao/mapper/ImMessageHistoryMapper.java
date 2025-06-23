package org.yangxin.im.service.message.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;
import org.yangxin.im.service.message.dao.ImMessageHistoryEntity;

import java.util.Collection;

@Repository
public interface ImMessageHistoryMapper extends BaseMapper<ImMessageHistoryEntity> {

    /**
     * 批量插入（mysql）
     */
    Integer insertBatchSomeColumn(Collection<ImMessageHistoryEntity> entityList);
}

