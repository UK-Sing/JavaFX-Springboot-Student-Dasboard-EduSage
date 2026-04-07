package com.edusage.service;

import com.edusage.model.Score;
import com.edusage.model.enums.RiskLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AnalyticsServiceTest {

    private AnalyticsService service;

    @BeforeEach
    void setUp() {
        service = new AnalyticsService();
    }

    private Score scoreOf(double pct) {
        Score s = new Score();
        s.setPercentage(pct);
        return s;
    }

    // ─── METRIC 1: Overall Average ───────────────────────────────────────

    @Test
    void overallAverage_emptyList_returnsZero() {
        assertEquals(0.0, service.computeOverallAverage(List.of()));
    }

    @Test
    void overallAverage_singleScore() {
        assertEquals(75.0, service.computeOverallAverage(List.of(scoreOf(75.0))));
    }

    @Test
    void overallAverage_multipleScores() {
        List<Score> scores = List.of(scoreOf(80.0), scoreOf(60.0), scoreOf(40.0));
        assertEquals(60.0, service.computeOverallAverage(scores), 0.001);
    }

    // ─── METRIC 2: Moving Average ─────────────────────────────────────────

    @Test
    void movingAverage_emptyList_returnsZero() {
        assertEquals(0.0, service.computeMovingAverage(List.of(), 5));
    }

    @Test
    void movingAverage_fewerThanNScores_usesAll() {
        List<Score> scores = List.of(scoreOf(60.0), scoreOf(80.0));
        assertEquals(70.0, service.computeMovingAverage(scores, 5), 0.001);
    }

    @Test
    void movingAverage_moreThanNScores_usesLastN() {
        List<Score> scores = List.of(scoreOf(10.0), scoreOf(20.0), scoreOf(80.0), scoreOf(60.0), scoreOf(40.0), scoreOf(100.0));
        // last 5: 20, 80, 60, 40, 100 → avg = 60
        assertEquals(60.0, service.computeMovingAverage(scores, 5), 0.001);
    }

    // ─── METRIC 3: Improvement Rate ──────────────────────────────────────

    @Test
    void improvementRate_fewerThanTwoScores_returnsZero() {
        assertEquals(0.0, service.computeImprovementRate(List.of(scoreOf(80.0))));
    }

    @Test
    void improvementRate_improving() {
        List<Score> scores = List.of(scoreOf(50.0), scoreOf(75.0));
        assertEquals(50.0, service.computeImprovementRate(scores), 0.001);
    }

    @Test
    void improvementRate_declining() {
        List<Score> scores = List.of(scoreOf(80.0), scoreOf(60.0));
        assertEquals(-25.0, service.computeImprovementRate(scores), 0.001);
    }

    @Test
    void improvementRate_previousZero_returnsZero() {
        List<Score> scores = List.of(scoreOf(0.0), scoreOf(50.0));
        assertEquals(0.0, service.computeImprovementRate(scores));
    }

    // ─── METRIC 5: Risk Level ─────────────────────────────────────────────

    @Test
    void classifyRisk_below40_returnsHighRisk() {
        assertEquals(RiskLevel.HIGH_RISK, service.classifyRisk(39.9));
    }

    @Test
    void classifyRisk_exactly40_returnsNeedsAttention() {
        assertEquals(RiskLevel.NEEDS_ATTENTION, service.classifyRisk(40.0));
    }

    @Test
    void classifyRisk_between40and60_returnsNeedsAttention() {
        assertEquals(RiskLevel.NEEDS_ATTENTION, service.classifyRisk(59.9));
    }

    @Test
    void classifyRisk_exactly60_returnsOnTrack() {
        assertEquals(RiskLevel.ON_TRACK, service.classifyRisk(60.0));
    }

    @Test
    void classifyRisk_above60_returnsOnTrack() {
        assertEquals(RiskLevel.ON_TRACK, service.classifyRisk(95.0));
    }

    // ─── METRIC 7a: Weak Subjects ─────────────────────────────────────────

    @Test
    void detectWeakSubjects_returnsBottom2() {
        Map<String, Double> avgs = new LinkedHashMap<>();
        avgs.put("Math", 80.0);
        avgs.put("Science", 55.0);
        avgs.put("English", 30.0);
        avgs.put("History", 45.0);
        List<String> weak = service.detectWeakSubjects(avgs);
        assertEquals(2, weak.size());
        assertEquals("English", weak.get(0));
        assertEquals("History", weak.get(1));
    }

    @Test
    void detectWeakSubjects_fewerThan2Subjects_returnsAll() {
        Map<String, Double> avgs = Map.of("Math", 70.0);
        List<String> weak = service.detectWeakSubjects(avgs);
        assertEquals(1, weak.size());
    }

    // ─── METRIC 7b: Decline Detection ────────────────────────────────────

    @Test
    void detectDecline_lastThreeDecreasing_returnsTrue() {
        List<Score> scores = List.of(scoreOf(80.0), scoreOf(70.0), scoreOf(60.0), scoreOf(50.0));
        assertTrue(service.detectDecline(scores, 3));
    }

    @Test
    void detectDecline_notStrictlyDecreasing_returnsFalse() {
        List<Score> scores = List.of(scoreOf(80.0), scoreOf(70.0), scoreOf(80.0), scoreOf(50.0));
        assertFalse(service.detectDecline(scores, 3));
    }

    @Test
    void detectDecline_fewerThanNScores_returnsFalse() {
        List<Score> scores = List.of(scoreOf(80.0), scoreOf(60.0));
        assertFalse(service.detectDecline(scores, 3));
    }

    @Test
    void detectDecline_equalConsecutiveScores_returnsFalse() {
        List<Score> scores = List.of(scoreOf(70.0), scoreOf(70.0), scoreOf(50.0));
        assertFalse(service.detectDecline(scores, 3));
    }
}
