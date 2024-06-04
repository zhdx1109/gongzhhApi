package com.github.niefy.modules.wx.manage;

import com.github.niefy.common.utils.R;
import com.github.niefy.modules.wx.form.MaterialFileDeleteForm;
import com.github.niefy.modules.wx.service.WxAssetsService;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSyntaxException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.draft.*;
import me.chanjar.weixin.mp.bean.freepublish.WxMpFreePublishList;
import me.chanjar.weixin.mp.bean.material.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 微信公众号素材管理
 * 参考官方文档：https://developers.weixin.qq.com/doc/offiaccount/Asset_Management/New_temporary_materials.html
 * 参考WxJava开发文档：https://github.com/Wechat-Group/WxJava/wiki/MP_永久素材管理
 */
@Slf4j
@RestController
@RequestMapping("/manage/wxAssets")
@Api(tags = {"公众号素材-管理后台"})
public class WxAssetsManageController {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    WxAssetsService wxAssetsService;
    @Autowired
    WxMpService wxMpService;

    /**
     * 获取素材总数
     *
     * @return
     * @throws WxErrorException
     */
    @GetMapping("/materialCount")
    @ApiOperation(value = "文件素材总数")
    public R materialCount(@CookieValue String appid) throws WxErrorException {
//        WxMpMaterialCountResult res = wxAssetsService.materialCount(appid);

        log.info("从API获取素材总量");
        wxMpService.switchoverTo(appid);
        //test 获取草稿素材总数
//        WxMpMaterialCountResult wxMpMaterialCountResult= new WxMpMaterialCountResult();
//        wxMpMaterialCountResult.setNewsCount(2);
//        wxMpMaterialCountResult.setImageCount(1);
//        wxMpMaterialCountResult.setVideoCount(1);
//        wxMpMaterialCountResult.setVoiceCount(1);
        //todo
        WxMpMaterialCountResult wxMpMaterialCountResult = wxMpService.getMaterialService().materialCount();
        Long aLong = wxMpService.getDraftService().countDraft();
        wxMpMaterialCountResult.setNewsCount(Integer.parseInt(String.valueOf(aLong)));

        return R.ok().put(wxMpMaterialCountResult);
    }

    /**
     * 获取素材总数--此方法已经作废: reason 微信官方已经不再支持图文素材，图文素材全部移至草稿箱
     *
     * @return
     * @throws WxErrorException
     */
//    @GetMapping("/materialNewsInfo")
//    @ApiOperation(value = "图文素材总数")
//    public R materialNewsInfo(@CookieValue String appid, String mediaId) throws WxErrorException {
//        WxMpMaterialNews res = wxAssetsService.materialNewsInfo(appid, mediaId);
//        return R.ok().put(res);
//    }


    /**
     * 根据类别分页获取非图文素材列表
     *
     * @param type
     * @param page
     * @return
     * @throws WxErrorException
     */
    @GetMapping("/materialFileBatchGet")
    @RequiresPermissions("wx:wxassets:list")
    @ApiOperation(value = "根据类别分页获取非图文素材列表")
    public R materialFileBatchGet(@CookieValue String appid, @RequestParam(defaultValue = "image") String type,
                                  @RequestParam(defaultValue = "1") int page) throws WxErrorException {
        WxMpMaterialFileBatchGetResult res = wxAssetsService.materialFileBatchGet(appid, type, page);
        //获取图片 音频 视频的信息
//        WxMpMaterialFileBatchGetResult res = getTest(type);
        return R.ok().put(res);
    }

