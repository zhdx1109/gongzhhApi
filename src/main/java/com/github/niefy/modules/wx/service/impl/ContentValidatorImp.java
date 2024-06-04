package com.github.niefy.modules.wx.service.impl;

import com.github.niefy.modules.wx.enums.PlatformEnum;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author zhangdongxu
 * @Description create bean
 * @Date 2024/5/29 11:30
 * @Version 1.0
 */
@Service
public class ContentValidatorImp {

    public String validateAndReplace(String xml) {
        String content = extractContent(xml);

        //如果传过来的内容非rul,直接放行
        if (content == null || !content.startsWith("http://") && !content.startsWith("https://")) {
            return xml;
        }

//        List<String> allowedDomains = Arrays.asList("qidian.com", "toutiao.com");
        List<String> allowedDomains = PlatformEnum.getPlatFormValues();
        //需要将匹配到的域名进行枚举对应出关键字
        boolean domainAllowed = false; //用于跳出循环
        PlatformEnum platformEnum = null;
        for (String strValue : allowedDomains) {
            Pattern pattern = Pattern.compile(strValue);
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                platformEnum = PlatformEnum.getPlatformEnum(strValue);
                domainAllowed=true;
            }
            if (domainAllowed){
                break;
            }
        }
        if (null == platformEnum) {
            //非此公众号指定小说平台
            String replaceStr = "非此公众号指定小说平台";

            return bookIdReplace(replaceStr, xml);
        }

        String bookId = extractBookId(content, platformEnum);

        if (bookId == null || bookId.isEmpty()) {
            return bookIdReplace("指定平台但是无bookId可取", xml);
        }
        return bookIdReplace(bookId, xml);
    }

    //用于替换
    private static String bookIdReplace(String bookId, String xml) {
        String updatedContent = "<Content><![CDATA[bookId=" + bookId + "]]></Content>";
        String updatedXml = xml.replaceFirst("<Content><!\\[CDATA\\[.*?\\]\\]></Content>", updatedContent);
        return updatedXml;
    }

    //抽取出<Content>
    private static String extractContent(String xml) {
        Pattern pattern = Pattern.compile("<Content><!\\[CDATA\\[(.*?)\\]\\]></Content>");
        Matcher matcher = pattern.matcher(xml);
        return matcher.find() ? matcher.group(1) : null;
    }

    //用去从小说rul中获取指定bookId
    private static String extractBookId(String content, PlatformEnum platformEnum) {
        if (!(null == platformEnum.getKeyCode())) {
            String keyCode = platformEnum.getKeyCode();
            Pattern pattern = Pattern.compile(keyCode + "=(\\d+)");
//            Pattern pattern = Pattern.compile("bookId=(\\d+)");
            Matcher matcher = pattern.matcher(content);
            return matcher.find() ? matcher.group(1) : null;
        } else {
            if (PlatformEnum.P_FALOO.getCode().equals(platformEnum.getCode())) {
                //https://wap.faloo.com/604210.html
                return checkAndExtractFLNov(content);

            }
            if (PlatformEnum.p_wtzw.getCode().equals(platformEnum.getCode())) {
                //https://app-share.wtzw.com/app-h5/freebook/article-detail/1805575
                return checkAndExtractQMNov(content);
            }
            return "平台尚未对接此小说平台,请您检查小说链接!";
        }
    }

    //单独处理七猫小说平台的url
    private static String checkAndExtractQMNov(String url) {
        // 判断最后一个 '/' 之前是否包含 "wtzw.com"
        int lastSlashIndex = url.lastIndexOf('/');
        if (lastSlashIndex == -1 || !url.substring(0, lastSlashIndex).contains("wtzw.com")) {
            return "七猫小说链接错误,请您查证!";
        }

        // 判断最后一个 '/' 后面是否为纯数字
        String data = url.substring(lastSlashIndex + 1);
        if (!data.matches("\\d+")) {
            return "七猫小说链接错误,请您查证!";
        }

        // 返回截取的数据
        return data;
    }


    //抽取飞卢小说bookId标识。并判断是否为飞卢平台
    private static String checkAndExtractFLNov(String url) {
        // 判断最后一个 '/' 之前是否包含 "faloo.com"
        int lastSlashIndex = url.lastIndexOf('/');
        if (lastSlashIndex == -1 || !url.substring(0, lastSlashIndex).contains("faloo.com")) {
            return "飞卢小说链接错误,请您查证!";
        }

        // 判断最后一个 '.' 后面是否为 "html"
        int lastDotIndex = url.lastIndexOf('.');
        if (lastDotIndex == -1 || !url.substring(lastDotIndex + 1).equals("html")) {
            return "飞卢小说链接错误,请您查证!";
        }

        // 截取最后一个 '/' 到最后一个 '.' 之间的数据
        String data = url.substring(lastSlashIndex + 1, lastDotIndex);
        return data;
    }
}
