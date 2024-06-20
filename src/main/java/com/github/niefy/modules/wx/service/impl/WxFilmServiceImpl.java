package com.github.niefy.modules.wx.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.niefy.modules.wx.dao.WxFilmInfoMapper;
import com.github.niefy.modules.wx.entity.WxFilmInfo;
import com.github.niefy.modules.wx.service.WxFilmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author zhangdongxu
 * @Description create bean
 * @Date 2024/6/12 17:07
 * @Version 1.0
 */
@Service
public class WxFilmServiceImpl extends ServiceImpl<WxFilmInfoMapper, WxFilmInfo> implements WxFilmService {

    @Autowired
    WxFilmInfoMapper wxFilmInfoMapper;

    @Override
    public List<WxFilmInfo> queryFilmList(List<String> syncStatus) {
        List<WxFilmInfo> wxFilmInfoList = wxFilmInfoMapper.selectList(new LambdaQueryWrapper<WxFilmInfo>().in(WxFilmInfo::getSyncStatus, syncStatus)
                .orderByDesc(WxFilmInfo::getUpdateTime));
        return wxFilmInfoList;
    }


    @Override
    public List<WxFilmInfo> queryFilmInfoListForCategory(List<String> categoryList, String syncStatus) {

        List<WxFilmInfo> wxFilmInfoList = wxFilmInfoMapper.selectList(new LambdaQueryWrapper<WxFilmInfo>().in(WxFilmInfo::getFilmType, categoryList)
                .eq(WxFilmInfo::getSyncStatus, syncStatus));
        return wxFilmInfoList;
    }
}
