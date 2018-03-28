package com.timgroup.smileykt

import com.codahale.metrics.JvmAttributeGaugeSet
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.graphite.Graphite
import com.codahale.metrics.graphite.GraphiteReporter
import com.codahale.metrics.jvm.FileDescriptorRatioGauge
import com.codahale.metrics.jvm.GarbageCollectorMetricSet
import com.codahale.metrics.jvm.MemoryUsageGaugeSet
import com.codahale.metrics.jvm.ThreadStatesGaugeSet
import com.google.common.util.concurrent.AbstractIdleService
import com.google.common.util.concurrent.Service
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.management.ManagementFactory
import java.net.InetSocketAddress
import java.net.URI
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit

data class Metrics(val registry: MetricRegistry, val reporterService: Service)

fun createMetrics(config: Properties): Metrics {
    val registry = defaultMetricsRegistry()
    return Metrics(registry, metricsReporterService(config, registry))
}

fun defaultMetricsRegistry(): MetricRegistry = MetricRegistry().apply {
    register("jvm", JvmAttributeGaugeSet())
    register("jvm.fd_usage", FileDescriptorRatioGauge())
    register("jvm.gc", GarbageCollectorMetricSet())
    register("jvm.memory", MemoryUsageGaugeSet(
            ManagementFactory.getMemoryMXBean(),
            ManagementFactory.getMemoryPoolMXBeans().filter { !it.name.contains("'") }
    ))
    register("jvm.thread-states", ThreadStatesGaugeSet())
}

fun metricsReporterService(config: Properties, registry: MetricRegistry): Service {
    val enabled = config.getStringValue("graphite.enabled").toBoolean()
    if (!enabled) return NoMetrics()
    val host = config.getStringValue("graphite.host")
    val port = config.getStringValue("graphite.port").toInt()
    val prefix = config.getStringValue("graphite.prefix")
    return GraphiteReporterService(URI("graphite://$host:$port"), prefix, registry)
}

class NoMetrics : AbstractIdleService() {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    override fun startUp() {
        logger.info("Metrics are not enabled in this instance")
    }

    override fun shutDown() {
    }
}

class GraphiteReporterService(
        private val serverUri: URI,
        private val prefix: String,
        registry: MetricRegistry,
        private val reportingPeriod: Duration = Duration.ofSeconds(10)
) : AbstractIdleService() {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val reporter = GraphiteReporter.forRegistry(registry)
            .prefixedWith(prefix)
            .build(Graphite(InetSocketAddress(serverUri.host, serverUri.port)))

    override fun startUp() {
        logger.info("Sending metrics to $serverUri at $prefix every $reportingPeriod")
        reporter.start(reportingPeriod.toMillis(), TimeUnit.MILLISECONDS)
    }

    override fun shutDown() {
        reporter.stop()
    }
}
