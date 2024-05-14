package com.payneteasy.raid.metrics.fetch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class MetricsParserShowAll implements IMetricParser {

    private static final Logger LOG = LoggerFactory.getLogger(MetricsParserShowAll.class);

    public List<Metric> parseMetrics(String aText) {
        LOG.info("Parsing\n{}", aText);

        List<Metric> metrics = new ArrayList<>();
        metrics.addAll(parseMetrics("dg_vd", "drive", extractTableLines(aText, "DG/VD"), 2));
        metrics.addAll(parseMetrics("eid_slt", "slot", extractTableLines(aText, "EID:Slt"), 2));
        logMetrics(metrics);

        return metrics;
    }

    private void logMetrics(List<Metric> metrics) {
        for (Metric metric : metrics) {
            LOG.info("{} = {}", metric.getName(), metric.getValue());
        }
    }

    private List<Metric> parseMetrics(String aPrefix, String aName, List<String> aLines, int aPosition) {
        List<Metric> metrics = new ArrayList<>();
        for (String line : aLines) {
            List<String> tokens = tokens(line, " \t");
            metrics.add(toMetric(aPrefix, aName, tokens.get(0), tokens.get(aPosition)));
        }
        return metrics;
    }

    private Metric toMetric(String aPrefix, String aMetricName, String aName, String aValue) {
        String name = "%s{%s=\"%s\",raid_state=\"%s\"}".formatted(aPrefix, aMetricName, fixName(aName), aValue);
        return new Metric(name, 1.0);
    }

    private String fixName(String aName) {
        return aName.replace("/", "_").replace(":", "_");
    }

    private List<String> tokens(String line, String aSeparator) {
        List<String>    tokens = new ArrayList<>();
        StringTokenizer st     = new StringTokenizer(line, aSeparator);
        while (st.hasMoreTokens()) {
            tokens.add(st.nextToken());
        }
        return tokens;
    }

    enum TableState {
        FINDING,
        FOUND,
        FIRST_SEPARATOR,
        SECOND_SEPARATOR

    }

    List<String> extractTableLines(String aText, String aMatch) {
        List<String> lines    = new ArrayList<>();
        List<String> original = aText.lines().collect(Collectors.toList());
        TableState   state    = TableState.FINDING;
        for (String line : original) {
            switch (state) {
                case FINDING -> {
                    if (line.startsWith(aMatch)) {
                        state = TableState.FOUND;
                    }
                }

                case FOUND -> {
                    if (line.startsWith("-----")) {
                        state = TableState.FIRST_SEPARATOR;
                    }
                }

                case FIRST_SEPARATOR -> {
                    if (line.startsWith("-----")) {
                        state = TableState.SECOND_SEPARATOR;
                    } else {
                        lines.add(line);
                    }
                }

                case SECOND_SEPARATOR -> {
                    return lines;
                }
            }
        }
        throw new IllegalStateException("Illegal state for matching '" + aMatch + "'. State is " + state);
    }

}
