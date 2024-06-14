package com.github.niefy.modules.wx.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.niefy.modules.wx.entity.WxTaskInfo;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author zhangdongxu
 * @Description create bean
 * @Date 2024/6/14 12:15
 * @Version 1.0
 */

@Mapper
@CacheNamespace(flushInterval = 300000L)//缓存五分钟过期
public interface WxTaskInfoMapper  extends BaseMapper<WxTaskInfo> {
}
