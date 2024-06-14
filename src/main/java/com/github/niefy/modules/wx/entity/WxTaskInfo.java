package com.github.niefy.modules.wx.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.niefy.common.utils.Json;
import lombok.Data;

import java.util.Date;

/**
 * @Author zhangdongxu
 * @Description create bean
 * @Date 2024/6/14 12:12
 * @Version 1.0
 */

@Data
@TableName("wx_task_info")
public class WxTaskInfo {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    private String taskId;
    private String taskName;
    private String taskUrl;
    private String isUsed;
    private String status;
    private Date updateTime;

    public WxTaskInfo() {
    }
    @Override
    public String toString() {
        return Json.toJsonString(this);
    }
}
