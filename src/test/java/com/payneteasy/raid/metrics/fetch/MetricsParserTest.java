package com.payneteasy.raid.metrics.fetch;


import org.junit.Test;

import java.util.List;

public class MetricsParserTest {

    private final String OUTPUT_1 = """
        CLI Version = 007.2309.0000.0000 Sep 16, 2022
        Operating system = Linux 5.14.0-362.24.2.el9_3.x86_64
        Controller = 0
        Status = Success
        Description = None
            
        Controller Properties :
        =====================
            
        --------------------------------------
        Ctrl_Prop                       Value\s
        --------------------------------------
        ROC temperature(Degree Celsius) 51   \s
        --------------------------------------
        """;

    @Test
    public void test() {
        MetricsParser parser = new MetricsParser();
        List<Metric>  metrics = parser.parseMetrics(OUTPUT_1);
        System.out.println("metrics = " + metrics);
    }
}