    private WxMpMaterialFileBatchGetResult getTest(String type) {
        String jsonString = "";
        if ("image".equalsIgnoreCase(type)) {
            jsonString = "{\"itemCount\":1,\"items\":[{\"mediaId\":\"10amUGA13zbsCtl2JFj7eah_GBkgYrEqfvLouafxGTrEaiEvi9rH23_39cIkYPQ4\",\"name\":\"简报--3445050206905389841.jpg\",\"updateTime\":1716997481000,\"url\":\"https://mmbiz.qpic.cn/mmbiz_jpg/FQdfXv9omibAyeZ9yBywx1FfBFic2YAGIptlG03BLJ0WybMrxkzXHJg7l31H2aDibZn50TicP3xdv36iavd0bL80WWQ/0?wx_fmt=jpeg\"}],\"totalCount\":1}";
        } else {
            jsonString = "{\"itemCount\":0,\"items\":[],\"totalCount\":0}";
        }
        WxMpMaterialFileBatchGetResult result = null;
        try {
            // 创建自定义Gson解析器来处理时间戳
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (json, typeOfT, context) -> {
                        return new Date(json.getAsJsonPrimitive().getAsLong());
                    })
                    .setDateFormat(DateFormat.LONG)
                    .create();
            result = gson.fromJson(jsonString, WxMpMaterialFileBatchGetResult.class);
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }


    /**
     * 分页获取图文素材列表--已经废除，微信官方已经将图文素材转移至草稿箱
     *
     * @param page
     * @return
     * @throws WxErrorException
     */
//    @GetMapping("/materialNewsBatchGet")
//    @RequiresPermissions("wx:wxassets:list")
//    @ApiOperation(value = "分页获取图文素材列表")
//    public R materialNewsBatchGet(@CookieValue String appid, @RequestParam(defaultValue = "1") int page) throws WxErrorException {
//        WxMpMaterialNewsBatchGetResult res = wxAssetsService.materialNewsBatchGet(appid, page);
//        return R.ok().put(res);
//    }


