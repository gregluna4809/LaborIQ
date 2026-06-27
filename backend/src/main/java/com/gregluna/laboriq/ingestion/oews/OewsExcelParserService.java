package com.gregluna.laboriq.ingestion.oews;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.Styles;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.springframework.stereotype.Service;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
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
    private static final int MAX_OEWS_RECORD_BYTES = 600_000_000;

    public List<OewsRow> parse(byte[] zipBytes) {
        try {
            byte[] xlsxBytes = extractXlsx(zipBytes);
            List<OewsRow> rows = parseXlsx(xlsxBytes);
            log.info("Parsed {} occupation rows from OEWS dataset", rows.size());
            return rows;
        } catch (IOException | OpenXML4JException | ParserConfigurationException | SAXException e) {
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

    private List<OewsRow> parseXlsx(byte[] xlsxBytes)
            throws IOException, OpenXML4JException, ParserConfigurationException, SAXException {
        IOUtils.setByteArrayMaxOverride(MAX_OEWS_RECORD_BYTES);
        try (OPCPackage pkg = OPCPackage.open(new ByteArrayInputStream(xlsxBytes))) {
            XSSFReader reader = new XSSFReader(pkg);
            Styles styles = reader.getStylesTable();
            ReadOnlySharedStringsTable sharedStrings = new ReadOnlySharedStringsTable(pkg);
            XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) reader.getSheetsData();
            if (!sheets.hasNext()) {
                return List.of();
            }

            StreamingSheetHandler sheetHandler = new StreamingSheetHandler();
            XMLReader parser = XMLHelper.newXMLReader();
            parser.setContentHandler(new XSSFSheetXMLHandler(
                    styles,
                    sharedStrings,
                    sheetHandler,
                    new DataFormatter(),
                    false
            ));

            try (var sheet = sheets.next()) {
                parser.parse(new InputSource(sheet));
            }
            return sheetHandler.rows();
        }
    }

    private OewsRow parseRow(Map<Integer, String> row, Map<String, Integer> colIndex) {
        if (!isNationalCrossIndustryRow(row, colIndex)) {
            return null;
        }

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

    private boolean isNationalCrossIndustryRow(Map<Integer, String> row, Map<String, Integer> colIndex) {
        String area = getString(row, colIndex, "AREA");
        String naics = getString(row, colIndex, "NAICS");
        if (area == null && naics == null) {
            return true;
        }
        return "99".equals(area) && "000000".equals(naics);
    }

    private Map<String, Integer> buildColumnIndex(Map<Integer, String> headerRow) {
        Map<String, Integer> index = new HashMap<>();
        headerRow.forEach((cellIndex, name) -> {
            if (name != null && !name.isBlank()) {
                index.put(name.toUpperCase(), cellIndex);
            }
        });
        return index;
    }

    private String getString(Map<Integer, String> row, Map<String, Integer> colIndex, String col) {
        Integer idx = colIndex.get(col);
        if (idx == null) return null;
        String val = row.get(idx);
        if (val == null) return null;
        val = val.trim();
        return val.isEmpty() ? null : val;
    }

    private BigDecimal getDecimal(Map<Integer, String> row, Map<String, Integer> colIndex, String col) {
        String val = getString(row, colIndex, col);
        if (val == null || NULL_MARKERS.contains(val)) return null;
        try {
            return new BigDecimal(val.replace(",", "")).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long getLong(Map<Integer, String> row, Map<String, Integer> colIndex, String col) {
        String val = getString(row, colIndex, col);
        if (val == null || NULL_MARKERS.contains(val)) return null;
        try {
            return Long.parseLong(val.replace(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private class StreamingSheetHandler implements XSSFSheetXMLHandler.SheetContentsHandler {

        private final List<OewsRow> rows = new ArrayList<>();
        private final Map<Integer, String> currentRow = new HashMap<>();
        private Map<String, Integer> colIndex = Map.of();

        @Override
        public void startRow(int rowNum) {
            currentRow.clear();
        }

        @Override
        public void endRow(int rowNum) {
            if (rowNum == 0) {
                colIndex = buildColumnIndex(currentRow);
                return;
            }

            OewsRow row = parseRow(currentRow, colIndex);
            if (row != null) {
                rows.add(row);
            }
        }

        @Override
        public void cell(String cellReference, String formattedValue, XSSFComment comment) {
            int column = columnIndex(cellReference);
            currentRow.put(column, formattedValue);
        }

        private List<OewsRow> rows() {
            return rows;
        }

        private int columnIndex(String cellReference) {
            int column = 0;
            for (int i = 0; i < cellReference.length(); i++) {
                char ch = cellReference.charAt(i);
                if (ch < 'A' || ch > 'Z') {
                    break;
                }
                column = (column * 26) + (ch - 'A' + 1);
            }
            return column - 1;
        }
    }
}
