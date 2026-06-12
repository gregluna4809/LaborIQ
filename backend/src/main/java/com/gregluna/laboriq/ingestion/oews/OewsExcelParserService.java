package com.gregluna.laboriq.ingestion.oews;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Service
public class OewsExcelParserService {

    private static final Set<String> NULL_MARKERS = Set.of("#", "*", "**");

    public List<OewsRow> parse(byte[] zipBytes) {
        try {
            byte[] xlsxBytes = extractXlsx(zipBytes);
            List<OewsRow> rows = parseXlsx(xlsxBytes);
            log.info("Parsed {} occupation rows from OEWS dataset", rows.size());
            return rows;
        } catch (IOException e) {
            throw new OewsIngestionException("Failed to parse OEWS dataset", e);
        }
    }

    private byte[] extractXlsx(byte[] zipBytes) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().toLowerCase().endsWith(".xlsx")) {
                    log.debug("Extracting {} from ZIP", entry.getName());
                    return zis.readAllBytes();
                }
                zis.closeEntry();
            }
        }
        throw new OewsIngestionException("No .xlsx file found in OEWS ZIP archive");
    }

    private List<OewsRow> parseXlsx(byte[] xlsxBytes) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(xlsxBytes))) {
            Sheet sheet = wb.getSheetAt(0);
            Map<String, Integer> colIndex = buildColumnIndex(sheet.getRow(0));
            List<OewsRow> rows = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                OewsRow parsed = parseRow(row, colIndex);
                if (parsed != null) rows.add(parsed);
            }
            return rows;
        }
    }

    private Map<String, Integer> buildColumnIndex(Row headerRow) {
        Map<String, Integer> index = new HashMap<>();
        if (headerRow == null) return index;
        for (Cell cell : headerRow) {
            String name = stringValue(cell);
            if (name != null) index.put(name.toUpperCase(), cell.getColumnIndex());
        }
        return index;
    }

    private OewsRow parseRow(Row row, Map<String, Integer> colIndex) {
        String occCode = getString(row, colIndex, "OCC_CODE");
        if (occCode == null || occCode.isBlank()) return null;

        return new OewsRow(
                occCode,
                getString(row, colIndex, "OCC_TITLE"),
                getString(row, colIndex, "O_GROUP"),
                getLong(row, colIndex, "TOT_EMP"),
                getDecimal(row, colIndex, "A_MEAN"),
                getDecimal(row, colIndex, "A_MEDIAN"),
                getDecimal(row, colIndex, "A_PCT10"),
                getDecimal(row, colIndex, "A_PCT25"),
                getDecimal(row, colIndex, "A_PCT75"),
                getDecimal(row, colIndex, "A_PCT90")
        );
    }

    private String getString(Row row, Map<String, Integer> colIndex, String col) {
        Integer idx = colIndex.get(col);
        if (idx == null) return null;
        return stringValue(row.getCell(idx));
    }

    private BigDecimal getDecimal(Row row, Map<String, Integer> colIndex, String col) {
        Integer idx = colIndex.get(col);
        if (idx == null) return null;
        Cell cell = row.getCell(idx);
        if (cell == null || cell.getCellType() == CellType.BLANK) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP);
        }
        if (cell.getCellType() == CellType.STRING) {
            String val = cell.getStringCellValue().trim();
            if (val.isEmpty() || NULL_MARKERS.contains(val)) return null;
            try {
                return new BigDecimal(val).setScale(2, RoundingMode.HALF_UP);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private Long getLong(Row row, Map<String, Integer> colIndex, String col) {
        Integer idx = colIndex.get(col);
        if (idx == null) return null;
        Cell cell = row.getCell(idx);
        if (cell == null || cell.getCellType() == CellType.BLANK) return null;
        if (cell.getCellType() == CellType.NUMERIC) return (long) cell.getNumericCellValue();
        if (cell.getCellType() == CellType.STRING) {
            String val = cell.getStringCellValue().trim();
            if (val.isEmpty() || NULL_MARKERS.contains(val)) return null;
            try {
                return Long.parseLong(val.replace(",", ""));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private String stringValue(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) return null;
        if (cell.getCellType() == CellType.STRING) {
            String val = cell.getStringCellValue().trim();
            return val.isEmpty() ? null : val;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((long) cell.getNumericCellValue());
        }
        return null;
    }
}
