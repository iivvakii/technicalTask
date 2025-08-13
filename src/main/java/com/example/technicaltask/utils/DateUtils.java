package com.example.technicaltask.utils;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateUtils {
    public static long convertDateToUnixDate(String date) {
        if(date.contains("Posted on")){
            date = date.replace("Posted on", "").trim();
        } else {
            date = date.replace("Posted", "").trim();
        }

        if (date.equalsIgnoreCase("today")) {
            return LocalDate.now().atStartOfDay(ZoneOffset.UTC).toEpochSecond();
        }

        // Якщо "yesterday"
        if (date.equalsIgnoreCase("yesterday")) {
            return LocalDate.now().minusDays(1).atStartOfDay(ZoneOffset.UTC).toEpochSecond();
        }

        // Якщо "X days ago"
        if (date.matches("\\d+ days ago")) {
            int days = Integer.parseInt(date.split(" ")[0]);
            return LocalDate.now().minusDays(days).atStartOfDay(ZoneOffset.UTC).toEpochSecond();
        }

        // Якщо "X+ months ago" або "X months ago"
        if (date.matches("\\d+\\+? months ago")) {
            int months = Integer.parseInt(date.split("\\+?")[0].trim());
            return LocalDate.now().minusMonths(months).atStartOfDay(ZoneOffset.UTC).toEpochSecond();
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);
            LocalDate localDate = LocalDate.parse(date, formatter);
            return localDate.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
        } catch (Exception e) {
            System.err.println("Невідомий формат дати: " + date);
            return 0;
        }
    }
}
