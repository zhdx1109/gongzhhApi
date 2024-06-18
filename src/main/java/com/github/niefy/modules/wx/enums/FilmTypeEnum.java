package com.github.niefy.modules.wx.enums;

/**
 * @Author zhangdongxu
 * @Description create bean
 * @Date 2024/6/17 14:58
 * @Version 1.0
 */
public enum FilmTypeEnum {

    FILM_1("1","电影"),
    FILM_2("2","电视剧"),
    FILM_3("3","动漫");
    private String code;
    private String value;

    FilmTypeEnum(String code, String value) {
        this.code = code;
        this.value = value;
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


    public static String getValueByCode(String code){
        if(code == null){
            return "";
        }

        for(FilmTypeEnum typeEnum:FilmTypeEnum.values()){
            if(typeEnum.getCode().equals(code)){
                return typeEnum.getValue();
            }
        }
        return "";
    }
}
