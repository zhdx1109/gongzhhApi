package com.github.niefy.modules.wx.service;

import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.bean.draft.*;
import me.chanjar.weixin.mp.bean.freepublish.WxMpFreePublishList;
import me.chanjar.weixin.mp.bean.material.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface WxAssetsService {
    /**
     *  获取素材总数
     * @return
     * @throws WxErrorException
     * @param appid
     */
    WxMpMaterialCountResult materialCount(String appid) throws WxErrorException;

    /**
     * 获取图文素材详情
     *
     * @param appid
     * @param mediaId
     * @return
     * @throws WxErrorException
     */
    WxMpMaterialNews materialNewsInfo(String appid, String mediaId) throws WxErrorException;
    /**
     * 根据类别分页获取非图文素材列表
     *
     * @param appid
     * @param type
     * @param page
     * @return
     * @throws WxErrorException
     */
    WxMpMaterialFileBatchGetResult materialFileBatchGet(String appid, String type, int page) throws WxErrorException;

    /**
     * 分页获取图文素材列表
     *
     * @param appid
     * @param page
     * @return
     * @throws WxErrorException
     */
    WxMpMaterialNewsBatchGetResult materialNewsBatchGet(String appid, int page) throws WxErrorException;

    /**
     * 添加图文永久素材-->已修改 实际应该是添加草稿箱
     *
     * @param appid
     * @param articles
     * @return
     * @throws WxErrorException
     */
    WxMpMaterialUploadResult addDraft(String appid, List<WxMpDraftArticles> articles)throws WxErrorException;

    /**
     * 添加图文永久素材
     *
     * @param appid
     * @param articles
     * @return
     * @throws WxErrorException
     */
    WxMpMaterialUploadResult materialNewsUpload(String appid, List<WxMpNewsArticle> articles)throws WxErrorException;


    /**
     * 更新图文素材中的某篇文章
     * @param appid
     * @param form
     */
    void materialArticleUpdate(String appid, WxMpUpdateDraft form) throws WxErrorException;

    /**
     * 添加多媒体永久素材
     *
     * @param appid
     * @param mediaType
     * @param fileName
     * @param file
     * @return
     * @throws WxErrorException
     */
    WxMpMaterialUploadResult materialFileUpload(String appid, String mediaType, String fileName, MultipartFile file) throws WxErrorException, IOException;

    /**
     * 删除素材
     *
     * @param appid
     * @param mediaId
     * @return
     * @throws WxErrorException
     */
    boolean materialDelete(String appid, String mediaId)throws WxErrorException;

    /**
     * 删除草稿
     *
     * @param appid
     * @param mediaId
     * @return
     * @throws WxErrorException
     */
    boolean draftDelete(String appid, String mediaId)throws WxErrorException;

    /**
     *
     * @param offset    分页页数，从0开始 从全部素材的该偏移位置开始返回，0表示从第一个素材返回
     * @param count     每页数量 返回素材的数量，取值在1到20之间
     * @param noContent 1 表示不返回 content 字段，0 表示正常返回，默认为 0
     * @return 草稿信息列表 wx mp draft list
     * @return
     * @throws WxErrorException
     */
    WxMpDraftList draftBatchGet(String appid,int offset, int count, int noContent)  throws WxErrorException;

    /**
     * 发布接口
     * @param appid
     * @param mediaId
     * @return
     * @throws WxErrorException
     */
    String submitDraftList(String appid,String mediaId)  throws WxErrorException;


    /**
     *
     * @param offset    分页页数，从0开始 从全部素材的该偏移位置开始返回，0表示从第一个素材返回
     * @param count     每页数量 返回素材的数量，取值在1到20之间
     * @param noContent 1 表示不返回 content 字段，0 表示正常返回，默认为 0
     * @return
     * @throws WxErrorException
     */
    WxMpFreePublishList getPublicationRecords( String appid,int offset, int count, int noContent) throws WxErrorException;

    /**
     *
     Params:
     articleId – 成功发布时返回的 article_id
     Returns:
     the boolean
     * @throws WxErrorException
     */
    Boolean deletePushAllArticle(String appid,String articleId) throws WxErrorException;
}
