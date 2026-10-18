package com.gxnu.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gxnu.library.entity.ViolationRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 违规记录 Mapper
 */
@Mapper
public interface ViolationRecordMapper extends BaseMapper<ViolationRecord> {
}
