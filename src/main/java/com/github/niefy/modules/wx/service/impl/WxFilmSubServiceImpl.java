package com.github.niefy.modules.wx.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.niefy.modules.wx.dao.WxFilmSubInfoMapper;
import com.github.niefy.modules.wx.entity.WxFilmSubInfo;
import com.github.niefy.modules.wx.service.WxFilmService;
import com.github.niefy.modules.wx.service.WxFilmSubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author zhangdongxu
 * @Description create bean
 * @Date 2024/6/12 17:18
 * @Version 1.0
 */

@Service
public class WxFilmSubServiceImpl extends ServiceImpl<WxFilmSubInfoMapper, WxFilmSubInfo> implements WxFilmSubService {
    @Autowired
    WxFilmSubInfoMapper wxFilmSubInfoMapper;

    @Override
    public List<WxFilmSubInfo> querySubList(List<Integer> parentIds) {
        List<WxFilmSubInfo> wxFilmSubInfos = wxFilmSubInfoMapper.selectList(new QueryWrapper<WxFilmSubInfo>()
                .in(null != parentIds && parentIds.size() > 0, "parent_id", parentIds)
                .orderByAsc("priority")
            );
        return wxFilmSubInfos;
    }
}
