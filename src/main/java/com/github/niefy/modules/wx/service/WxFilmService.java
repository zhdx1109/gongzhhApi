package com.github.niefy.modules.wx.service;

import com.github.niefy.modules.wx.entity.WxFilmInfo;
import com.github.niefy.modules.wx.entity.WxFilmSubInfo;

import java.util.List;

/**
 * @Author zhangdongxu
 * @Description create bean
 * @Date 2024/6/12 17:00
 * @Version 1.0
 */
public interface WxFilmService {
    //获取列表
    List<WxFilmInfo>queryFilmList(List<String> syncStatus);

    List<WxFilmInfo> queryFilmInfoListForCategory(List<String> categoryList,String syncSytatus);

}
