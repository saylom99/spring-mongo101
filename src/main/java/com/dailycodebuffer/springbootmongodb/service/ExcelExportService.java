package com.dailycodebuffer.springbootmongodb.service;

import com.dailycodebuffer.springbootmongodb.dto.*;
import com.dailycodebuffer.springbootmongodb.model.Transaction;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.*;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class ExcelExportService {

    public byte[] exportTransactions(List<Transaction> transactions) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Transactions");


            Row headerRow = sheet.createRow(0);
            String[] headers = {"Date", "Type", "Amount"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            int rowNum = 1;
            for (Transaction transaction : transactions) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(
                        transaction.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                row.createCell(1).setCellValue(transaction.getType());
                row.createCell(2).setCellValue(transaction.getAmount());
            }

            return writeWorkbookToByteArray(workbook);
        } catch (Exception e) {
            throw new RuntimeException("Failed to export transactions", e);
        }
    }


    public byte[] exportSummary(Map<String, Object> summary) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Summary");

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Transaction Type");
            headerRow.createCell(1).setCellValue("Amount");
            headerRow.createCell(2).setCellValue("Count");

            // Add type-wise details
            List<Map<String, Object>> types = (List<Map<String, Object>>) summary.get("types");
            int rowNum = 1;
            for (Map<String, Object> type : types) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue((String) type.get("type"));
                row.createCell(1).setCellValue((Double) type.get("totalAmount"));
                row.createCell(2).setCellValue((Long) type.get("count"));
            }

            // Auto-size columns
            for (int i = 0; i < 3; i++) {
                sheet.autoSizeColumn(i);
            }

            return writeWorkbookToByteArray(workbook);
        } catch (Exception e) {
            throw new RuntimeException("Failed to export summary", e);
        }
    }


    public byte[] exportDistribution(List<TypeDistributionDTO> distribution) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Distribution");

            // Add data for chart
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Type");
            headerRow.createCell(1).setCellValue("Percentage");

            int rowNum = 1;
            for (TypeDistributionDTO type : distribution) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(type.getType());
                row.createCell(1).setCellValue(type.getPercentage());
            }

            // Create pie chart
            XSSFDrawing drawing = sheet.createDrawingPatriarch();
            XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 4, 0, 15, 15);

            XSSFChart chart = drawing.createChart(anchor);
            chart.setTitleText("Type Distribution");
            chart.setTitleOverlay(false);

            // Create data sources for the chart
            XDDFDataSource<String> types = XDDFDataSourcesFactory.fromStringCellRange(sheet,
                    new CellRangeAddress(1, distribution.size(), 0, 0));
            XDDFNumericalDataSource<Double> values = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                    new CellRangeAddress(1, distribution.size(), 1, 1));

            // Create pie chart data
            XDDFChartData data = chart.createData(ChartTypes.PIE, null, null);
            XDDFChartData.Series series = data.addSeries(types, values);

            // Plot the chart
            chart.plot(data);

            // Style the chart
            XDDFChartLegend legend = chart.getOrAddLegend();
            legend.setPosition(LegendPosition.RIGHT);

            return writeWorkbookToByteArray(workbook);
        } catch (Exception e) {
            throw new RuntimeException("Failed to export distribution", e);
        }
    }

    // Helper method to write workbook to byte array
    private byte[] writeWorkbookToByteArray(XSSFWorkbook workbook) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}