package com.github.niefy.modules.wx.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.niefy.common.utils.Json;
import com.github.niefy.modules.wx.dao.MsgReplyRuleMapper;
import com.github.niefy.modules.wx.dao.WxTaskInfoMapper;
import com.github.niefy.modules.wx.dao.WxTaskResoInfoMapper;
import com.github.niefy.modules.wx.entity.*;
import com.github.niefy.modules.wx.service.WxFilmService;
import com.github.niefy.modules.wx.service.WxFilmSubService;
import io.netty.util.internal.StringUtil;
import io.swagger.models.auth.In;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @Author zhangdongxu
 * @Description create bean
 * @Date 2024/6/12 17:43
 * @Version 1.0
 */
@Service("wxFilmProcessService")
public class WxFilmProcessServiceImpl {
    @Autowired
    WxFilmService wxFilmService;
    @Autowired
    WxFilmSubService wxFilmSubService;

    @Autowired
    WxTaskResoInfoMapper wxTaskResoInfoMapper;

    @Autowired
    WxTaskInfoMapper wxTaskInfoMapper;

    @Autowired
    MsgReplyRuleMapper msgReplyRuleMapper;


    public void addTaskResourceProcess() {
        //当前方法只生成资源的秘钥和url,汇总的形式需要单独考虑
        List<WxFilmInfo> wxFilmInfoList = wxFilmService.queryFilmList();
        //处理孤品影视
        List<WxFilmInfo> isSingleWxFilmInfoList = wxFilmInfoList.stream().filter(x -> "1".equalsIgnoreCase(x.getIsSingle())).collect(Collectors.toList());

        isSingleWxFilmInfoList.stream().forEach(x -> {
            String filmNameDec = x.getFilmNameDec();
            String strCurrURL = filmNameDec + "url";//影url
            WxTaskResoInfo wxTaskResoInfo = new WxTaskResoInfo();
            wxTaskResoInfo.setTaskName(strCurrURL);
            wxTaskResoInfo.setUpdateTime(new Date());
            wxTaskResoInfo.setStatus(true);
            wxTaskResoInfo.setSyncUsed(false);
            wxTaskResoInfoMapper.insert(wxTaskResoInfo);
        });


        //取出需要的Map<Integer,WxFilmInfo> //此处的作用是将作品的id和bean进行对等。过滤掉孤品影视
        ConcurrentMap<Integer, WxFilmInfo> keyWxFilmInfoMap = wxFilmInfoList.stream().filter(x -> !"1".equalsIgnoreCase(x.getIsSingle())).collect(Collectors.toConcurrentMap(WxFilmInfo::getFilmId, Function.identity()));
        //将其取出需要的ids
//        List<Integer> collect = wxFilmInfoList.stream().map(WxFilmInfo::getFilmId).collect(Collectors.toList());

        List<Integer> collect = Arrays.asList(keyWxFilmInfoMap.keySet().toArray(new Integer[0]));

        List<WxFilmSubInfo> wxFilmSubInfos = wxFilmSubService.querySubList(collect);
        //需要对其进行分组 :以parentId进行分组,以priority进行升序 两种方式 先分组 后排序  先排序后分组
        //1 先排序后分组   验证两种结果是一样的。
        Map<Integer, List<WxFilmSubInfo>> collect1 = wxFilmSubInfos.stream().sorted(Comparator.comparing(WxFilmSubInfo::getPriority)).collect(Collectors.groupingBy(WxFilmSubInfo::getParentId));
        System.out.println("先排序后分组:" + Json.toJsonString(collect1));
        //2 先分组后排序 将分组后的数据进行内部排序
//        Map<Integer, List<WxFilmSubInfo>> collect2 = wxFilmSubInfos.stream().collect(Collectors.groupingBy(x -> x.getParentId()));
//        collect2.keySet().forEach(key->collect2.computeIfPresent(key,(k,v)->v.stream().sorted(Comparator.comparing(WxFilmSubInfo::getPriority)).collect(Collectors.toList())));
//        System.out.println("先分组后排序:"+Json.toJsonString(collect2));
        Integer[] integers = keyWxFilmInfoMap.keySet().toArray(new Integer[0]);

        Integer[] keys = collect1.keySet().toArray(new Integer[0]);
        for (int c = 0; c < keys.length; c++) {
            Integer key = keys[c];
            List<WxFilmSubInfo> wxFilmSubInfos1 = collect1.get(key);
            Integer integer = integers[c];
            WxFilmInfo wxFilmInfo = keyWxFilmInfoMap.get(integer);

            String strCurrURL = "";

            for (int i = 0; i < wxFilmSubInfos1.size(); i++) {
                WxFilmSubInfo wxFilmSubInfo = wxFilmSubInfos1.get(i);
                String filmNameDec = "", filmName = "", startValue = "";
                String followUpValue = wxFilmInfo.getFollowUpValue();
                filmNameDec = wxFilmSubInfo.getFilmNameDec();
                filmName = wxFilmSubInfo.getFilmName();
                startValue = wxFilmInfo.getStartValue();
                String seriesValue = wxFilmSubInfo.getSeriesValue();
                if ("1".equals(wxFilmInfo.getIsFollowUp())) { //1 是
                    String[] parts = seriesValue.split("-");
                    String lastPart = parts[parts.length - 1]; // 取数组的最后一个元素
                    Integer numberInt = Integer.parseInt(lastPart); // 转换为Integer

                    // 生成从1至numberInt的List数组
                    List<Integer> numbersList = IntStream.rangeClosed(1, numberInt).boxed().collect(Collectors.toList());

                    String finalFilmName = filmName;
                    String finalFilmNameDec = filmNameDec;
                    for (int j = 0; j < numbersList.size(); j++) {
                        //拼接
                        String strRes = finalFilmNameDec + "-" + String.valueOf(numbersList.get(j));//1-1
                        String strCurr = finalFilmName + strRes; //斯巴达1-1  关键字
                        strCurrURL = strCurr + "url";//斯巴达1-1url

                        WxTaskResoInfo wxTaskResoInfo = new WxTaskResoInfo();
                        wxTaskResoInfo.setTaskName(strCurrURL);
                        wxTaskResoInfo.setUpdateTime(new Date());
                        wxTaskResoInfo.setStatus(true);
                        wxTaskResoInfo.setSyncUsed(false);
                        wxTaskResoInfoMapper.insert(wxTaskResoInfo);
                    }

                } else {
                    strCurrURL = filmNameDec + "url";
                    WxTaskResoInfo wxTaskResoInfo = new WxTaskResoInfo();
                    wxTaskResoInfo.setTaskName(strCurrURL);
                    wxTaskResoInfo.setUpdateTime(new Date());
                    wxTaskResoInfo.setStatus(true);
                    wxTaskResoInfo.setSyncUsed(false);
                    wxTaskResoInfoMapper.insert(wxTaskResoInfo);
                }
            }
        }

    }



