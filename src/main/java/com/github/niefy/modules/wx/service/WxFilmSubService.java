package com.github.niefy.modules.wx.service;

import com.github.niefy.modules.wx.entity.WxFilmSubInfo;

import java.util.List;

/**
 * @Author zhangdongxu
 * @Description create bean
 * @Date 2024/6/12 17:10
 * @Version 1.0
 */
public interface WxFilmSubService {
    //根据父Id获取子信息列表
    List<WxFilmSubInfo> querySubList(List<Integer> parentIds);
}
