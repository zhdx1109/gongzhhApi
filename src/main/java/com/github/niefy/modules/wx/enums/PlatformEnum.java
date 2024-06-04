package com.github.niefy.modules.wx.enums;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author zhangdongxu
 * @Description create bean
 * @Date 2024/5/29 10:56
 * @Version 1.0 功能:为了匹配各小说平台对应的小说Id
 */
public enum PlatformEnum {
    // 起点
    P_QIDIAN("1","qidian.com","bookId"),
    //头条 悟空浏览器
    P_TOUTIAO("2","toutiao.com","book_id"),
    //番茄小说
    P_CHANGDUNOVEL("4","changdunovel.com","book_id"),
    //QQ阅读
    P_QQREADER("5","reader.qq.com","bid"),
    //七猫小说
    p_wtzw("6","wtzw.com",null),
    //书旗小说
    P_SHUQIREADER("7","shuqireader.com","bookId"),
    //飞卢小说
    P_FALOO("8","faloo.com",null),
    //掌阅app
    P_ZHANGYUE("9","zhangyue.com","p2");
    
    private String code;
    private String value;
    private String keyCode;


    PlatformEnum(String code, String value, String keyCode) {
        this.code = code;
        this.value = value;
        this.keyCode = keyCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(String keyCode) {
        this.keyCode = keyCode;
    }

    //
    public static String getValueByCode(String code){
        if(code == null){
            return "";
        }

        for(PlatformEnum typeEnum:PlatformEnum.values()){
            if(typeEnum.getCode().equals(code)){
                return typeEnum.getValue();
            }
        }
        return "";
    }


    /**
     * 通过code获取实例
     * @param code
     * @return
     */
    public static PlatformEnum getTypeEnumBytypeId(String code){
        for(PlatformEnum topType:values()){
            if(topType.getCode().equals(code)){
                return topType;
            }
        }
        return null;
    }

    //将value转成list
    public static List<String> getPlatFormValues() {
        List<String> platFormValues = new ArrayList<>();
        for(PlatformEnum topType:values()){
            platFormValues.add(topType.getValue());
        }
        return platFormValues;
    }

    //通过value匹配对应的enum
    public static PlatformEnum getPlatformEnum(String value){
        for (PlatformEnum topType:values()) {
            if (value.equalsIgnoreCase(topType.getValue())){
                return topType;
            }
        }
        return null;
    }



}
