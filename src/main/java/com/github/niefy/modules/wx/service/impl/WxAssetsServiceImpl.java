package com.github.niefy.modules.wx.service.impl;

import com.github.niefy.modules.wx.dto.PageSizeConstant;
import com.github.niefy.modules.wx.service.WxAssetsService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpMaterialService;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpMaterialServiceImpl;
import me.chanjar.weixin.mp.bean.draft.*;
import me.chanjar.weixin.mp.bean.freepublish.WxMpFreePublishList;
import me.chanjar.weixin.mp.bean.material.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
@CacheConfig(cacheNames = {"wxAssetsServiceCache"})
@Slf4j
public class WxAssetsServiceImpl implements WxAssetsService {
    @Autowired
    WxMpService wxMpService;

    @Override
    @Cacheable(key="methodName+ #appid")
    public WxMpMaterialCountResult materialCount(String appid) throws WxErrorException {
        log.info("从API获取素材总量");
        wxMpService.switchoverTo(appid);
        WxMpMaterialCountResult wxMpMaterialCountResult = wxMpService.getMaterialService().materialCount();
        Long aLong = wxMpService.getDraftService().countDraft();
        wxMpMaterialCountResult.setNewsCount(Integer.parseInt(String.valueOf(aLong)));
        return wxMpMaterialCountResult;
    }

    @Override
    @Cacheable(key="methodName + #appid + #mediaId")
    public WxMpMaterialNews materialNewsInfo(String appid, String mediaId) throws WxErrorException {
        log.info("从API获取图文素材详情,mediaId={}",mediaId);
        wxMpService.switchoverTo(appid);
        return wxMpService.getMaterialService().materialNewsInfo(mediaId);
    }

    @Override
    @Cacheable(key="methodName + #appid + #type + #page")
    public WxMpMaterialFileBatchGetResult materialFileBatchGet(String appid, String type, int page) throws WxErrorException {
        log.info("从API获取媒体素材列表,type={},page={}",type,page);
        wxMpService.switchoverTo(appid);
        final int pageSize = PageSizeConstant.PAGE_SIZE_SMALL;
        int offset=(page-1)* pageSize;
        return wxMpService.getMaterialService().materialFileBatchGet(type,offset, pageSize);
    }

    @Cacheable(key="methodName + #appid + #page")
    @Override
    public WxMpMaterialNewsBatchGetResult materialNewsBatchGet(String appid, int page) throws WxErrorException {
        log.info("从API获取媒体素材列表,page={}",page);
        wxMpService.switchoverTo(appid);
        final int pageSize = PageSizeConstant.PAGE_SIZE_SMALL;
        int offset=(page-1)*pageSize;
        return wxMpService.getMaterialService().materialNewsBatchGet(offset, pageSize);
    }

    @Override
    public WxMpMaterialUploadResult addDraft(String appid, List<WxMpDraftArticles> articles) throws WxErrorException {
        Assert.notEmpty(articles,"图文列表不得为空");
        log.info("上传图文素材...");
        //  private Integer needOpenComment;
        //  private Integer onlyFansCanComment;
        //   private Integer showCoverPic;
        wxMpService.switchoverTo(appid);
        WxMpAddDraft news = new WxMpAddDraft();
        news.setArticles(articles);
        String draftMediaId = wxMpService.getDraftService().addDraft(news);
        WxMpMaterialUploadResult result = new WxMpMaterialUploadResult();
        result.setMediaId(draftMediaId);
        result.setErrCode(0);
        return result;
    }

