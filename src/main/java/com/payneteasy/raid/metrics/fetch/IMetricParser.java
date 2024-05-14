package com.payneteasy.raid.metrics.fetch;

import java.util.List;

public interface IMetricParser {

    List<Metric> parseMetrics(String aText);

}
