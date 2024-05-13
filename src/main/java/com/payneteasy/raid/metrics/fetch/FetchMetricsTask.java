package com.payneteasy.raid.metrics.fetch;

import com.payneteasy.osprocess.api.IProcessService;
import com.payneteasy.osprocess.api.ProcessDescriptor;
import com.payneteasy.osprocess.api.ProcessException;
import com.payneteasy.osprocess.api.ProcessRunResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

import static java.lang.Thread.currentThread;
import static java.util.Collections.emptyList;

public class FetchMetricsTask implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(FetchMetricsTask.class );

    private final MetricsParser parser = new MetricsParser();

    private final IProcessService processService;
    private final MetricsStore store;
    private final long         sleepMs;
    private final String       program;
    private final List<String> args;
    private final File         workingDir;

    public FetchMetricsTask(IProcessService processService, MetricsStore store, long sleepMs, String program, List<String> args, File workingDir) {
        this.processService = processService;
        this.store          = store;
        this.sleepMs        = sleepMs;
        this.program        = program;
        this.args           = args;
        this.workingDir     = workingDir;
    }

    @Override
    public void run() {
        while (!currentThread().isInterrupted()) {
            try {
                List<Metric> metrics = fetchMetrics();
                store.updateMetrics(metrics);
            } catch (Exception e) {
                LOG.error("Error while fetching metrics", e);
            }

            try {
                LOG.debug("Sleeping {}ms ...\n", sleepMs);
                Thread.sleep(sleepMs);
            } catch (InterruptedException e) {
                LOG.error("Interrupted sleep. Exited.", e);
                return;
            }
        }
    }

    private List<Metric> fetchMetrics() throws ProcessException {
        ProcessRunResult result = processService.runProcess(new ProcessDescriptor(
                program
                , args
                , emptyList()
                , workingDir
        ));

        if (result.getExitCode() != 0) {
            LOG.error("Exit code is not 0 but was {}", result.getExitCode());
            return emptyList();
        }

        return parser.parseMetrics(result.getOutput());
    }

}
