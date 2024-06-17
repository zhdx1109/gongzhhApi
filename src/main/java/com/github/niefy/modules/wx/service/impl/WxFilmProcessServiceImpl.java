package com.github.niefy.modules.wx.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.niefy.common.utils.Json;
import com.github.niefy.modules.wx.dao.MsgReplyRuleMapper;
import com.github.niefy.modules.wx.dao.WxFilmInfoMapper;
import com.github.niefy.modules.wx.dao.WxTaskInfoMapper;
import com.github.niefy.modules.wx.dao.WxTaskResoInfoMapper;
import com.github.niefy.modules.wx.entity.*;
import com.github.niefy.modules.wx.service.WxFilmService;
import com.github.niefy.modules.wx.service.WxFilmSubService;
import com.sun.org.apache.bcel.internal.generic.ARETURN;
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

    @Autowired
    WxFilmInfoMapper wxFilmInfoMapper;


    public void addTaskResourceProcess() {
        //当前方法只生成资源的秘钥和url,汇总的形式需要单独考虑
        List<String> strings = Arrays.asList("0");
        List<WxFilmInfo> wxFilmInfoList = wxFilmService.queryFilmList(strings);
        //处理孤品影视
        List<WxFilmInfo> isSingleWxFilmInfoList = wxFilmInfoList.stream().filter(x -> "1".equalsIgnoreCase(x.getIsSingle())).collect(Collectors.toList());

        // 需要将 resoInfo中的所有 taskName捞出来 进行判断是否已经存在，不存在 则新增
        List<WxTaskResoInfo> wxTaskResoInfos = wxTaskResoInfoMapper.selectList(new LambdaQueryWrapper<WxTaskResoInfo>().orderByDesc(WxTaskResoInfo::getUpdateTime));
        List<String> getTaskNameList = null;
        if (null != wxTaskResoInfos && wxTaskResoInfos.size() > 0) {
            getTaskNameList = wxTaskResoInfos.stream().map(WxTaskResoInfo::getTaskName).collect(Collectors.toList());
        }

        List<String> finalGetTaskNameList = getTaskNameList;

        isSingleWxFilmInfoList.stream().forEach(x -> {
            String filmNameDec = x.getFilmNameDec();
            String strCurrURL = filmNameDec + "url";//影url
            if (null != finalGetTaskNameList && finalGetTaskNameList.size() > 0) {
                if (!finalGetTaskNameList.contains(filmNameDec)) {
                    WxTaskResoInfo wxTaskResoInfo = new WxTaskResoInfo();
                    wxTaskResoInfo.setTaskName(strCurrURL);
                    wxTaskResoInfo.setUpdateTime(new Date());
                    wxTaskResoInfo.setStatus(true);
                    wxTaskResoInfo.setSyncUsed(false);
                    wxTaskResoInfo.setParentId(x.getFilmId());
                    WxTaskResoInfo wxTaskResoInfo1 = wxTaskResoInfoMapper.selectOne(new LambdaQueryWrapper<WxTaskResoInfo>().eq(WxTaskResoInfo::getTaskName, strCurrURL));
                    if (null==wxTaskResoInfo1){
                        wxTaskResoInfoMapper.insert(wxTaskResoInfo);
                        finalGetTaskNameList.add(filmNameDec);
                    }
                    //更新 film_info.sync_status=1
                    x.setSyncStatus("1");
                    x.setUpdateTime(new Date());
                    wxFilmInfoMapper.updateById(x);

                }
            }
        });


        //取出需要的Map<Integer,WxFilmInfo> //此处的作用是将作品的id和bean进行对等。过滤掉孤品影视
        ConcurrentMap<Integer, WxFilmInfo> keyWxFilmInfoMap = wxFilmInfoList.stream().filter(x -> !"1".equalsIgnoreCase(x.getIsSingle())).collect(Collectors.toConcurrentMap(WxFilmInfo::getFilmId, Function.identity()));
        //将其取出需要的ids
//        List<Integer> collect = wxFilmInfoList.stream().map(WxFilmInfo::getFilmId).collect(Collectors.toList());
            if (null==keyWxFilmInfoMap||keyWxFilmInfoMap.size()==0){
                return ;
            }
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

        if (null==collect1||collect1.size()==0){return;}

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
                if ("1".equals(wxFilmInfo.getIsFollowUp())) { //1 是连续剧类型
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
                        extracted(finalGetTaskNameList, wxFilmInfo, strCurrURL, strCurrURL, followUpValue, strRes,wxFilmSubInfo);
                    }
                } else {
                    strCurrURL = filmNameDec + "url";
                    extracted(finalGetTaskNameList, wxFilmInfo, strCurrURL, filmNameDec, followUpValue, seriesValue,wxFilmSubInfo);
                }
            }
        }

    }

    private void extracted(List<String> finalGetTaskNameList, WxFilmInfo wxFilmInfo, String strCurrURL, String filmNameDec, String followUpValue, String strRes,WxFilmSubInfo wxFilmSubInfo) {
        if (null != finalGetTaskNameList && finalGetTaskNameList.size() > 0) {
            if (!finalGetTaskNameList.contains(strCurrURL)) {
                WxTaskResoInfo wxTaskResoInfo = new WxTaskResoInfo();
                wxTaskResoInfo.setTaskName(strCurrURL);
                wxTaskResoInfo.setUpdateTime(new Date());
                wxTaskResoInfo.setStatus(true);
                wxTaskResoInfo.setSyncUsed(false);
                wxTaskResoInfo.setParentId(wxFilmSubInfo.getFilmSubId());
                WxTaskResoInfo wxTaskResoInfo1 = wxTaskResoInfoMapper.selectOne(new LambdaQueryWrapper<WxTaskResoInfo>().eq(WxTaskResoInfo::getTaskName, strCurrURL));
                if (null==wxTaskResoInfo1){
                    wxTaskResoInfoMapper.insert(wxTaskResoInfo);
                    finalGetTaskNameList.add(strCurrURL);
                }
                //如果是当前结束标识 -> 修改状态
                if (strRes.equalsIgnoreCase(followUpValue)) {
                    wxFilmInfo.setSyncStatus("1");
                    wxFilmInfo.setUpdateTime(new Date());
                    wxFilmInfoMapper.updateById(wxFilmInfo);
                }
            }
        }
    }


    public void addMsgReplyRuleProcess() {
        //当前方法只生成资源的秘钥和url,汇总的形式需要单独考虑  只有2 没有三  对应的状态应该是 0 1 2的状态为 reso表中资源缺少rul 导致3 部分未能匹配  4
        List<String> strings = Arrays.asList("1", "2", "3");
        List<WxFilmInfo> wxFilmInfoList = wxFilmService.queryFilmList(strings);
        Map<Integer, String> parentIdNoECount = new HashMap<>();
        //处理孤品影视
        List<WxFilmInfo> isSingleWxFilmInfoList = wxFilmInfoList.stream().filter(x -> "1".equalsIgnoreCase(x.getIsSingle())).collect(Collectors.toList());
        isSingleWxFilmInfoList.stream().forEach(x -> {
            String filmNameDec = x.getFilmNameDec();
            String strCurrMY = filmNameDec + "秘钥";//影秘钥
            String strCurrURL = filmNameDec + "url";//影url
            syncTable(filmNameDec, strCurrMY, strCurrURL, null, null, x.getFilmId(), parentIdNoECount);
            //更新film 状态为4
            if (!parentIdNoECount.containsKey(x.getFilmId())) {
                updateFilmStatus(x, "4");
            }
        });

        //取出需要的Map<Integer,WxFilmInfo> //此处的作用是将作品的id和bean进行对等。过滤掉孤品影视
        ConcurrentMap<Integer, WxFilmInfo> keyWxFilmInfoMap = wxFilmInfoList.stream().filter(x -> !"1".equalsIgnoreCase(x.getIsSingle())).collect(Collectors.toConcurrentMap(WxFilmInfo::getFilmId, Function.identity()));
        //将其取出需要的ids
//        List<Integer> collect = wxFilmInfoList.stream().map(WxFilmInfo::getFilmId).collect(Collectors.toList());
        if (null==keyWxFilmInfoMap||keyWxFilmInfoMap.size()==0){
            return ;
        }
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
        if (null==collect1||collect1.size()==0){return;}
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
                            syncTable(strCurr, strCurrMY, strCurrURL, null, strNextA, key, parentIdNoECount);

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
                            syncTable(strCurr, strCurrMY, strCurrURL, strPreA, strNextA, key, parentIdNoECount);

                        } else {
                            int m = j;
                            String strPre = finalFilmName + finalFilmNameDec + "-" + String.valueOf(numbersList.get(m - 1));
                            String strPreA = "<a href=\"weixin://bizmsgmenu?msgmenucontent=" + strPre + "&msgmenuid=1\">" + strPre + "</a>";
                            syncTable(strCurr, strCurrMY, strCurrURL, strPreA, null, key, parentIdNoECount);
                            if (!parentIdNoECount.containsKey(key)) {
                                updateFilmStatus(wxFilmInfo, "4");
                            }
                        }
                    }

                } else {
                    String strCurrMY = filmNameDec + "秘钥";
                    strCurrURL = filmNameDec + "url";
                    if (startValue.equalsIgnoreCase(seriesValue)) {
                        WxFilmSubInfo wxFilmSubNextInfo = wxFilmSubInfos1.get(i + 1);
                        String strNext = wxFilmSubNextInfo.getFilmNameDec();
                        String strNextA = "<a href=\"weixin://bizmsgmenu?msgmenucontent=" + strNext + "&msgmenuid=1\">" + strNext + "</a>";
                        syncTable(filmNameDec, strCurrMY, strCurrURL, null, strNextA, key, parentIdNoECount);
                    } else if (!followUpValue.equalsIgnoreCase(seriesValue)) {
                        String strPre = wxFilmSubInfos1.get(i - 1).getFilmNameDec();
                        String strNext = wxFilmSubInfos1.get(i + 1).getFilmNameDec();
                        String strPreA = "<a href=\"weixin://bizmsgmenu?msgmenucontent=" + strPre + "&msgmenuid=1\">" + strPre + "</a>";
                        String strNextA = "<a href=\"weixin://bizmsgmenu?msgmenucontent=" + strNext + "&msgmenuid=1\">" + strNext + "</a>";
                        syncTable(filmNameDec, strCurrMY, strCurrURL, strPreA, strNextA, key, parentIdNoECount);
                    } else {
                        String strPre = wxFilmSubInfos1.get(i - 1).getFilmNameDec();
                        String strPreA = "<a href=\"weixin://bizmsgmenu?msgmenucontent=" + strPre + "&msgmenuid=1\">" + strPre + "</a>";
                        syncTable(filmNameDec, strCurrMY, strCurrURL, strPreA, null, key, parentIdNoECount);
                        if (!parentIdNoECount.containsKey(key)) {
                            updateFilmStatus(wxFilmInfo, "4");
                        }
                    }
                }
            }
        }
        if (null != parentIdNoECount && parentIdNoECount.size() > 0) {
            for (WxFilmInfo wx : wxFilmInfoList) {
                if (parentIdNoECount.containsKey(wx.getFilmId())) {
                    String s = parentIdNoECount.get(wx.getFilmId());
                    wx.setSyncValue(s);
                    wx.setSyncStatus("2");
                    wxFilmInfoMapper.updateById(wx);
                }
            }

        }

    }

    //更新wxfilmInfo表状态
    void updateFilmStatus(WxFilmInfo wxFilmInfo, String status) {
        wxFilmInfo.setSyncStatus(status);
        wxFilmInfo.setUpdateTime(new Date());
        wxFilmInfoMapper.updateById(wxFilmInfo);
    }

    ;


    /**
     * @param filmNameDec 关键词
     * @param strCurrMY   当前秘钥
     * @param strCurrURL  当前url
     * @param strPreA     上一个
     * @param strNextA    下一个
     */
    public void syncTable(String filmNameDec, String strCurrMY, String strCurrURL, String strPreA, String strNextA, Integer key, Map<Integer, String> parentIdNoECount) {

        try {
            WxTaskResoInfo wxTaskResoInfo = wxTaskResoInfoMapper.selectOne(new LambdaQueryWrapper<WxTaskResoInfo>().eq(WxTaskResoInfo::getTaskName, strCurrURL));
            if (null != wxTaskResoInfo && !StringUtil.isNullOrEmpty(wxTaskResoInfo.getTaskResoUrl())) {
                boolean isExist = true;
                WxTaskInfo wxTaskInfo = null;
                String taskName = "";
                String taskUrlCode = "";
                MsgReplyRule msgReplyRule1 = msgReplyRuleMapper.selectOne(new LambdaQueryWrapper<MsgReplyRule>().eq(MsgReplyRule::getRuleName, strCurrURL));

                if (null != msgReplyRule1) {
                    return;
                }

                do {
                    // 取出重复的 matchVale 确保唯一
                    List<WxTaskInfo> wxTaskInfos = wxTaskInfoMapper.selectList(new LambdaQueryWrapper<WxTaskInfo>().eq(WxTaskInfo::isSyncUsed, false).eq(WxTaskInfo::isStatus, true));
                    if (null != wxTaskInfos && wxTaskInfos.size() > 0) {
                        //获取当前最小的taskId
                        Optional<WxTaskInfo> min = wxTaskInfos.stream().min(Comparator.comparing(WxTaskInfo::getTaskId));
                        wxTaskInfo = min.get();
                        taskName = wxTaskInfo.getTaskName();//打开QQ阅读->
                        taskUrlCode = wxTaskInfo.getTaskUrlCode();//穿成了反派崽崽的亲妈
                        //需要判断 taskUrlCode 是否重复，如果是需要删除词条数据。并重新查询
                        MsgReplyRule msgReplyRule = msgReplyRuleMapper.selectOne(new LambdaQueryWrapper<MsgReplyRule>().eq(MsgReplyRule::getMatchValue, taskUrlCode));
                        if (null != msgReplyRule) {
                            //根据id 删除对应的任务数据
                            wxTaskInfoMapper.deleteById(wxTaskInfo);
                            System.out.println("重复数据:" + Json.toJsonString(msgReplyRule));
                            isExist = false;
                        }
                    }

                } while (!isExist);

                //是否真的存在任务列表 为空 直接退出去
                if (null == wxTaskInfo) {
                    return;
                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String format = sdf.format(new Date());
                //新增秘钥
                MsgReplyRule msgMy = new MsgReplyRule();
                msgMy.setRuleName(strCurrMY);
                msgMy.setMatchValue(filmNameDec);
                msgMy.setExactMatch(true);
                msgMy.setReplyType("text");
                if (!StringUtil.isNullOrEmpty(strPreA)) {
                    taskName = taskName + "\n" + strPreA;
                }
                if (!StringUtil.isNullOrEmpty(strNextA)) {
                    taskName = taskName + "\n" + strNextA;
                }
                msgMy.setReplyContent(taskName);
                msgMy.setStatus(true);
                msgMy.setPriority(0);
                msgMy.setDesc(format + "auto");
                msgMy.setUpdateTime(new Date());
                msgMy.setParentId(key);
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
                msgURL.setDesc(format + "auto");
                msgURL.setUpdateTime(new Date());
                msgURL.setParentId(key);
                msgReplyRuleMapper.insert(msgURL);
                //更新
                wxTaskInfo.setSyncUsed(true);
                wxTaskInfoMapper.updateById(wxTaskInfo);
                wxTaskResoInfo.setTaskId(wxTaskInfo.getTaskId());
                wxTaskResoInfo.setSyncUsed(true);
                wxTaskResoInfoMapper.updateById(wxTaskResoInfo);

            } else {
                //只要存在 key获取到的reso_code 为空,需要将 film sync_status 状态修改为2 记录一下当前的 strCurrURL
                if (null != key) {
                    String s1 = parentIdNoECount.get(key);
                    if (null == s1) {
                        parentIdNoECount.put(key, strCurrURL + "&" + 1);
                    } else {
                        String[] split = s1.split("&");
                        int i = Integer.parseInt(split[1]);
                        i = i + 1;
                        s1 = split[0] + "&" + i;
                        parentIdNoECount.put(key, s1);
                    }
                }
            }
        } catch (BeansException e) {
            e.printStackTrace();
        }
    }






    public void delFilmResources(Integer filmId,String filmName) {
    //删除逻辑:
        //验证 是否存在该film 信息
        WxFilmInfo wxFilmInfo = wxFilmInfoMapper.selectOne(new LambdaQueryWrapper<WxFilmInfo>().eq(WxFilmInfo::getFilmId, filmId).eq(WxFilmInfo::getFilmName, filmName));
        if (null==wxFilmInfo){
            System.out.println("无此删除信息");
            return;
        }
        //获取判断是


    }
}
