package ua.dscorp.poessence.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ua.dscorp.poessence.Application.APP_DATA_FOLDER;

public final class UtilClass {

    public static final String SNAPSHOTS_FOLDER = "snapshots/";
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");

    private UtilClass() {}

    public static List<String> getTableContentFileNames(ItemType itemType) {
        File currentDir = new File(APP_DATA_FOLDER, SNAPSHOTS_FOLDER);
        FilenameFilter filter = new TableContentFileFilter(itemType);
        String[] fileNames = currentDir.list(filter);

        List<String> matchingFiles = new ArrayList<>();
        if (fileNames != null) {
            matchingFiles.addAll(Arrays.asList(fileNames));
        }
        return matchingFiles;
    }

    public static String generateFileName(String itemName,  LocalDateTime now) throws IOException {
        Files.createDirectories(Paths.get(APP_DATA_FOLDER, SNAPSHOTS_FOLDER));
        return APP_DATA_FOLDER + "\\" + SNAPSHOTS_FOLDER + itemName + "_" + now.format(FORMATTER) + "_save.json";
    }

    public static LocalDateTime extractDateFromName(String fileName) {
        String date = fileName.split("_")[1];
        String time = fileName.split("_")[2];
        return LocalDateTime.parse(date + "_" + time, FORMATTER);
    }

    public static Integer parseOrElse(String text, int defaultValue) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
