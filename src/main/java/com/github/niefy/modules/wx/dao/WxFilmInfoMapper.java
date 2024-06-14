package com.github.niefy.modules.wx.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.niefy.modules.wx.entity.WxFilmInfo;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author zhangdongxu
 * @Description create bean
 * @Date 2024/6/12 16:55
 * @Version 1.0
 */

@Mapper
@CacheNamespace(flushInterval = 300000L)//缓存五分钟过期
public interface WxFilmInfoMapper  extends BaseMapper<WxFilmInfo> {
}
