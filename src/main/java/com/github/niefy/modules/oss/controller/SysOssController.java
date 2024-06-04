package com.github.niefy.modules.oss.controller;

import com.alibaba.fastjson.JSON;
import com.github.niefy.common.exception.RRException;
import com.github.niefy.common.utils.*;
import com.github.niefy.common.validator.ValidatorUtils;
import com.github.niefy.common.validator.group.AliyunGroup;
import com.github.niefy.common.validator.group.QcloudGroup;
import com.github.niefy.common.validator.group.QiniuGroup;
import com.github.niefy.modules.oss.cloud.CloudStorageConfig;
import com.github.niefy.modules.oss.cloud.OSSFactory;
import com.github.niefy.modules.oss.entity.SysOssEntity;
import com.github.niefy.modules.oss.service.SysOssService;
import com.github.niefy.modules.sys.service.SysConfigService;
import io.netty.util.internal.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.material.WxMediaImgUploadResult;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * 文件上传
 * @author Mark sunlightcs@gmail.com
 */
@RestController
@RequestMapping("sys/oss")
@Api(tags = {"对象存储/文件上传"})
public class SysOssController {
    @Autowired
    private SysOssService sysOssService;
    @Autowired
    private SysConfigService sysConfigService;
    @Autowired
    WxMpService wxMpService;

    private final static String KEY = ConfigConstant.CLOUD_STORAGE_CONFIG_KEY;

    /**
     * 列表
     */
    @ApiOperation(value = "文件列表",notes = "对象存储管理的文件")
    @GetMapping("/list")
    @RequiresPermissions("sys:oss:all")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = sysOssService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 云存储配置信息
     */
    @GetMapping("/config")
    @RequiresPermissions("sys:oss:all")
    @ApiOperation(value = "云存储配置信息",notes = "首次使用前先管理后台新增配置")
    public R config() {
        CloudStorageConfig config = sysConfigService.getConfigObject(KEY, CloudStorageConfig.class);

        return R.ok().put("config", config);
    }


    /**
     * 保存云存储配置信息
     */
    @PostMapping("/saveConfig")
    @RequiresPermissions("sys:oss:all")
    @ApiOperation(value = "保存云存储配置信息")
    public R saveConfig(@RequestBody CloudStorageConfig config) {
        //校验类型
        ValidatorUtils.validateEntity(config);

        if (config.getType() == Constant.CloudService.QINIU.getValue()) {
            //校验七牛数据
            ValidatorUtils.validateEntity(config, QiniuGroup.class);
        } else if (config.getType() == Constant.CloudService.ALIYUN.getValue()) {
            //校验阿里云数据
            ValidatorUtils.validateEntity(config, AliyunGroup.class);
        } else if (config.getType() == Constant.CloudService.QCLOUD.getValue()) {
            //校验腾讯云数据
            ValidatorUtils.validateEntity(config, QcloudGroup.class);
        }

        sysConfigService.updateValueByKey(KEY, JSON.toJSONString(config));

        return R.ok();
    }


    /**
     * 上传文件:涉及图片url必须来源 "上传图文消息内的图片获取URL"接口获取。外部图片url将被过滤。
     * wxMpService.getMaterialService().mediaImgUpload()
     * MultipartFile file
     */
    @PostMapping("/upload")
    @RequiresPermissions("sys:oss:all")
    @ApiOperation(value = "上传文件到OSS")
    public R upload(@CookieValue String appid,@RequestParam("file") MultipartFile file) throws Exception {
        if (file==null) {
            throw new RRException("上传文件不能为空");
        }

        File convert = MultipartFileToFileConverterUtils.convert(file);
        String url="upload error";
        wxMpService.switchoverTo(appid);
        WxMediaImgUploadResult wxMediaImgUploadResult=new WxMediaImgUploadResult();
//        WxMediaImgUploadResult wxMediaImgUploadResult = wxMpService.getMaterialService().mediaImgUpload(convert);

        //上传文件
//        String suffix = Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf("."));
//        String url = Objects.requireNonNull(OSSFactory.build()).uploadSuffix(file.getBytes(), suffix);
        wxMediaImgUploadResult.setUrl("http://mmbiz.qpic.cn/mmbiz_png/FQdfXv9omibCTPmqzbPn5uWcXkwQuZtpbI60dWLIydAbuMiaae1rlBl9gkXfscYYta3qlKTHSibZtIJsLYibdC6Y0Q/0?from=appmsg");
        if (null!=wxMediaImgUploadResult&& !StringUtil.isNullOrEmpty(wxMediaImgUploadResult.getUrl())){
            //保存文件信息
            url= wxMediaImgUploadResult.getUrl();
            SysOssEntity ossEntity = new SysOssEntity();
            ossEntity.setFileName(file.getOriginalFilename());
            ossEntity.setUrl(url);
            ossEntity.setCreateDate(new Date());
            sysOssService.save(ossEntity);
        }

        return R.ok().put("url", url);
    }


    /**
     * 删除
     */
    @PostMapping("/delete")
    @RequiresPermissions("sys:oss:all")
    @ApiOperation(value = "删除文件",notes = "只删除记录，云端文件不会删除")
    public R delete(@RequestBody Long[] ids) {
        sysOssService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
