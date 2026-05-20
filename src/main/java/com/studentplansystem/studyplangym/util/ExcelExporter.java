package com.studentplansystem.studyplangym.util;

import com.studentplansystem.studyplangym.entity.StudyPlan;
import com.studentplansystem.studyplangym.entity.SubjectEntry;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ExcelExporter {

    public static void exportNewFormat(List<StudyPlan> plans, HttpServletResponse response) throws IOException {
        Workbook workbook = new XSSFWorkbook();

        Sheet matrixSheet = workbook.createSheet("Mokinių pasirinkimai");
        Sheet generalSheet = workbook.createSheet("Bendra informacija");
        Sheet gradeThreeSheet = workbook.createSheet("III klasė");
        Sheet gradeFourSheet = workbook.createSheet("IV klasė");

        Map<String, Short> subjectColors = createSubjectColors();
        List<String> subjects = getAllSubjects(plans);

        createMatrixSheet(workbook, matrixSheet, plans, subjects, subjectColors);
        createGeneralSheet(workbook, generalSheet, plans);
        createGradeSpecificSheet(workbook, gradeThreeSheet, plans, subjects, "III");
        createGradeSpecificSheet(workbook, gradeFourSheet, plans, subjects, "IV");

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    private static void createMatrixSheet(
            Workbook workbook,
            Sheet sheet,
            List<StudyPlan> plans,
            List<String> subjects,
            Map<String, Short> subjectColors
    ) {
        sheet.setDisplayGridlines(true);

        CellStyle metaHeaderStyle = createMetaHeaderStyle(workbook);
        CellStyle studentStyle = createStudentStyle(workbook);
        CellStyle numberStyle = createNumberStyle(workbook);
        CellStyle selectedStyle = createNumberStyleWithFill(workbook, IndexedColors.LIGHT_GREEN.getIndex());

        Row titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(190);

        Row filterRow = sheet.createRow(1);
        filterRow.setHeightInPoints(20);

        createCell(titleRow, 0, "Nr.", metaHeaderStyle);
        createCell(titleRow, 1, "Mokinio vardas ir pavardė", metaHeaderStyle);
        createCell(titleRow, 2, "Klasė", metaHeaderStyle);
        createCell(titleRow, 3, "Mokslo metai", metaHeaderStyle);

        createCell(filterRow, 0, "", metaHeaderStyle);
        createCell(filterRow, 1, "", metaHeaderStyle);
        createCell(filterRow, 2, "", metaHeaderStyle);
        createCell(filterRow, 3, "", metaHeaderStyle);

        int subjectStartCol = 4;

        for (int i = 0; i < subjects.size(); i++) {
            String subjectName = subjects.get(i);
            int col = subjectStartCol + i;

            Short color = subjectColors.getOrDefault(subjectName, IndexedColors.LIGHT_BLUE.getIndex());
            CellStyle subjectHeaderStyle = createVerticalHeaderStyle(workbook, color);

            createCell(titleRow, col, subjectName, subjectHeaderStyle);
            createCell(filterRow, col, "", createFilterRowStyle(workbook, color));

            sheet.setColumnWidth(col, 1600);
        }

        int rowIndex = 2;
        int number = 1;

        for (StudyPlan plan : plans) {
            Row row = sheet.createRow(rowIndex++);
            row.setHeightInPoints(19);

            createCell(row, 0, formatStudentNumber(number), studentStyle);
            createCell(row, 1, safe(plan.getFullName()), studentStyle);
            createCell(row, 2, safe(plan.getClassYear()), studentStyle);
            createCell(row, 3, safe(plan.getSchoolYear()), studentStyle);

            Map<String, SubjectEntry> selectedSubjects = new HashMap<>();

            if (plan.getSubjects() != null) {
                for (SubjectEntry subject : plan.getSubjects()) {
                    selectedSubjects.put(subject.getSubject(), subject);
                }
            }

            for (int i = 0; i < subjects.size(); i++) {
                String subjectName = subjects.get(i);
                int col = subjectStartCol + i;

                SubjectEntry entry = selectedSubjects.get(subjectName);

                if (entry == null) {
                    createCell(row, col, "", numberStyle);
                    continue;
                }

                String value = NumberOrZero(entry.getGradeIiiHours()) + "/" + NumberOrZero(entry.getGradeIvHours());
                createCell(row, col, value, selectedStyle);
            }

            number++;
        }

        sheet.setColumnWidth(0, 1600);
        sheet.setColumnWidth(1, 7800);
        sheet.setColumnWidth(2, 2200);
        sheet.setColumnWidth(3, 3600);

        sheet.createFreezePane(4, 2);
        sheet.setAutoFilter(new CellRangeAddress(1, Math.max(1, rowIndex - 1), 0, subjectStartCol + subjects.size() - 1));

        addLegend(workbook, sheet, rowIndex + 2);
    }

    private static void createGeneralSheet(Workbook workbook, Sheet sheet, List<StudyPlan> plans) {
        CellStyle headerStyle = createBlueHeaderStyle(workbook);
        CellStyle normalStyle = createStudentStyle(workbook);

        Row header = sheet.createRow(0);

        String[] headers = {
                "Nr.",
                "Mokinio vardas ir pavardė",
                "Mokinio numeris",
                "Klasė",
                "Mokslo metai",
                "Pasirinkti dalykai",
                "III klasės dalykai",
                "III klasės valandos",
                "IV klasės dalykai",
                "IV klasės valandos",
                "Iš viso valandų"
        };

        for (int i = 0; i < headers.length; i++) {
            createCell(header, i, headers[i], headerStyle);
        }

        int rowIndex = 1;
        int number = 1;

        for (StudyPlan plan : plans) {
            Row row = sheet.createRow(rowIndex++);

            int selectedSubjects = plan.getSubjects() == null ? 0 : plan.getSubjects().size();
            int gradeThreeSubjects = selectedSubjects;
            int gradeFourSubjects = selectedSubjects;

            int gradeThreeHours = 0;
            int gradeFourHours = 0;

            if (plan.getSubjects() != null) {
                for (SubjectEntry subject : plan.getSubjects()) {
                    gradeThreeHours += NumberOrZero(subject.getGradeIiiHours());
                    gradeFourHours += NumberOrZero(subject.getGradeIvHours());
                }
            }

            createCell(row, 0, number++, normalStyle);
            createCell(row, 1, safe(plan.getFullName()), normalStyle);
            createCell(row, 2, safe(plan.getStudentNumber()), normalStyle);
            createCell(row, 3, safe(plan.getClassYear()), normalStyle);
            createCell(row, 4, safe(plan.getSchoolYear()), normalStyle);
            createCell(row, 5, selectedSubjects, normalStyle);
            createCell(row, 6, gradeThreeSubjects, normalStyle);
            createCell(row, 7, gradeThreeHours, normalStyle);
            createCell(row, 8, gradeFourSubjects, normalStyle);
            createCell(row, 9, gradeFourHours, normalStyle);
            createCell(row, 10, gradeThreeHours + gradeFourHours, normalStyle);
        }

        autoSizeColumns(sheet, headers.length);
        sheet.createFreezePane(0, 1);
        sheet.setAutoFilter(new CellRangeAddress(0, Math.max(0, rowIndex - 1), 0, headers.length - 1));
    }

    private static void createGradeSpecificSheet(
            Workbook workbook,
            Sheet sheet,
            List<StudyPlan> plans,
            List<String> subjects,
            String grade
    ) {
        CellStyle headerStyle = createBlueHeaderStyle(workbook);
        CellStyle normalStyle = createStudentStyle(workbook);
        CellStyle numberStyle = "III".equalsIgnoreCase(grade)
                ? createNumberStyleWithFill(workbook, IndexedColors.LIGHT_YELLOW.getIndex())
                : createNumberStyleWithFill(workbook, IndexedColors.PALE_BLUE.getIndex());

        Row header = sheet.createRow(0);

        createCell(header, 0, "Nr.", headerStyle);
        createCell(header, 1, "Mokinio vardas ir pavardė", headerStyle);
        createCell(header, 2, "Mokinio numeris", headerStyle);
        createCell(header, 3, "Klasė", headerStyle);
        createCell(header, 4, "Mokslo metai", headerStyle);

        int subjectStartCol = 5;

        for (int i = 0; i < subjects.size(); i++) {
            createCell(header, subjectStartCol + i, subjects.get(i), headerStyle);
        }

        createCell(header, subjectStartCol + subjects.size(), "Iš viso valandų", headerStyle);

        int rowIndex = 1;
        int number = 1;

        for (StudyPlan plan : plans) {
            Row row = sheet.createRow(rowIndex++);

            createCell(row, 0, number++, normalStyle);
            createCell(row, 1, safe(plan.getFullName()), normalStyle);
            createCell(row, 2, safe(plan.getStudentNumber()), normalStyle);
            createCell(row, 3, safe(plan.getClassYear()), normalStyle);
            createCell(row, 4, safe(plan.getSchoolYear()), normalStyle);

            Map<String, SubjectEntry> selectedSubjects = new HashMap<>();

            if (plan.getSubjects() != null) {
                for (SubjectEntry subject : plan.getSubjects()) {
                    selectedSubjects.put(subject.getSubject(), subject);
                }
            }

            int total = 0;

            for (int i = 0; i < subjects.size(); i++) {
                String subjectName = subjects.get(i);
                SubjectEntry entry = selectedSubjects.get(subjectName);

                int col = subjectStartCol + i;

                if (entry == null) {
                    createCell(row, col, "", normalStyle);
                } else {
                    int hours = "III".equalsIgnoreCase(grade)
                            ? NumberOrZero(entry.getGradeIiiHours())
                            : NumberOrZero(entry.getGradeIvHours());

                    total += hours;
                    createCell(row, col, hours, numberStyle);
                }
            }

            createCell(row, subjectStartCol + subjects.size(), total, normalStyle);
        }

        autoSizeColumns(sheet, subjectStartCol + subjects.size() + 1);
        sheet.createFreezePane(5, 1);
        sheet.setAutoFilter(new CellRangeAddress(0, Math.max(0, rowIndex - 1), 0, subjectStartCol + subjects.size()));
    }

    private static List<String> getAllSubjects(List<StudyPlan> plans) {
        List<String> preferredOrder = getPreferredSubjectOrder();

        Set<String> selectedSubjects = plans.stream()
                .filter(plan -> plan.getSubjects() != null)
                .flatMap(plan -> plan.getSubjects().stream())
                .map(SubjectEntry::getSubject)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<String> ordered = new ArrayList<>();

        for (String subject : preferredOrder) {
            if (selectedSubjects.contains(subject)) {
                ordered.add(subject);
            }
        }

        List<String> remaining = selectedSubjects.stream()
                .filter(subject -> !ordered.contains(subject))
                .sorted()
                .collect(Collectors.toList());

        ordered.addAll(remaining);

        return ordered;
    }

    private static List<String> getPreferredSubjectOrder() {
        return Arrays.asList(
                "Lietuvių kalba ir literatūra",
                "Lietuvių kalba ir literatūra A",
                "Lietuvių kalba ir literatūra B",
                "Matematika",
                "Matematika A",
                "Matematika B",
                "Fizinis ugdymas",
                "Tikyba",
                "Etika",
                "Užsienio kalba (anglų)",
                "Anglų kalba (B2)",
                "Užsienio kalba (prancūzų)",
                "Užsienio kalba (vokiečių)",
                "Biologija",
                "Chemija",
                "Fizika",
                "Informatika",
                "Istorija 4 val.",
                "Istorija (4 val.)",
                "Istorija 3 val.",
                "Istorija (3 val.)",
                "Geografija",
                "Ekonomika ir verslumas",
                "Dailė",
                "Muzika",
                "Šokis",
                "Teatras",
                "Mityba",
                "Tekstilė",
                "Technologijos ir dizainas",
                "Užsienio kalba (vokiečių, B1 lygis)",
                "Užsienio kalba (vokiečių, pradedantiesiems)",
                "Užsienio kalba (prancūzų, B1 lygis)",
                "Užsienio kalba (prancūzų, pradedantiesiems)",
                "Užsienio kalba (rusų, B1 lygis)",
                "Užsienio kalba (anglų) modulis"
        );
    }

    private static Map<String, Short> createSubjectColors() {
        Map<String, Short> colors = new HashMap<>();

        colors.put("Lietuvių kalba ir literatūra", IndexedColors.RED.getIndex());
        colors.put("Lietuvių kalba ir literatūra A", IndexedColors.RED.getIndex());
        colors.put("Lietuvių kalba ir literatūra B", IndexedColors.RED.getIndex());

        colors.put("Matematika", IndexedColors.YELLOW.getIndex());
        colors.put("Matematika A", IndexedColors.YELLOW.getIndex());
        colors.put("Matematika B", IndexedColors.YELLOW.getIndex());

        colors.put("Fizinis ugdymas", IndexedColors.TAN.getIndex());

        colors.put("Tikyba", IndexedColors.SKY_BLUE.getIndex());
        colors.put("Etika", IndexedColors.SKY_BLUE.getIndex());

        colors.put("Anglų kalba (B2)", IndexedColors.BROWN.getIndex());
        colors.put("Užsienio kalba (anglų)", IndexedColors.BROWN.getIndex());
        colors.put("Užsienio kalba (prancūzų)", IndexedColors.BROWN.getIndex());
        colors.put("Užsienio kalba (vokiečių)", IndexedColors.BROWN.getIndex());

        colors.put("Biologija", IndexedColors.ROSE.getIndex());
        colors.put("Chemija", IndexedColors.ROSE.getIndex());
        colors.put("Fizika", IndexedColors.ROSE.getIndex());
        colors.put("Informatika", IndexedColors.ROSE.getIndex());

        colors.put("Istorija 4 val.", IndexedColors.LIGHT_GREEN.getIndex());
        colors.put("Istorija (4 val.)", IndexedColors.LIGHT_GREEN.getIndex());
        colors.put("Istorija 3 val.", IndexedColors.LIGHT_GREEN.getIndex());
        colors.put("Istorija (3 val.)", IndexedColors.LIGHT_GREEN.getIndex());
        colors.put("Geografija", IndexedColors.LIGHT_GREEN.getIndex());
        colors.put("Ekonomika ir verslumas", IndexedColors.LIGHT_GREEN.getIndex());

        colors.put("Dailė", IndexedColors.TURQUOISE.getIndex());
        colors.put("Muzika", IndexedColors.TURQUOISE.getIndex());
        colors.put("Šokis", IndexedColors.TURQUOISE.getIndex());
        colors.put("Teatras", IndexedColors.TURQUOISE.getIndex());

        colors.put("Mityba", IndexedColors.LIGHT_ORANGE.getIndex());
        colors.put("Tekstilė", IndexedColors.LIGHT_ORANGE.getIndex());
        colors.put("Technologijos ir dizainas", IndexedColors.LIGHT_ORANGE.getIndex());

        colors.put("Užsienio kalba (vokiečių, B1 lygis)", IndexedColors.LIGHT_GREEN.getIndex());
        colors.put("Užsienio kalba (vokiečių, pradedantiesiems)", IndexedColors.LIGHT_GREEN.getIndex());
        colors.put("Užsienio kalba (prancūzų, B1 lygis)", IndexedColors.LIGHT_GREEN.getIndex());
        colors.put("Užsienio kalba (prancūzų, pradedantiesiems)", IndexedColors.LIGHT_GREEN.getIndex());
        colors.put("Užsienio kalba (rusų, B1 lygis)", IndexedColors.RED.getIndex());
        colors.put("Užsienio kalba (anglų) modulis", IndexedColors.LEMON_CHIFFON.getIndex());

        return colors;
    }

    private static void addLegend(Workbook workbook, Sheet sheet, int rowIndex) {
        CellStyle titleStyle = createBlueHeaderStyle(workbook);
        CellStyle selectedStyle = createNumberStyleWithFill(workbook, IndexedColors.LIGHT_GREEN.getIndex());
        CellStyle gradeThreeStyle = createNumberStyleWithFill(workbook, IndexedColors.LIGHT_YELLOW.getIndex());
        CellStyle gradeFourStyle = createNumberStyleWithFill(workbook, IndexedColors.PALE_BLUE.getIndex());

        Row title = sheet.createRow(rowIndex);
        createCell(title, 1, "Paaiškinimas", titleStyle);

        Row selected = sheet.createRow(rowIndex + 1);
        createCell(selected, 1, "Mokinių pasirinkimų lapas", selectedStyle);
        createCell(selected, 2, "Kiekvienas pasirinktas dalykas rodo III / IV klasės valandas", selectedStyle);

        Row gradeThree = sheet.createRow(rowIndex + 2);
        createCell(gradeThree, 1, "III klasės lapas", gradeThreeStyle);
        createCell(gradeThree, 2, "Rodo pasirinktų dalykų III klasės valandas", gradeThreeStyle);

        Row gradeFour = sheet.createRow(rowIndex + 3);
        createCell(gradeFour, 1, "IV klasės lapas", gradeFourStyle);
        createCell(gradeFour, 2, "Rodo pasirinktų dalykų IV klasės valandas", gradeFourStyle);
    }

    private static CellStyle createMetaHeaderStyle(Workbook workbook) {
        CellStyle style = baseBorderStyle(workbook);

        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.BLACK.getIndex());

        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    private static CellStyle createVerticalHeaderStyle(Workbook workbook, short color) {
        CellStyle style = baseBorderStyle(workbook);

        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.BLACK.getIndex());

        style.setFont(font);
        style.setFillForegroundColor(color);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setRotation((short) 90);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);

        return style;
    }

    private static CellStyle createFilterRowStyle(Workbook workbook, short color) {
        CellStyle style = baseBorderStyle(workbook);

        style.setFillForegroundColor(color);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    private static CellStyle createBlueHeaderStyle(Workbook workbook) {
        CellStyle style = baseBorderStyle(workbook);

        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());

        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    private static CellStyle createStudentStyle(Workbook workbook) {
        CellStyle style = baseBorderStyle(workbook);

        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(false);

        return style;
    }

    private static CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = baseBorderStyle(workbook);

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    private static CellStyle createNumberStyleWithFill(Workbook workbook, short color) {
        CellStyle style = createNumberStyle(workbook);

        style.setFillForegroundColor(color);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);

        return style;
    }

    private static CellStyle baseBorderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());

        return style;
    }

    private static void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value == null ? "" : value);
        cell.setCellStyle(style);
    }

    private static void createCell(Row row, int col, int value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private static int NumberOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String formatStudentNumber(int number) {
        return "a" + String.format("%02d", number);
    }

    private static void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);

            int currentWidth = sheet.getColumnWidth(i);
            int maxWidth = 9000;

            if (currentWidth > maxWidth) {
                sheet.setColumnWidth(i, maxWidth);
            }
        }
    }
}