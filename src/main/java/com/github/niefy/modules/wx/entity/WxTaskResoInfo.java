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
 * @Date 2024/6/14 14:07
 * @Version 1.0
 */

@Data
@TableName("wx_task_reso_info")
public class WxTaskResoInfo   implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String taskName;
    private String taskResoUrl;
    private Integer taskId;
    @TableField(value = "`sync_used`")
    private Boolean syncUsed;
    @TableField(value = "`status`")
    private boolean status;
    private Date updateTime;

    public WxTaskResoInfo() {
    }

    @Override
    public String toString() {
        return Json.toJsonString(this);
    }
}
