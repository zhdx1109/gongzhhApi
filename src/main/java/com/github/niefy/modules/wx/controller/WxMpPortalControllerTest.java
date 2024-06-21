package com.github.niefy.modules.wx.controller;

import com.github.niefy.modules.wx.entity.WxFilmInfo;
import com.github.niefy.modules.wx.service.impl.WxFilmProcessServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 微信消息
 * @author Binary Wang
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/wx/msgTest")
@Api(tags = {"微信消息 - 腾讯会调用"})
public class WxMpPortalControllerTest {
    private final WxMpService wxMpService;
    private final WxMpMessageRouter messageRouter;

    @Autowired
    WxFilmProcessServiceImpl wxFilmProcessService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @GetMapping(value = "/{appid}",produces = "text/plain;charset=utf-8")
    @ApiOperation(value = "微信服务器的认证消息",notes = "公众号接入开发模式时腾讯调用此接口")
    public String authGet(@PathVariable String appid) {
        System.out.println("start===getTest=======");
        return "appid:"+appid;
    }

    //此方法用于添加ResoInfoURL资源
    @PostMapping(value = "/addTaskResourceProcess")
    void addTaskResourceProcess(){
        wxFilmProcessService.addTaskResourceProcess();
    }

    //此方法用于生成自动回复
    @PostMapping(value = "/addMsgReplyRuleProcess")
    void addMsgReplyRuleProcess(){
        wxFilmProcessService.addMsgReplyRuleProcess();
    }

    //此方法用于生成资源目录
    @PostMapping(value = "/addCategoryProcessor")
    void addCategoryProcessor(){
        List<String> strings = Arrays.asList("1", "2", "3");
        wxFilmProcessService.addCategoryProcessor(strings,null,"all");
    }

    //此方法用于新增addFilm资源
    @PostMapping(value = "/addFilmInfo")
    void addFilmInfo(WxFilmInfo wxFilmInfo) {
        wxFilmProcessService.addFilmInfo(String.valueOf(wxFilmInfo.getFilmType()),wxFilmInfo.getFilmId(),wxFilmInfo.getFilmName());
    }

    //此方法用于删除需要的资源
    @PostMapping(value = "/delFilmResources")
    void delFilmResources(WxFilmInfo wxFilmInfo) {
        wxFilmProcessService.delFilmResources(String.valueOf(wxFilmInfo.getFilmType()),wxFilmInfo.getFilmId(),wxFilmInfo.getFilmName());
    }






//    @PostMapping( value = "/clearQuota")
//    @ApiOperation(value = "微信清空调用次数", notes = "微信清空调用次数")
//    public String post(@RequestBody String appid){
//        logger.info("clear quota param:{}",appid);
//        try {
////            wxMpService.switchoverTo(appid);
//            wxMpService.clearQuota(appid);
//            return "ok";
//        } catch (WxErrorException e) {
//            e.printStackTrace();
//            return "error";
//        }
//    }





}
