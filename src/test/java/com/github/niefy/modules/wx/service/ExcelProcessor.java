package com.github.niefy.modules.wx.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * @Author zhangdongxu
 * @Description create bean
 * @Date 2024/6/11 23:26
 * @Version 1.0
 */
public class ExcelProcessor {
    public static void main(String[] args) throws IOException {
        String directoryPath = "C:\\Users\\zhangdongxu\\Desktop\\公众号开发\\任务列表";
        File dir = new File(directoryPath);

        File[] files = dir.listFiles((d, name) -> name.endsWith(".xlsx"));
        if (files == null || files.length == 0) {
            System.out.println("No .xlsx files found in the directory");
            return;
        }

        // Create the output Excel file
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet outputSheet = workbook.createSheet("Output");

            // Write headers
            Row headerRow = outputSheet.createRow(0);
            headerRow.createCell(0).setCellValue("XXX秘钥");
            headerRow.createCell(1).setCellValue("XXXurl");

            List<List<ExcelData>> allSheetsData = new ArrayList<>();

            for (File file : files) {
                try (FileInputStream fis = new FileInputStream(file);
                     Workbook inputWorkbook = new XSSFWorkbook(fis)) {

                    int numberOfSheets = inputWorkbook.getNumberOfSheets();
                    for (int i = 0; i < numberOfSheets; i++) {
                        Sheet sheet = inputWorkbook.getSheetAt(i);
                        if (sheet != null) {
                            List<ExcelData> singleSheetData = new ArrayList<>();
                            String platform = getCellValue(sheet, "A2");
                            String novelName = getCellValue(sheet, "B2");
                            List<String> chapterList = getColumnValues(sheet, "C2");
                            List<String> chapNameList = getColumnValues(sheet, "D2", true);
                            List<String> chapNameListRaw = getColumnValues(sheet, "D2", false);

                            for (int j = 0; j < chapterList.size(); j++) {
                                String chapter = chapterList.get(j);
                                String chapName = chapNameList.size() > j ? chapNameList.get(j) : "";
                                String strReq = "打开" + platform + "->搜索<" + novelName + "> 回复 " + chapter + "章节名称:" + chapName;
                                String originalChapName = chapNameListRaw.size() > j ? chapNameListRaw.get(j) : "";
                                singleSheetData.add(new ExcelData(strReq, originalChapName));
                            }
                            allSheetsData.add(singleSheetData);
                        }
                    }
                }
            }

            int rowIndex = 1;
            boolean dataRemaining = true;
            int currentIndex = 0;

            while (dataRemaining) {
                dataRemaining = false;
                for (List<ExcelData> sheetData : allSheetsData) {
                    if (currentIndex < sheetData.size()) {
                        dataRemaining = true;
                        Row row = outputSheet.createRow(rowIndex++);
                        row.createCell(0).setCellValue(sheetData.get(currentIndex).getStrReq());
                        row.createCell(1).setCellValue(sheetData.get(currentIndex).getOriginalChapName());
                    }
                }
                currentIndex++;
            }

            // Write the output file to disk
            try (FileOutputStream fos = new FileOutputStream(directoryPath + "\\XXX秘钥url.xlsx")) {
                workbook.write(fos);
            }
        }
    }

    private static String getCellValue(Sheet sheet, String cellReference) {
        CellReference ref = new CellReference(cellReference);
        Row row = sheet.getRow(ref.getRow());
        if (row != null) {
            Cell cell = row.getCell(ref.getCol());
            if (cell != null) {
                return cell.getStringCellValue();
            }
        }
        return "";
    }

    private static List<String> getColumnValues(Sheet sheet, String startCellReference) {
        return getColumnValues(sheet, startCellReference, false);
    }

    private static List<String> getColumnValues(Sheet sheet, String startCellReference, boolean obfuscate) {
        List<String> values = new ArrayList<>();
        CellReference ref = new CellReference(startCellReference);
        int startRow = ref.getRow();
        int colIdx = ref.getCol();

        for (int rowIdx = startRow; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
            Row row = sheet.getRow(rowIdx);
            if (row != null) {
                Cell cell = row.getCell(colIdx);
                if (cell != null) {
                    String value = cell.getStringCellValue();
                    if (obfuscate) {
                        value = obfuscateValue(value);
                    }
                    values.add(value);
                }
            }
        }

        return values;
    }

    private static String obfuscateValue(String value) {
        if (value.length() <= 1) {
            return "X";
        }
        StringBuilder obfuscated = new StringBuilder();
        obfuscated.append(value.charAt(0));
        for (int i = 1; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                obfuscated.append('X');
            } else {
                obfuscated.append(c);
            }
        }
        return obfuscated.toString();
    }

    private static class ExcelData {
        private final String strReq;
        private final String originalChapName;

        public ExcelData(String strReq, String originalChapName) {
            this.strReq = strReq;
            this.originalChapName = originalChapName;
        }

        public String getStrReq() {
            return strReq;
        }

        public String getOriginalChapName() {
            return originalChapName;
        }
    }
}