    public void addMsgReplyRuleProcess() {
        //当前方法只生成资源的秘钥和url,汇总的形式需要单独考虑
        List<WxFilmInfo> wxFilmInfoList = wxFilmService.queryFilmList();
        //处理孤品影视
        List<WxFilmInfo> isSingleWxFilmInfoList = wxFilmInfoList.stream().filter(x -> "1".equalsIgnoreCase(x.getIsSingle())).collect(Collectors.toList());

        isSingleWxFilmInfoList.stream().forEach(x -> {
            String filmNameDec = x.getFilmNameDec();
            String strCurrMY = filmNameDec + "秘钥";//影秘钥
            String strCurrURL = filmNameDec + "url";//影url
            syncTable(filmNameDec, strCurrMY, strCurrURL,null, null);
        });


        //取出需要的Map<Integer,WxFilmInfo> //此处的作用是将作品的id和bean进行对等。过滤掉孤品影视
        ConcurrentMap<Integer, WxFilmInfo> keyWxFilmInfoMap = wxFilmInfoList.stream().filter(x -> !"1".equalsIgnoreCase(x.getIsSingle())).collect(Collectors.toConcurrentMap(WxFilmInfo::getFilmId, Function.identity()));
        //将其取出需要的ids
//        List<Integer> collect = wxFilmInfoList.stream().map(WxFilmInfo::getFilmId).collect(Collectors.toList());

        List<Integer> collect = Arrays.asList(keyWxFilmInfoMap.keySet().toArray(new Integer[0]));

        List<WxFilmSubInfo> wxFilmSubInfos = wxFilmSubService.querySubList(collect);
        //需要对其进行分组 :以parentId进行分组,以priority进行升序 两种方式 先分组 后排序  先排序后分组
        //1 先排序后分组   验证两种结果是一样的。
        Map<Integer, List<WxFilmSubInfo>> collect1 = wxFilmSubInfos.stream().sorted(Comparator.comparing(WxFilmSubInfo::getPriority)).collect(Collectors.groupingBy(WxFilmSubInfo::getParentId));
        System.out.println("先排序后分组:" + Json.toJsonString(collect1));
        //2 先分组后排序 将分组后的数据进行内部排序
//        Map<Integer, List<WxFilmSubInfo>> collect2 = wxFilmSubInfos.stream().collect(Collectors.groupingBy(x -> x.getParentId()));
//        collect2.keySet().forEach(key->collect2.computeIfPresent(key,(k,v)->v.stream().sorted(Comparator.comparing(WxFilmSubInfo::getPriority)).collect(Collectors.toList())));
//        System.out.println("先分组后排序:"+Json.toJsonString(collect2));
        Integer[] integers = keyWxFilmInfoMap.keySet().toArray(new Integer[0]);

        Integer[] keys = collect1.keySet().toArray(new Integer[0]);
        for (int c = 0; c < keys.length; c++) {
            Integer key = keys[c];
            List<WxFilmSubInfo> wxFilmSubInfos1 = collect1.get(key);
            Integer integer = integers[c];
            WxFilmInfo wxFilmInfo = keyWxFilmInfoMap.get(integer);

            String strCurrURL = "";

            for (int i = 0; i < wxFilmSubInfos1.size(); i++) {
                WxFilmSubInfo wxFilmSubInfo = wxFilmSubInfos1.get(i);
                String filmNameDec = "", filmName = "", startValue = "";
                String followUpValue = wxFilmInfo.getFollowUpValue();
                filmNameDec = wxFilmSubInfo.getFilmNameDec();
                filmName = wxFilmSubInfo.getFilmName();
                startValue = wxFilmInfo.getStartValue();
                String seriesValue = wxFilmSubInfo.getSeriesValue();
                if ("1".equals(wxFilmInfo.getIsFollowUp())) { //1 是
                    String[] parts = seriesValue.split("-");
                    String lastPart = parts[parts.length - 1]; // 取数组的最后一个元素
                    Integer numberInt = Integer.parseInt(lastPart); // 转换为Integer

                    // 生成从1至numberInt的List数组
                    List<Integer> numbersList = IntStream.rangeClosed(1, numberInt).boxed().collect(Collectors.toList());

                    String finalFilmName = filmName;
                    String finalFilmNameDec = filmNameDec;
                    for (int j = 0; j < numbersList.size(); j++) {
                        //拼接
                        String strRes = finalFilmNameDec + "-" + String.valueOf(numbersList.get(j));//1-1
                        String strCurr = finalFilmName + strRes; //斯巴达1-1  关键字
                        String strCurrMY = strCurr + "秘钥";//斯巴达1-1秘钥
                        strCurrURL = strCurr + "url";//斯巴达1-1url
                        if (startValue.equalsIgnoreCase(strRes)) {
                            int m = j; //重新赋值 防止j混乱
                            Integer nextInt = numbersList.get(m + 1);
                            String strNext = finalFilmName + finalFilmNameDec + "-" + String.valueOf(nextInt);
                            String strNextA = "<a href=\"weixin://bizmsgmenu?msgmenucontent=" + strNext + "&msgmenuid=1\">" + strNext + "</a>";
                            syncTable(strCurr, strCurrMY, strCurrURL,null, strNextA);

                        } else if (!followUpValue.equalsIgnoreCase(strRes)) {
                            int m = j;
                            String strNext = "";
                            String strPre = "";
                            if (m == 0) {
                                WxFilmSubInfo wxFilmSubInfo1 = wxFilmSubInfos1.get(i - 1);
                                String seriesValue1 = wxFilmSubInfo1.getSeriesValue();
                                String filmNameDec1 = wxFilmSubInfo1.getFilmNameDec();
                                String[] split = seriesValue1.split("-");
                                strPre = finalFilmName + filmNameDec1 + "-" + split[1];
                            } else {
                                strPre = finalFilmName + finalFilmNameDec + "-" + String.valueOf(numbersList.get(m - 1));
                            }

                            if (m == (numbersList.size() - 1)) {
                                WxFilmSubInfo wxFilmSubInfo1 = wxFilmSubInfos1.get(i + 1);
                                finalFilmNameDec = wxFilmSubInfo1.getFilmNameDec();
                                strNext = finalFilmName + finalFilmNameDec + "-1";

                            } else {
                                strNext = finalFilmName + finalFilmNameDec + "-" + String.valueOf(numbersList.get(m + 1));
                            }
                            String strPreA = "<a href=\"weixin://bizmsgmenu?msgmenucontent=" + strPre + "&msgmenuid=1\">" + strPre + "</a>";
                            String strNextA = "<a href=\"weixin://bizmsgmenu?msgmenucontent=" + strNext + "&msgmenuid=1\">" + strNext + "</a>";
                            syncTable(strCurr, strCurrMY, strCurrURL,strPreA, strNextA);

                        } else {
                            int m = j;
                            String strPre = finalFilmName + finalFilmNameDec + "-" + String.valueOf(numbersList.get(m - 1));
                            String strPreA = "<a href=\"weixin://bizmsgmenu?msgmenucontent=" + strPre + "&msgmenuid=1\">" + strPre + "</a>";
                            syncTable(strCurr, strCurrMY, strCurrURL,strPreA, null);
                        }
                    }

                } else {
                   String strCurrMY = filmNameDec+"秘钥";
                    strCurrURL = filmNameDec + "url";
                    if (startValue.equalsIgnoreCase(seriesValue)) {
                        WxFilmSubInfo wxFilmSubNextInfo = wxFilmSubInfos1.get(i + 1);
                        String strNext = wxFilmSubNextInfo.getFilmNameDec();
                        String strNextA = "<a href=\"weixin://bizmsgmenu?msgmenucontent=" + strNext + "&msgmenuid=1\">" + strNext + "</a>";
                        syncTable(filmNameDec, strCurrMY, strCurrURL,null, strNextA);
                    } else if (!followUpValue.equalsIgnoreCase(seriesValue)) {
                        String strPre = wxFilmSubInfos1.get(i - 1).getFilmNameDec();
                        String strNext = wxFilmSubInfos1.get(i + 1).getFilmNameDec();
                        String strPreA = "<a href=\"weixin://bizmsgmenu?msgmenucontent=" + strPre + "&msgmenuid=1\">" + strPre + "</a>";
                        String strNextA = "<a href=\"weixin://bizmsgmenu?msgmenucontent=" + strNext + "&msgmenuid=1\">" + strNext + "</a>";
                        syncTable(filmNameDec, strCurrMY, strCurrURL,strPreA, strNextA);
                    } else {
                        String strPre = wxFilmSubInfos1.get(i - 1).getFilmNameDec();
                        String strPreA = "<a href=\"weixin://bizmsgmenu?msgmenucontent=" + strPre + "&msgmenuid=1\">" + strPre + "</a>";
                        syncTable(filmNameDec, strCurrMY, strCurrURL,strPreA, null);
                    }
                }

            }


        }
        ;

    }


