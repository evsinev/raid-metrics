package com.payneteasy.raid.metrics;

import com.payneteasy.http.server.HttpServer;
import com.payneteasy.http.server.api.handler.IHttpRequestHandler;
import com.payneteasy.osprocess.api.IProcessService;
import com.payneteasy.osprocess.impl.ProcessServiceImpl;
import com.payneteasy.raid.metrics.fetch.FetchMetricsTask;
import com.payneteasy.raid.metrics.fetch.MetricsParserShowAll;
import com.payneteasy.raid.metrics.fetch.MetricsParserTemperature;
import com.payneteasy.raid.metrics.fetch.MetricsStore;
import com.payneteasy.raid.metrics.param.Param;

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

        Param.logValues();

        MetricsStore        temperatureMetrics = new MetricsStore();
        MetricsStore        showAllMetrics     = new MetricsStore();
        IHttpRequestHandler handler            = new MetricsHandler(List.of(temperatureMetrics, showAllMetrics));
        ExecutorService     httpExecutor       = newFixedThreadPool(Param.HTTP_THREADS_COUNT.getIntValue());
        ExecutorService     metricsExecutor    = newFixedThreadPool(Param.METRICS_THREADS_COUNT.getIntValue());
        ExecutorService     processExecutor    = newSingleThreadExecutor();
        IProcessService     processService     = new ProcessServiceImpl(processExecutor);

        HttpServer server = new HttpServer(
                new InetSocketAddress(Param.METRICS_PORT.getIntValue())
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
                        , Param.STORCLI64_BIN_PATH.getValue()
                        , List.of(Param.SHOW_TEMPERATURE_DEVICE.getValue(), "show", "temperature", "nolog")
                        , new File(Param.STORCLI64_WORKING_DIR.getValue())
                        , "raid_show_temperature_exit_code"
                )
        );

        metricsExecutor.execute(
                new FetchMetricsTask(
                        new MetricsParserShowAll()
                        , processService
                        , showAllMetrics
                        , Duration.ofMinutes(1).toMillis()
                        , Param.STORCLI64_BIN_PATH.getValue()
                        , List.of(Param.SHOW_ALL_DEVICE.getValue(), "show", "all", "nolog")
                        , new File(Param.STORCLI64_WORKING_DIR.getValue())
                        , "raid_show_all_exit_code"
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
