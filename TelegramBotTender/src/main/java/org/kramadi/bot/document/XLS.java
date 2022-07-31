package org.kramadi.bot.document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kramadi.bot.MySQL.TenderEntity;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class XLS {

    private static String[] columns = { "Найменування", "Організація", "Статус",
                                        "Дата початку", "Дата закінчення", "Вартість",
                                        "URL", "Дата додавання", "Дата оновлення" };
    private static List<TenderEntity> tenders1 = new ArrayList<TenderEntity>();
    public static void main(String[] args) throws IOException{
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("tenders");

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 14);
        headerFont.setColor(IndexedColors.RED.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

// Создание заголовка

        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }

// Заполнение остальных строк информацией о тендерах
        int rowNum = 1;

        for (TenderEntity tender : tenders1) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(tender.getSubject());
            row.createCell(1).setCellValue(tender.getOrganization());
            row.createCell(2).setCellValue(tender.getStatus());
            row.createCell(3).setCellValue(tender.getStartDate());
            row.createCell(4).setCellValue(tender.getEndDate());
            row.createCell(5).setCellValue(tender.getPrice());
            row.createCell(6).setCellValue(tender.getUrl());
            row.createCell(7).setCellValue(tender.getFoundDate());
            row.createCell(8).setCellValue(tender.getUpdateDate());	}

// изменение ширины колонок по размерам текста
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

// запись результата в файл
        FileOutputStream fileOut = new FileOutputStream(tenders1.get(0).getSearchBySearchId().getName()+"tenders.xls ");
        workbook.write(fileOut); fileOut.close();
    }

    public static File create(ArrayList<TenderEntity> tenders){
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("tenders");
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.RED.getIndex());
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

// создание строки заголовков
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }
// создание остальных строк с информацией по найденным тендерам
        int rowNum = 1;
        for (TenderEntity tender : tenders) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(tender.getSubject());
            row.createCell(1).setCellValue(tender.getOrganization());
            row.createCell(2).setCellValue(tender.getStatus());
            row.createCell(3).setCellValue(tender.getStartDate());
            row.createCell(4).setCellValue(tender.getEndDate());
            row.createCell(5).setCellValue(tender.getPrice());
            row.createCell(6).setCellValue(tender.getUrl());
            row.createCell(7).setCellValue(tender.getFoundDate().toString());
            row.createCell(8).setCellValue(tender.getUpdateDate().toString());
        }
// изменение ширины колонок по длине текста
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }
        File file = null;
// запись результата в файл
        try {
            file = new File(tenders.get(0).getSearchBySearchId().getName() + ".xls");
            ((HSSFWorkbook) workbook).write(file);
        }catch (IOException ex){ex.printStackTrace();}
        return file;
    }
}