    /**
     * 此方法为获取草稿箱列表-
     *
     * @param appid
     * @param offset
     * @param count
     * @param noContent
     * @return
     * @throws WxErrorException
     */
    @GetMapping("/draftBatchget")
    @RequiresPermissions("wx:wxassets:list")
    @ApiOperation(value = "分页获取图文素材列表")
    public R draftBatchGet(@CookieValue String appid, @RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "20") int count, @RequestParam(defaultValue = "0") int noContent) throws WxErrorException {
        WxMpDraftList wxMpDraftList = wxAssetsService.draftBatchGet(appid, offset, count, noContent);
        //test 获取草稿列表
//        WxMpDraftList wxMpDraftList =  testBatchGet();
        return R.ok().put(wxMpDraftList);
    }

    public static WxMpDraftList testBatchGet() {

        String json = "{\"item\":[{\"content\":{\"news_item\":[{\"author\":\"12\",\"content\":\"<p>123</p>\",\"content_source_url\":\"\",\"digest\":\"1234\",\"need_open_comment\":0,\"only_fans_can_comment\":0,\"show_cover_pic\":0,\"thumb_media_id\":\"10amUGA13zbsCtl2JFj7eah_GBkgYrEqfvLouafxGTrEaiEvi9rH23_39cIkYPQ4\",\"thumb_url\":\"http://mmbiz.qpic.cn/mmbiz_jpg/FQdfXv9omibDoPYEjzJtkvB9RWBLKUfb5a9iaAK6ia4axib6ES2mKe00TzTMK1j3yRFuBU6rjskb2fRvrP7nNkYIhQ/0?wx_fmt=jpeg\",\"title\":\"12345\",\"url\":\"http://mp.weixin.qq.com/s?__biz=MzkzNzcwODU3NA==&tempkey=MTI3Ml9LazUxSkRxMzZUU2VqRzNVNnYwaGNEYm1XcGlpNnBjaGNIWUZ5cXQ0dktKVzVlVW1zUXB6QUI2dEVhX2s1dVhVMlRvc1Fla2tRNGpFQ3NnbzFvb1pQNmQtM1duTVM1ajJJZk9oem1lRDZlekNtNVQ5QTNSam8xcW54NVZnT2o3SW9KRlZSYUE0NGlNM2p3ZmJQNDJDczBDZG1qMkZ0dGpnU2x6VmRBfn4%3D&chksm=428a1c6075fd9576e184594b3a4c62347348a090199aea9e8c9639ec1bb3b057e07c93a75466#rd\"}]},\"media_id\":\"10amUGA13zbsCtl2JFj7eQgLfoepYu_-Wr2lhPYeHJWuNbJTT5I2F-8VP1vbSYcv\",\"update_time\":1717040136},{\"content\":{\"news_item\":[{\"author\":\"12\",\"content\":\"<p>123</p>\",\"content_source_url\":\"\",\"digest\":\"1234\",\"need_open_comment\":0,\"only_fans_can_comment\":0,\"show_cover_pic\":0,\"thumb_media_id\":\"10amUGA13zbsCtl2JFj7eah_GBkgYrEqfvLouafxGTrEaiEvi9rH23_39cIkYPQ4\",\"thumb_url\":\"http://mmbiz.qpic.cn/mmbiz_jpg/FQdfXv9omibAyeZ9yBywx1FfBFic2YAGIptlG03BLJ0WybMrxkzXHJg7l31H2aDibZn50TicP3xdv36iavd0bL80WWQ/0?wx_fmt=jpeg\",\"title\":\"12345\",\"url\":\"http://mp.weixin.qq.com/s?__biz=MzkzNzcwODU3NA==&tempkey=MTI3Ml96RllGbWR2Y3Z6b2EyKzB0NnYwaGNEYm1XcGlpNnBjaGNIWUZ5cXQ0dktKVzVlVW1zUXB6QUI2dEVhLTVoa3gxcTBIWGY1Zk1oM2J6UE1zMFAwVWoxcF9TRTFvOFNUM2gweGJiYUEyQU9KajNqTnN1ZFQtN3JEUGVCUDJ1S3ZKUW9NVlpwcGxXOFRIalBad0F6OGp5RUpLTnM0QjVkd3dORVNFV1N3fn4%3D&chksm=428a1c7f75fd95699823bbaa506c4d4b88a5ecf7e2abc5e2f4dadbdd1ccc8908ae72522091be#rd\"}]},\"media_id\":\"10amUGA13zbsCtl2JFj7eVR8ysUg6WwCAR9TuR4ljg9jst05iaLl8mSqoxbe5AOz\",\"update_time\":1717040132},{\"content\":{\"news_item\":[{\"author\":\"\",\"content\":\"<p>测试1</p>\",\"content_source_url\":\"\",\"digest\":\"测试1\",\"need_open_comment\":0,\"only_fans_can_comment\":0,\"show_cover_pic\":0,\"thumb_media_id\":\"10amUGA13zbsCtl2JFj7eah_GBkgYrEqfvLouafxGTrEaiEvi9rH23_39cIkYPQ4\",\"thumb_url\":\"http://mmbiz.qpic.cn/mmbiz_jpg/FQdfXv9omibAyeZ9yBywx1FfBFic2YAGIpslEIBX31ap9AvdyficotaJkUhSicIANKHZUmRsDNrgSn5gsfXDnWibhZg/0?wx_fmt=jpeg\",\"title\":\"测试1\",\"url\":\"http://mp.weixin.qq.com/s?__biz=MzkzNzcwODU3NA==&tempkey=MTI3Ml9OUzdPSEdoNUJQL1ZQQmNzNnYwaGNEYm1XcGlpNnBjaGNIWUZ5cXQ0dktKVzVlVW1zUXB6QUI2dEVhX1RYTVNOOHg1NEZwc3ZMWU9FdmdpU2tvM29SS29QRURBbk5zclFES1NaQ3NUMFVFRjkwYXJIQmw1dGpoWi1yLUxlRVRpSnBKVWd6Wll5eWpXRnlVbnpxeF8xTzFGWWdSSEZ0b09HS3RNVnlBfn4%3D&chksm=428a1c7a75fd956c79d730ff4d7b26be3cec025cdde2437e8b28ade6c0b0aaf08b73f90e3e27#rd\"}]},\"media_id\":\"10amUGA13zbsCtl2JFj7eb4kzMCm8O2kQ_hFe6w8OQyNdJ0iCzXAYjFicyEqkTzS\",\"update_time\":1717006638},{\"content\":{\"news_item\":[{\"author\":\"\",\"content\":\"<p>测试111</p>\",\"content_source_url\":\"\",\"digest\":\"测试111\",\"need_open_comment\":0,\"only_fans_can_comment\":0,\"show_cover_pic\":0,\"thumb_media_id\":\"10amUGA13zbsCtl2JFj7eah_GBkgYrEqfvLouafxGTrEaiEvi9rH23_39cIkYPQ4\",\"thumb_url\":\"http://mmbiz.qpic.cn/mmbiz_jpg/FQdfXv9omibAyeZ9yBywx1FfBFic2YAGIpslEIBX31ap9AvdyficotaJkUhSicIANKHZUmRsDNrgSn5gsfXDnWibhZg/0?wx_fmt=jpeg\",\"title\":\"test11\",\"url\":\"http://mp.weixin.qq.com/s?__biz=MzkzNzcwODU3NA==&tempkey=MTI3Ml81VVdsRkNNalV1ei9iSEw0NnYwaGNEYm1XcGlpNnBjaGNIWUZ5cXQ0dktKVzVlVW1zUXB6QUI2dEVhLUQzWk5tX1FxLXJrTHgzbFVMdUpNZG00MEpfT2J2a3lMdlJpV0pqRXBJVFFEeWJZYk9mMW56cVAxbEpnZGkxaWY3Q3l3S1lWbTFYMktMUlVmZVRzNFJSTUhtZVEyZURaV0hJQmt4UGg2TlZRfn4%3D&chksm=428a1c7975fd956f34a2fb89ab2bbdb6868ce235f18cc6e91dcacd90611365576afbcad2422e#rd\"}]},\"media_id\":\"10amUGA13zbsCtl2JFj7eZIH97p9HKMnsxIewipOCgLM37kKeKn9NlmEYDt5_FzJ\",\"update_time\":1717006204},{\"content\":{\"news_item\":[{\"author\":\"\",\"content\":\"<p>测试1</p>\",\"content_source_url\":\"\",\"digest\":\"测试1\",\"need_open_comment\":0,\"only_fans_can_comment\":0,\"show_cover_pic\":0,\"thumb_media_id\":\"10amUGA13zbsCtl2JFj7eah_GBkgYrEqfvLouafxGTrEaiEvi9rH23_39cIkYPQ4\",\"thumb_url\":\"http://mmbiz.qpic.cn/mmbiz_jpg/FQdfXv9omibAyeZ9yBywx1FfBFic2YAGIpslEIBX31ap9AvdyficotaJkUhSicIANKHZUmRsDNrgSn5gsfXDnWibhZg/0?wx_fmt=jpeg\",\"title\":\"测试1\",\"url\":\"http://mp.weixin.qq.com/s?__biz=MzkzNzcwODU3NA==&tempkey=MTI3Ml8rV1Z6Sjl0TjNaRStBSGdlNnYwaGNEYm1XcGlpNnBjaGNIWUZ5cXQ0dktKVzVlVW1zUXB6QUI2dEVhLTVHSFlNQWJFNG1JZDk5SUxWX29FcXdOYVI0eXM3ZkN0bHdOQWRkRUVrOGJZakVwUldtWkY5dDBvblJfMm01UVhQcDVqdzkwZ1E3QU5ZcFdENmlzTlBVV1IxbHNoNzJBT3dkenBhYU0tRG5nfn4%3D&chksm=428a1c7475fd956209396c3b5bb4838a3bb69e30b7acfaf7417381e00185b7f68b0b62529ed8#rd\"}]},\"media_id\":\"10amUGA13zbsCtl2JFj7eaUdDkaKxpL6Pv-P_Ts9c1mmK4vnvoDi8mMq0ER5uITL\",\"update_time\":1717005996},{\"content\":{\"news_item\":[{\"author\":\"\",\"content\":\"<p>测试</p>\",\"content_source_url\":\"\",\"digest\":\"测试\",\"need_open_comment\":0,\"only_fans_can_comment\":0,\"show_cover_pic\":0,\"thumb_media_id\":\"10amUGA13zbsCtl2JFj7eah_GBkgYrEqfvLouafxGTrEaiEvi9rH23_39cIkYPQ4\",\"thumb_url\":\"http://mmbiz.qpic.cn/mmbiz_jpg/FQdfXv9omibAyeZ9yBywx1FfBFic2YAGIpslEIBX31ap9AvdyficotaJkUhSicIANKHZUmRsDNrgSn5gsfXDnWibhZg/0?wx_fmt=jpeg\",\"title\":\"测试\",\"url\":\"http://mp.weixin.qq.com/s?__biz=MzkzNzcwODU3NA==&tempkey=MTI3Ml9yazRIRVAvRm5ORmRFaEhxNnYwaGNEYm1XcGlpNnBjaGNIWUZ5cXQ0dktKVzVlVW1zUXB6QUI2dEVhX3RCcDN2SDE4TkpwM1ZMLThLaHZaN0lEV0ZTRkxkZmNBMU13MGlpQ0ZpdDRzMjlsbzN0SWRMUlQxVUhMRHV3bnpwNEQ2NDdmRFNINzM4eHd5NkozakdOV0NTbmVWN1hSRDZhRjNXUmI2Y2tBfn4%3D&chksm=428a1c7375fd95650e906a32cde9c8fd794173f41beeedf704dfef06076cbcdadc32ffa5467e#rd\"}]},\"media_id\":\"10amUGA13zbsCtl2JFj7eaj1EHctbWKZ051p8BvCg2h9S2cCvHvcVHSYtN3UrUg1\",\"update_time\":1717005874}],\"item_count\":6,\"total_count\":6}";

        WxMpDraftList wxMpDraftList = WxMpDraftList.fromJson(json);
        return wxMpDraftList;

    }


