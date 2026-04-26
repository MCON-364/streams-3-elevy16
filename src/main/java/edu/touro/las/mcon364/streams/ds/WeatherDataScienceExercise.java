package edu.touro.las.mcon364.streams.ds;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.*;
import java.util.stream.*;


public class WeatherDataScienceExercise {

    record WeatherRecord(
            String stationId,
            String city,
            String date,
            double temperatureC,
            int humidity,
            double precipitationMm
    ) {}

    public static void main(String[] args) throws Exception {
        List<String> rows = readCsvRows("noaa_weather_sample_200_rows.csv");

        List<WeatherRecord> cleaned = rows.stream()
                .skip(1) // skip header
                .map(WeatherDataScienceExercise::parseRow)
                .flatMap(Optional::stream)
                .filter(WeatherDataScienceExercise::isValid)
                .toList();

        System.out.println("Total raw rows (excluding header): " + (rows.size() - 1));
        System.out.println("Total cleaned rows: " + cleaned.size());

        // TODO 1:
        // Count how many valid weather records remain after cleaning.
        long validRecords = cleaned.stream()
                .count();

        // TODO 2:
        // Compute the average temperature across all valid rows.
        double averageTemp = cleaned.stream()
                .mapToDouble(n -> n.temperatureC())
                .average()
                .orElse(0.0);

        // TODO 3:
        // Find the city with the highest average temperature.
        var avgTempByCity = cleaned.stream()
                .collect(Collectors.groupingBy(c -> c.city(),
                        Collectors.averagingDouble(c -> c.temperatureC())
                ));

        String hottestCity = avgTempByCity.entrySet().stream()
                .max(Comparator.comparingDouble(e -> e.getValue()))
                .get()
                .getKey();

        // TODO 4:
        // Group records by city.
        var groupedByCity = cleaned.stream()
                .collect(Collectors.groupingBy(c -> c.city()));

        // TODO 6:
        // Partition rows into freezing days (temperature <= 0)
        // and non-freezing days (temperature > 0).
        var partitionedByFreeze = cleaned.stream()
                .collect(Collectors.partitioningBy(c -> c.temperatureC() <= 0));

        // TODO 7:
        // Create a Set<String> of all distinct cities.
        Set<String> allDistinctCities = cleaned.stream()
                .map(c -> c.city())
                .collect(Collectors.toSet());

        // TODO 8:
        // Find the wettest single day.
        var wettest = cleaned.stream()
                .max(Comparator.comparingDouble(e -> e.precipitationMm()));

        // TODO 9:
        // Create a Map<String, Double> from city to average humidity.
        var cityToHumidity = cleaned.stream()
                .collect(Collectors.groupingBy(c -> c.city(),
                        Collectors.averagingInt(c -> c.humidity())));

        // TODO 10:
        // Produce a list of formatted strings like:
        // "Miami on 2025-01-02: 25.1C, humidity 82%"
        var formattedRecords = cleaned.stream()
                .map(c -> String.format("%s on %s: %.1fC, humidity %d%%",
                                c.city(),
                                c.date(),
                                c.temperatureC(),
                                c.humidity()))
                .toList();

        // TODO 11 (optional):
        // Build a Map<String, CityWeatherSummary> for all cities.

        // Put your code below these comments or refactor into helper methods.
    }

    static Optional<WeatherRecord> parseRow(String row) {
        // TODO:
        // 1. Split the row by commas
        String[] parts =  row.split(",");

        // 2. Reject malformed rows
        if (parts.length < 6) return Optional.empty();

        // 3. Reject rows with missing temperature
        if (parts[3].isBlank()) return Optional.empty();

        // 4. Parse numeric values safely
        try {
            double temperatureC = Double.parseDouble(parts[3].trim());
            int humidity = Integer.parseInt(parts[4].trim());
            double precipitationMm = Double.parseDouble(parts[5].trim());


        // 5. Return Optional.empty() if parsing fails
        return Optional.of(new WeatherRecord(
                parts[0].trim(),  // stationId
                parts[1].trim(),  // city
                parts[2].trim(),  // date
                temperatureC,
                humidity,
                precipitationMm
        ));

        } catch (NumberFormatException e) {
            return Optional.empty();
        }

    }

    static boolean isValid(WeatherRecord r) {
        // TODO:
        // Keep only rows where:
        // - temperature is between -60 and 60
        return r.temperatureC() >= -60 && r.temperatureC() <= 60

        // - humidity is between 0 and 100
        && r.humidity() >= 0 && r.humidity() <= 100

        // - precipitation is >= 0
        && r.precipitationMm() >= 0;
    }

    record CityWeatherSummary(
            String city,
            long dayCount,
            double avgTemp,
            double avgPrecipitation,
            double maxTemp
    ) {}

    private static List<String> readCsvRows(String fileName) throws IOException {
        InputStream in = WeatherDataScienceExercise.class.getResourceAsStream(fileName);
        if (in == null) {
            throw new NoSuchFileException("Classpath resource not found: " + fileName);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            return reader.lines().toList();
        }
    }
}