    @Override
    @CacheEvict(allEntries = true)
    public WxMpMaterialUploadResult materialNewsUpload(String appid, List<WxMpNewsArticle> articles) throws WxErrorException {
        Assert.notEmpty(articles, "图文列表不得为空");
        log.info("上传图文素材...");
        wxMpService.switchoverTo(appid);
        WxMpMaterialUploadResult result = null;
        try {
            WxMpMaterialNews wxMpMaterialNews = new WxMpMaterialNews();
            wxMpMaterialNews.setArticles(articles);
            WxMpMaterialUploadResult wxMpMaterialUploadResult = wxMpService.getMaterialService().materialNewsUpload(wxMpMaterialNews);
            result = new WxMpMaterialUploadResult();
            result.setMediaId(wxMpMaterialUploadResult.getMediaId());
        } catch (WxErrorException e) {
            e.printStackTrace();
            System.out.println("materialNewsUpload 报错:{}"+e);
        }
        result.setErrCode(0);
        return result;
    }

    /**
     * 更新图文素材中的某篇文章
     * @param appid
     * @param form
     */
    @Override
    @CacheEvict(allEntries = true)
    public void materialArticleUpdate(String appid, WxMpUpdateDraft form)  throws WxErrorException{
        log.info("更新图文素材...");
        wxMpService.switchoverTo(appid);
        wxMpService.getDraftService().updateDraft(form);
    }
    @Override
    @CacheEvict(allEntries = true)
    public WxMpMaterialUploadResult materialFileUpload(String appid, String mediaType, String fileName, MultipartFile file) throws WxErrorException, IOException {
        log.info("上传媒体素材：{}",fileName);
        wxMpService.switchoverTo(appid);
        String originalFilename=file.getOriginalFilename();
        File tempFile = File.createTempFile(fileName+"--", Objects.requireNonNull(originalFilename).substring(originalFilename.lastIndexOf(".")));
        file.transferTo(tempFile);
        WxMpMaterial wxMaterial = new WxMpMaterial();
        wxMaterial.setFile(tempFile);
        wxMaterial.setName(fileName);
        if(WxConsts.MediaFileType.VIDEO.equals(mediaType)){
            wxMaterial.setVideoTitle(fileName);
        }
        WxMpMaterialUploadResult res = wxMpService.getMaterialService().materialFileUpload(mediaType,wxMaterial);
        tempFile.deleteOnExit();
        return res;
    }

    @Override
    @CacheEvict(allEntries = true)
    public boolean materialDelete(String appid, String mediaId) throws WxErrorException {
        log.info("删除素材，mediaId={}",mediaId);
        wxMpService.switchoverTo(appid);
        return wxMpService.getMaterialService().materialDelete(mediaId);
    }

    @Override
    @CacheEvict(allEntries = true)
    public boolean draftDelete(String appid, String mediaId) throws WxErrorException {
        log.info("删除素材，mediaId={}",mediaId);
        wxMpService.switchoverTo(appid);
        Boolean aBoolean = wxMpService.getDraftService().delDraft(mediaId);
        return aBoolean;
    }

    @Override
    public WxMpDraftList draftBatchGet(String appid,int offset, int count, int noContent) throws WxErrorException {
        log.info("获取草稿列表,appid:{}",appid);
        wxMpService.switchoverTo(appid);
        WxMpDraftList wxMpDraftList = wxMpService.getDraftService().listDraft(offset, count, noContent);
        return wxMpDraftList;
    }

    @Override
    public String submitDraftList(String appid, String mediaId) throws WxErrorException {
        log.info("草稿发布接口,appid:{}",appid);
        wxMpService.switchoverTo(appid);
        String submit = wxMpService.getFreePublishService().submit(mediaId);
        return submit;
    }


    @Override
    public WxMpFreePublishList getPublicationRecords(String appid,int offset, int count, int noContent) throws WxErrorException {
        log.info("草稿发布接口,appid:{}",appid);
        wxMpService.switchoverTo(appid);
        WxMpFreePublishList publicationRecords = wxMpService.getFreePublishService().getPublicationRecords(offset, count, noContent);
        return publicationRecords;
    }

    @Override
    public Boolean deletePushAllArticle(String appid,String articleId) throws WxErrorException {
        log.info("草稿发布接口,appid:{}",appid);
        wxMpService.switchoverTo(appid);
        Boolean aBoolean = wxMpService.getFreePublishService().deletePushAllArticle(articleId);
        return aBoolean;
    }
}
