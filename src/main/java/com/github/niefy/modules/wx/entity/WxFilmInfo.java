package com.github.niefy.modules.wx.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.niefy.common.utils.Json;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author zhangdongxu
 * @Description create bean
 * @Date 2024/6/12 16:41
 * @Version 1.0
 */

@Data
@TableName("wx_films_info")
public class WxFilmInfo  implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    private Integer filmId;
    private String filmName;
    private Integer filmType;
    private String filmNameDec;
    private String isSingle;
    private String isFollowUp;
    private String followUpValue;
    private String startValue;
    private String syncStatus;
    private String syncValue;
    private int filmCount;
    private Date updateTime;
    public WxFilmInfo() {
    }

    @Override
    public String toString() {
        return Json.toJsonString(this);
    }
}
