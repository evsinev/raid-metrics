package com.payneteasy.raid.metrics;

import com.payneteasy.http.server.HttpServer;
import com.payneteasy.http.server.api.handler.IHttpRequestHandler;
import com.payneteasy.http.server.log.HttpLoggerSystemOut;
import com.payneteasy.osprocess.api.IProcessService;
import com.payneteasy.osprocess.impl.ProcessServiceImpl;
import com.payneteasy.raid.metrics.fetch.FetchMetricsTask;
import com.payneteasy.raid.metrics.fetch.MetricsStore;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

public class RaidMetricsApplication {

    public static void main(String[] args) throws IOException {

        MetricsStore        metricsStore    = new MetricsStore();
        IHttpRequestHandler handler         = new MetricsHandler(metricsStore);
        ExecutorService     httpExecutor    = newFixedThreadPool(10);
        ExecutorService     processExecutor = newSingleThreadExecutor();
        IProcessService     processService  = new ProcessServiceImpl(processExecutor);

        HttpServer server = new HttpServer(
                new InetSocketAddress(9093)
                , new HttpLoggerSlf4jImpl()
                , httpExecutor
                , handler
                , 10_000
        );

        FetchMetricsTask fetchMetricsTask = new FetchMetricsTask(
                processService
                , metricsStore
                , Duration.ofMinutes(1).toMillis()
                , "/opt/storecli/storcli64"
                , List.of("/c0", "show", "temperature")
                , new File("/opt/storecli")
        );

        Thread thread = new Thread(fetchMetricsTask);
        thread.start();

        getRuntime().addShutdownHook(new Thread(() -> {
            thread.interrupt();
            server.stop();
            httpExecutor.shutdown();
            processExecutor.shutdown();
        }));

        server.acceptSocketAndWait();
    }
}
