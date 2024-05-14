package com.payneteasy.raid.metrics;

import com.payneteasy.http.server.HttpServer;
import com.payneteasy.http.server.api.handler.IHttpRequestHandler;
import com.payneteasy.osprocess.api.IProcessService;
import com.payneteasy.osprocess.impl.ProcessServiceImpl;
import com.payneteasy.raid.metrics.fetch.FetchMetricsTask;
import com.payneteasy.raid.metrics.fetch.MetricsParserShowAll;
import com.payneteasy.raid.metrics.fetch.MetricsParserTemperature;
import com.payneteasy.raid.metrics.fetch.MetricsStore;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

public class RaidMetricsApplication {

    public static void main(String[] args) throws IOException {

        MetricsStore        temperatureMetrics    = new MetricsStore();
        MetricsStore        showAllMetrics    = new MetricsStore();
        IHttpRequestHandler handler         = new MetricsHandler(List.of(temperatureMetrics, showAllMetrics));
        ExecutorService     httpExecutor    = newFixedThreadPool(10);
        ExecutorService     metricsExecutor = newFixedThreadPool(2);
        ExecutorService     processExecutor = newSingleThreadExecutor();
        IProcessService     processService  = new ProcessServiceImpl(processExecutor);

        HttpServer server = new HttpServer(
                new InetSocketAddress(9093)
                , new HttpLoggerSlf4jImpl()
                , httpExecutor
                , handler
                , 10_000
        );

        metricsExecutor.execute(
                new FetchMetricsTask(
                        new MetricsParserTemperature()
                        , processService
                        , temperatureMetrics
                        , Duration.ofMinutes(1).toMillis()
                        , "/opt/MegaRAID/storcli/storcli64"
                        , List.of("/c0", "show", "temperature", "nolog")
                        , new File("/opt/MegaRAID/storcli")
                )
        );

        metricsExecutor.execute(
                new FetchMetricsTask(
                        new MetricsParserShowAll()
                        , processService
                        , showAllMetrics
                        , Duration.ofMinutes(1).toMillis()
                        , "/opt/MegaRAID/storcli/storcli64"
                        , List.of("/c0/v0", "show", "all", "nolog")
                        , new File("/opt/MegaRAID/storcli")
                )
        );

        getRuntime().addShutdownHook(new Thread(() -> {
            server.stop();
            httpExecutor.shutdown();
            processExecutor.shutdown();
            metricsExecutor.shutdown();
        }));

        server.acceptSocketAndWait();
    }
}