//    public static void main(String[] args) {
//        WxMpDraftList wxMpDraftList = testBatchGet();
//        System.out.println(wxMpDraftList);
//    }


    /**
     * 添加图文永久素材：使用方法为addDraft 添加图文
     *
     * @param articles
     * @return
     * @throws WxErrorException
     */
    @PostMapping("/materialNewsUpload")
    @RequiresPermissions("wx:wxassets:save")
    @ApiOperation(value = "添加图文永久素材")
    public R materialNewsUpload(@CookieValue String appid, @RequestBody List<WxMpDraftArticles> articles) throws WxErrorException {
        if (articles.isEmpty()) {
            return R.error("图文列表不得为空");
        }
        //添加永久素材 test
//        WxMpMaterialUploadResult res = new WxMpMaterialUploadResult();
        WxMpMaterialUploadResult res = wxAssetsService.addDraft(appid, articles);
        return R.ok().put(res);
    }

    /**
     * 修改图文素材文章
     *
     * @param form
     * @return
     * @throws WxErrorException
     */
    @PostMapping("/materialArticleUpdate")
    @RequiresPermissions("wx:wxassets:save")
    @ApiOperation(value = "修改图文素材文章")
    public R materialArticleUpdate(@CookieValue String appid, @RequestBody WxMpUpdateDraft form) throws WxErrorException {
        if (form.getArticles() == null) {
            return R.error("文章不得为空");
        }
        // 更新图文素材
        wxAssetsService.materialArticleUpdate(appid, form);
        return R.ok();
    }

    /**
     * 添加多媒体非图文永久素材
     *
     * @param file
     * @param fileName
     * @param mediaType
     * @return
     * @throws WxErrorException
     * @throws IOException
     */
    @PostMapping("/materialFileUpload")
    @RequiresPermissions("wx:wxassets:save")
    @ApiOperation(value = "添加多媒体永久素材")
    public R materialFileUpload(@CookieValue String appid, MultipartFile file, String fileName, String mediaType) throws WxErrorException, IOException {
        if (file == null) {
            return R.error("文件不得为空");
        }

        WxMpMaterialUploadResult res = wxAssetsService.materialFileUpload(appid, mediaType, fileName, file);
        return R.ok().put(res);
    }

    /**
     * 删除素材
     *
     * @param form
     * @return
     * @throws WxErrorException
     */
    @PostMapping("/materialDelete")
    @RequiresPermissions("wx:wxassets:delete")
    @ApiOperation(value = "删除素材")
    public R materialDelete(@CookieValue String appid, @RequestBody MaterialFileDeleteForm form) throws WxErrorException {
        boolean res = wxAssetsService.materialDelete(appid, form.getMediaId());
        return R.ok().put(res);
    }


    /**
     * 删除草稿
     *
     * @param form
     * @return
     * @throws WxErrorException
     */
    @PostMapping("/draftDelete")
    @RequiresPermissions("wx:wxassets:delete")
    @ApiOperation(value = "删除草稿")
    public R draftDelete(@CookieValue String appid, @RequestBody WxMpUpdateDraft form) throws WxErrorException {
        boolean res = wxAssetsService.draftDelete(appid, form.getMediaId());
        //test
//        boolean res = true;
        return R.ok().put(res);
    }

    /**
     * 发布草稿
     *
     * @param form
     * @return
     * @throws WxErrorException
     */
    @PostMapping("/freePublish")
    @RequiresPermissions("wx:wxassets:delete")
    @ApiOperation(value = "发布草稿")
    public R freePublish(@CookieValue String appid, @RequestBody WxMpUpdateDraft form) throws WxErrorException {
        String s = wxAssetsService.submitDraftList(appid, form.getMediaId());
        return R.ok().put(s);
    }

    /**
     * 获取成功发布列表
     * @param appid
     * @param offset
     * @param count
     * @param noContent
     * @return
     * @throws WxErrorException
     */
    @GetMapping("/freepublish/batchget")
    @RequiresPermissions("wx:wxassets:delete")
    @ApiOperation(value = "查询发布成功草稿")
    public R freePublishBatchGet(@CookieValue String appid, @RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "20") int count, @RequestParam(defaultValue = "0") int noContent) throws WxErrorException {
        WxMpFreePublishList publicationRecords = wxAssetsService.getPublicationRecords(appid, offset, count, noContent);
//        WxMpFreePublishList publicationRecords = getTest();
        return R.ok().put(publicationRecords);
    }

    /**
     * 删除发布草稿
     *
     * @param form
     * @return
     * @throws WxErrorException
     */
    @PostMapping("/publicDelete")
    @RequiresPermissions("wx:wxassets:delete")
    @ApiOperation(value = "删除发布成功列表")
    public R publicDelete(@CookieValue String appid, @RequestBody WxMpUpdateDraft form) throws WxErrorException {
        boolean res = wxAssetsService.deletePushAllArticle(appid, form.getMediaId());
        //test
//        boolean res = true;
        return R.ok().put(res);
    }





    public WxMpFreePublishList getTest(){
        String json="{\"item\":[{\"article_id\":\"YYuxknr3b1x1zf5a73qXZ0t8twXFFNetdKy82mb0dM_eXqK9IxUWJ8IbXPktLH7M\",\"content\":{\"news_item\":[{\"title\":\"test111\",\"author\":\"\",\"digest\":\"1234\",\"content\":\"<p>1234<\\/p>\",\"content_source_url\":\"\",\"thumb_media_id\":\"PztNGeFSv05NSwLwGxgk9cyiicTfJVCLPYigzHmFUvcLgYriO8fL1EGAgydSQBok\",\"show_cover_pic\":0,\"url\":\"http:\\/\\/mp.weixin.qq.com\\/s?__biz=MzkyODcwNzA4Mw==&mid=2247483665&idx=1&sn=391d2a7be9b99f25fab833101613e79a&chksm=c215e675f5626f63fb90aa12f996ec3a21cb53d9f295df7e3875ad8b6bd1a6f9c11359570695#rd\",\"thumb_url\":\"http:\\/\\/mmbiz.qpic.cn\\/mmbiz_jpg\\/qMzwtoo4WtHIjC9Z9icvpIPmN9zsKzfibwqkxDQjNnGKlicBgtKjQRYygK3ibdNunV5Qxiba9KByVjescn70AtcGSAQ\\/0?wx_fmt=jpeg\",\"need_open_comment\":1,\"only_fans_can_comment\":1,\"is_deleted\":false}],\"create_time\":1717341075,\"update_time\":1717341107},\"update_time\":1717341107}],\"total_count\":1,\"item_count\":1}";
        WxMpFreePublishList wxMpFreePublishList1 = WxMpFreePublishList.fromJson(json);
        return wxMpFreePublishList1;
    }



}
