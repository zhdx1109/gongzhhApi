package com.github.niefy.modules.wx.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.niefy.modules.wx.dao.WxTaskInfoMapper;
import com.github.niefy.modules.wx.dao.WxTaskResoInfoMapper;
import com.github.niefy.modules.wx.entity.WxFilmInfo;
import com.github.niefy.modules.wx.entity.WxTaskInfo;
import com.github.niefy.modules.wx.entity.WxTaskResoInfo;
import com.github.niefy.modules.wx.service.impl.WxFilmProcessServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @Author zhangdongxu
 * @Description create bean
 * @Date 2024/6/12 18:37
 * @Version 1.0
 */
@SpringBootTest
public class WxFilmProcessServiceImplTest {
    @Autowired
    WxFilmProcessServiceImpl wxFilmProcessService;

    @Autowired
    WxTaskInfoMapper wxTaskInfoMapper;

    @Autowired
    WxTaskResoInfoMapper wxTaskResoInfoMapper;



    //此方法用于同步TaskResource 任务资源表
    @Test
    void addTaskResourceProcessTest(){
        wxFilmProcessService.addTaskResourceProcess();
    }

    //此方法用于同步MsgReplyRule表
    @Test
    void addMsgReplyRuleProcessTest(){
        wxFilmProcessService.addMsgReplyRuleProcess();
    }



    //此方法用于生成资源目录
    @Test
    void addCategoryProcessorTest(){
        List<String> strings = Arrays.asList("1", "2", "3");
        wxFilmProcessService.addCategoryProcessor(strings,null,"all");
    }

    @Test
    void delFilmResourcesTest() {
//        wxFilmProcessService.delFilmResources("1",4,"影");
        wxFilmProcessService.delFilmResources("2",12,"黑社会的我成为高中生");
//        wxFilmProcessService.delFilmResources("1",13,"秦");
    }

    @Test
    void addFilmInfoTest() {
//        wxFilmProcessService.addFilmInfo("1",13,"秦");
        wxFilmProcessService.addFilmInfo("2",12,"黑社会的我成为高中生");

    }




    @Test
    void addSubCategoryListTest() {
        WxFilmInfo wxFilmInfo =new WxFilmInfo();
        wxFilmInfo.setFilmId(6);
        wxFilmInfo.setIsSingle("0");
        wxFilmInfo.setIsFollowUp("0");
        wxFilmInfo.setFilmName("黑夜传说");
        wxFilmInfo.setFilmType(1);
//        wxFilmProcessService.addSubCategoryList(wxFilmInfo);

        WxFilmInfo wxFilmInfo1 =new WxFilmInfo();
        wxFilmInfo1.setFilmId(2);
        wxFilmInfo1.setIsSingle("0");
        wxFilmInfo1.setIsFollowUp("1");
        wxFilmInfo1.setFilmName("斯巴达克斯");
        wxFilmInfo1.setFilmType(2);
//        wxFilmProcessService.addSubCategoryList(wxFilmInfo1);


        WxFilmInfo wxFilmInfo2 =new WxFilmInfo();
        wxFilmInfo2.setFilmId(1);
        wxFilmInfo2.setFilmName("漫威电影");
        wxFilmInfo2.setFilmNameDec("漫威系列电影共31部");
        wxFilmInfo2.setIsFollowUp("0");
        wxFilmInfo2.setIsSingle("0");
        wxFilmInfo2.setFilmType(1);
        wxFilmProcessService.addSubCategoryList(wxFilmInfo2);

    }




    //此方法用于将xx秘钥url.xlsx进行同步数据表:task_info表
    @Test
    void insertWxTaskInfoTest(){
        //读取C:\Users\zhangdongxu\Desktop\公众号开发\任务列表\XXX秘钥url.xlsx
        String filePath = "C:\\Users\\zhangdongxu\\Desktop\\公众号开发\\任务列表\\XXX秘钥url.xlsx";

        try (FileInputStream fileInputStream = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fileInputStream)) {

            Sheet sheet = workbook.getSheetAt(0); // 假设要读取第一个工作表

            Iterator<Row> rowIterator = sheet.iterator();
            if (rowIterator.hasNext()) { // 跳过第一行
                rowIterator.next();
            }

            System.out.println("A列数据：");
            System.out.println("B列数据：");

            int countA = 0;
            int countB = 0;

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Cell cellA = row.getCell(0); // A列
                Cell cellB = row.getCell(1); // B列

                String cellAValue = getCellValue(cellA);
                String cellBValue = getCellValue(cellB);
                WxTaskInfo wxTaskInfo = new WxTaskInfo();
                wxTaskInfo.setTaskName(cellAValue);
                wxTaskInfo.setTaskUrlCode(cellBValue);
                wxTaskInfo.setSyncUsed(false);
                wxTaskInfo.setStatus(true);
                wxTaskInfo.setUpdateTime(new Date());
                try {
                    wxTaskInfoMapper.insert(wxTaskInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }

//                System.out.println("A: " + cellAValue + ", B: " + cellBValue);

                countA++;
                countB++;
            }

            System.out.println("A列数据数量: " + countA);
            System.out.println("B列数据数量: " + countB);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private static String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return "";
        }
    }
}
