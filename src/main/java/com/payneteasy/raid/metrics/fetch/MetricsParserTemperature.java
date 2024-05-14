package com.payneteasy.raid.metrics.fetch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class MetricsParserTemperature implements IMetricParser {

    private static final Logger LOG = LoggerFactory.getLogger( MetricsParserTemperature.class );

    public List<Metric> parseMetrics(String aText) {
        LOG.info("Parsing\n{}", aText);

        List<String> metricsLines  = metricsLines(aText);
        int          valuePosition = calcValuePosition(aText);

        List<Metric> metrics = createMetrics(metricsLines, valuePosition);
        logMetrics(metrics);

        return metrics;
    }

    private void logMetrics(List<Metric> metrics) {
        for (Metric metric : metrics) {
            LOG.info("{} = {}", metric.getName(), metric.getValue());
        }
    }

    private List<Metric> createMetrics(List<String> metricsLines, int valuePosition) {
        List<Metric> metrics = new ArrayList<>(metricsLines.size());
        for (String metricsLine : metricsLines) {
            metrics.add(Metric.builder()
                    .name(extractName(metricsLine, valuePosition))
                    .value(extractValue(metricsLine, valuePosition))
                    .build()
            );
        }
        return metrics;
    }

    private double extractValue(String metricsLine, int valuePosition) {
        return Double.parseDouble(metricsLine.substring(valuePosition));
    }

    private String extractName(String metricsLine, int valuePosition) {
        StringTokenizer st = new StringTokenizer(metricsLine.substring(0, valuePosition), " ()");
        StringBuilder sb = new StringBuilder();
        while (st.hasMoreTokens()) {
            sb.append("_");
            sb.append(st.nextToken());
        }
        return sb.substring(1);
    }

    private int calcValuePosition(String aText) {
        String line = aText.lines()
                .filter(it -> it.contains("Ctrl_Prop"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Cannot find line with Ctrl_Prop"));
        return line.indexOf("Value");
    }

    private List<String> metricsLines(String aText) {
        List<String> lines    = new ArrayList<>();
        List<String> original = aText.lines().collect(Collectors.toList());
        boolean      started  = false;
        for (String line : original) {
            if (line.contains("Ctrl_Prop")) {
                started = true;
                continue;
            }

            if (line.contains("------")) {
                continue;
            }

            if (started) {
                lines.add(line);
            }
        }
        return lines;
    }
}
