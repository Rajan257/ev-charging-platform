package com.evroaminghub.analytics.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

/**
 * Analytics service.
 * In production: queries ClickHouse for time-series data.
 * Here: returns realistic sample data with proper structure.
 */
@Service
@Slf4j
public class AnalyticsService {

    public Map<String, Object> getUtilization(String stationId, String from, String to, String groupBy) {
        List<Map<String, Object>> series = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            series.add(Map.of(
                "date",         date.toString(),
                "totalSessions", 18 + new Random(i).nextInt(15),
                "totalEnergyKwh", 280.5 + new Random(i).nextInt(100),
                "avgSessionMinutes", 42 + new Random(i).nextInt(20),
                "utilizationPct", 60 + new Random(i).nextInt(30)
            ));
        }
        return Map.of("stationId", stationId != null ? stationId : "ALL",
                      "period", "last7days", "groupBy", groupBy, "data", series);
    }

    public Map<String, Object> getRevenue(String from, String to, String groupBy) {
        List<Map<String, Object>> series = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            double rev = 12000 + new Random(i).nextInt(8000);
            series.add(Map.of(
                "date",         date.toString(),
                "grossRevenue",  rev,
                "cgst",          rev * 0.09,
                "sgst",          rev * 0.09,
                "netRevenue",    rev * 0.82,
                "sessions",      18 + new Random(i).nextInt(15)
            ));
        }
        return Map.of("period", "last7days", "currency", "INR", "data", series);
    }

    public Map<String, Object> getPeakHours(String stationId, String from, String to) {
        // 24-hour demand heatmap
        List<Map<String, Object>> hourly = new ArrayList<>();
        int[] pattern = {1,1,1,1,2,5,8,12,10,9,11,13,14,13,12,14,16,18,15,12,10,7,4,2};
        for (int h = 0; h < 24; h++) {
            hourly.add(Map.of("hour", h, "avgSessions", pattern[h], "avgEnergyKwh", pattern[h] * 18.5));
        }
        return Map.of("stationId", stationId != null ? stationId : "ALL",
                      "peakHour", 17, "peakSessions", 18, "data", hourly);
    }

    public Map<String, Object> getTopStations(String sortBy, int limit) {
        List<Map<String, Object>> stations = List.of(
            Map.of("stationId","s001","name","Tata Power - CP","sessions",312,"revenue",48200.0,"energyKwh",3240.0),
            Map.of("stationId","s003","name","Ather Grid - Kora","sessions",298,"revenue",43100.0,"energyKwh",2980.0),
            Map.of("stationId","s005","name","BPCL - Bandra","sessions",276,"revenue",40200.0,"energyKwh",2760.0)
        );
        return Map.of("sortBy", sortBy, "limit", limit, "stations", stations);
    }

    public Map<String, Object> getDemandForecast(String stationId) {
        List<Map<String, Object>> forecast = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            LocalDate date = LocalDate.now().plusDays(i);
            forecast.add(Map.of(
                "date",              date.toString(),
                "predictedSessions", 20 + new Random(i * 13L).nextInt(12),
                "predictedEnergyKwh",310 + new Random(i * 7L).nextInt(80),
                "confidence",        0.78 + (new Random(i).nextDouble() * 0.15)
            ));
        }
        return Map.of("stationId", stationId != null ? stationId : "ALL",
                      "forecastDays", 7, "model", "LinearRegression-v1",
                      "generatedAt", Instant.now().toString(), "forecast", forecast);
    }

    public Map<String, Object> getPlatformSummary() {
        return Map.of(
            "totalStations",         127,
            "activeStations",        119,
            "totalSessionsToday",    342,
            "totalEnergyTodayKwh",   4820.5,
            "totalRevenueToday",     67487.0,
            "activeSessions",        23,
            "registeredUsers",       4832,
            "carbonSavedKgToday",    3953.0,
            "avgSessionDurationMin", 43.2,
            "platformUptimePct",     99.8
        );
    }
}
