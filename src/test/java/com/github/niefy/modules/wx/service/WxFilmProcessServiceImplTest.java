package com.github.niefy.modules.wx.service;

import com.github.niefy.modules.wx.dao.WxTaskInfoMapper;
import com.github.niefy.modules.wx.entity.WxTaskInfo;
import com.github.niefy.modules.wx.service.impl.WxFilmProcessServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
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

    @Test
    void queryWxFilmProcess(){
        wxFilmProcessService.filmResourceProcess();
    }

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
                wxTaskInfo.setTaskUrl(cellBValue);
                wxTaskInfo.setIsUsed("0");
                wxTaskInfo.setStatus("1");
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
