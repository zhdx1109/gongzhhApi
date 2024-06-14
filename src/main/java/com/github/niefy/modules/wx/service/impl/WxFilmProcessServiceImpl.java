package com.github.niefy.modules.wx.service.impl;

import com.github.niefy.common.utils.Json;
import com.github.niefy.modules.wx.entity.WxFilmInfo;
import com.github.niefy.modules.wx.entity.WxFilmSubInfo;
import com.github.niefy.modules.wx.service.WxFilmService;
import com.github.niefy.modules.wx.service.WxFilmSubService;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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


    public void filmResourceProcess() {
        //当前方法只生成资源的秘钥和url,汇总的形式需要单独考虑
        List<WxFilmInfo> wxFilmInfoList = wxFilmService.queryFilmList();
        //处理孤品影视
        List<WxFilmInfo> isSingleWxFilmInfoList =  wxFilmInfoList.stream().filter(x -> "1".equalsIgnoreCase(x.getIsSingle())).collect(Collectors.toList());

        isSingleWxFilmInfoList.stream().forEach(x ->{
            String filmNameDec = x.getFilmNameDec();

            String strCurrMY = filmNameDec + "秘钥";//影秘钥
            String strCurrURL = filmNameDec + "url";//影url
            String keywordDec = filmNameDec; //匹配的关键字
            String strCurrA = "<a href=\"weixin://bizmsgmenu?msgmenucontent=" + filmNameDec + "&msgmenuid=1\">" + filmNameDec + "</a>";
            System.out.println("独立作品:"+strCurrMY+",关键字:"+keywordDec+","+strCurrURL+","+strCurrA);
        });


        //取出需要的Map<Integer,WxFilmInfo> //此处的作用是将作品的id和bean进行对等。过滤掉孤品影视
        ConcurrentMap<Integer, WxFilmInfo> keyWxFilmInfoMap = wxFilmInfoList.stream().filter(x -> !"1".equalsIgnoreCase(x.getIsSingle())).collect(Collectors.toConcurrentMap(WxFilmInfo::getFilmId, Function.identity()));
        //将其取出需要的ids
//        List<Integer> collect = wxFilmInfoList.stream().map(WxFilmInfo::getFilmId).collect(Collectors.toList());

        List<Integer> collect = Arrays.asList(keyWxFilmInfoMap.keySet().toArray(new Integer[0])) ;

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
                        String strCurr = finalFilmName + strRes; //斯巴达1-1
                        String strCurrMY = strCurr + "秘钥";//斯巴达1-1秘钥
                        String strCurrURL = strCurr + "url";//斯巴达1-1url
                        if (startValue.equalsIgnoreCase(strRes)) {
                            int m = j; //重新赋值 防止j混乱
                            Integer nextInt = numbersList.get(m + 1);
                            String strNext = finalFilmName + finalFilmNameDec + "-" + String.valueOf(nextInt);
                            String strNextA = "<a href=\"weixin://bizmsgmenu?msgmenucontent=" + strNext + "&msgmenuid=1\">" + strNext + "</a>";
                            System.out.println("当前:" + strCurr);
                            System.out.println("下一个:" + strNextA);

                        } else if (!followUpValue.equalsIgnoreCase(strRes)) {
                            int m = j;
                            String strNext = "";
                            String strPre = "";
                            if (m == 0) {
                                WxFilmSubInfo wxFilmSubInfo1 = wxFilmSubInfos1.get(i - 1);
                                String seriesValue1 = wxFilmSubInfo1.getSeriesValue();
                                String filmNameDec1 = wxFilmSubInfo1.getFilmNameDec();
                                String[] split = seriesValue1.split("-");
                                strPre = finalFilmName + filmNameDec1 +"-"+ split[1];
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
                            System.out.println("上一个:" + strPreA);
                            System.out.println("当前:" + strCurr);
                            System.out.println("下一个:" + strNextA);
                        } else {
                            int m = j;
                            String strPre = finalFilmName + finalFilmNameDec + "-" + String.valueOf(numbersList.get(m - 1));
                            String strPreA = "<a href=\"weixin://bizmsgmenu?msgmenucontent=" + strPre + "&msgmenuid=1\">" + strPre + "</a>";
                            System.out.println("上一个:" + strPreA);
                            System.out.println("当前:" + strCurr);
                        }
                    }

                } else {
                    String strCurr = filmNameDec;
                    if (startValue.equalsIgnoreCase(seriesValue)) {
                        WxFilmSubInfo wxFilmSubNextInfo = wxFilmSubInfos1.get(i + 1);
                        String strNext = wxFilmSubNextInfo.getFilmNameDec();
                        String strNextA = "<a href=\"weixin://bizmsgmenu?msgmenucontent=" + strNext + "&msgmenuid=1\">" + strNext + "</a>";
                        System.out.println("当前:" + strCurr);
                        System.out.println("下一个:" + strNextA);
                    } else if (!followUpValue.equalsIgnoreCase(seriesValue)) {
                        String strPreA = wxFilmSubInfos1.get(i - 1).getFilmNameDec();
                        String strNextA = wxFilmSubInfos1.get(i + 1).getFilmNameDec();
                        System.out.println("上一个:" + strPreA);
                        System.out.println("当前:" + strCurr);
                        System.out.println("下一个:" + strNextA);
                    } else {
                        String strPreA = wxFilmSubInfos1.get(i - 1).getFilmNameDec();
                        System.out.println("上一个:" + strPreA);
                        System.out.println("当前:" + strCurr);
                    }
                }
            }
        }
        ;

    }

}