    /**
     *
     * @param filmNameDec 关键词
     * @param strCurrMY 当前秘钥
     * @param strCurrURL 当前url
     * @param strPreA 上一个
     * @param strNextA 下一个
     */
    public void syncTable(String filmNameDec, String strCurrMY, String strCurrURL,String strPreA,String strNextA) {

        try {
            WxTaskResoInfo wxTaskResoInfo = wxTaskResoInfoMapper.selectOne(new LambdaQueryWrapper<WxTaskResoInfo>().eq(WxTaskResoInfo::getTaskName, strCurrURL));
            if (null != wxTaskResoInfo&& !StringUtil.isNullOrEmpty(wxTaskResoInfo.getTaskResoUrl()) ){
                List<WxTaskInfo> wxTaskInfos = wxTaskInfoMapper.selectList(new LambdaQueryWrapper<WxTaskInfo>().eq(WxTaskInfo::isSyncUsed, false).eq(WxTaskInfo::isStatus, true));
                if (null!=wxTaskInfos&&wxTaskInfos.size()>0){
                    //获取当前最小的taskId
                    Optional<WxTaskInfo> min = wxTaskInfos.stream().min(Comparator.comparing(WxTaskInfo::getTaskId));
                    WxTaskInfo wxTaskInfo = min.get();
                    String taskName = wxTaskInfo.getTaskName();//打开QQ阅读->
                    String taskUrlCode = wxTaskInfo.getTaskUrlCode();//穿成了反派崽崽的亲妈
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String format = sdf.format(new Date());
                    //新增秘钥
                    MsgReplyRule msgMy = new MsgReplyRule();
                    msgMy.setRuleName(strCurrMY);
                    msgMy.setMatchValue(filmNameDec);
                    msgMy.setExactMatch(true);
                    msgMy.setReplyType("text");
                    if (!StringUtil.isNullOrEmpty(strPreA)){
                        taskName=  taskName+"\n"+strPreA;
                    }
                    if (!StringUtil.isNullOrEmpty(strNextA)){
                        taskName= taskName+"\n"+strNextA;
                    }
                    msgMy.setReplyContent(taskName);
                    msgMy.setStatus(true);
                    msgMy.setPriority(0);
                    msgMy.setDesc(format+"auto");
                    msgMy.setUpdateTime(new Date());
                    msgReplyRuleMapper.insert(msgMy);
                    //新增url
                    MsgReplyRule msgURL = new MsgReplyRule();
//                    BeanUtils.copyProperties(msgMy, msgURL);
                    msgURL.setRuleName(strCurrURL);
                    msgURL.setMatchValue(taskUrlCode);
                    msgURL.setReplyContent(wxTaskResoInfo.getTaskResoUrl());
                    msgURL.setExactMatch(true);
                    msgURL.setReplyType("text");
                    msgURL.setStatus(true);
                    msgURL.setPriority(0);
                    msgURL.setDesc(format+"auto");
                    msgURL.setUpdateTime(new Date());
                    msgReplyRuleMapper.insert(msgURL);
                    //更新
                    wxTaskInfo.setSyncUsed(true);
                    wxTaskInfoMapper.updateById(wxTaskInfo);
                    wxTaskResoInfo.setTaskId(wxTaskInfo.getTaskId());
                    wxTaskResoInfo.setSyncUsed(true);
                    wxTaskResoInfoMapper.updateById(wxTaskResoInfo);
                }

            }
        } catch (BeansException e) {
            e.printStackTrace();
        }
    }


}
