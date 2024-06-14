package com.github.niefy.modules.wx.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
@TableName("wx_films_sub_info")
public class WxFilmSubInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    private Long filmSubId;
    private String filmName;
    private String filmNameDec;
    private String seriesValue;
    private Integer priority;
    private Integer parentId;
    private Date updateTime;
    public WxFilmSubInfo() {
    }

    @Override
    public String toString() {
        return Json.toJsonString(this);
    }
}
