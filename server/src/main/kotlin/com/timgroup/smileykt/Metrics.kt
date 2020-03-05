package com.timgroup.smileykt

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.graphite.Graphite
import com.codahale.metrics.graphite.GraphiteReporter
import com.codahale.metrics.jvm.FileDescriptorRatioGauge
import com.codahale.metrics.jvm.GarbageCollectorMetricSet
import com.codahale.metrics.jvm.JvmAttributeGaugeSet
import com.codahale.metrics.jvm.MemoryUsageGaugeSet
import com.codahale.metrics.jvm.ThreadStatesGaugeSet
import com.google.common.util.concurrent.AbstractIdleService
import com.google.common.util.concurrent.Service
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.lang.management.ManagementFactory
import java.net.InetSocketAddress
import java.net.URI
import java.time.Duration
import java.util.Properties
import java.util.concurrent.TimeUnit
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.StreamingOutput
import kotlin.math.roundToLong
import kotlin.text.Charsets.UTF_8

data class Metrics(val registry: MetricRegistry, val reporterService: Service)

fun createMetrics(config: Properties) = defaultMetricsRegistry().let { Metrics(it, metricsReporterService(config, it)) }

fun defaultMetricsRegistry() = MetricRegistry().apply {
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
    val interval = Duration.ofSeconds(config.getStringValue("graphite.period").toLong())
    return GraphiteReporterService(URI("graphite://$host:$port"), prefix, registry, interval)
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

@Path("/info/metrics")
class MetricsResource(private val registry: MetricRegistry) {
    @GET
    @Produces("text/plain; charset=UTF-8")
    fun show() = StreamingOutput { out ->
        PrintWriter(OutputStreamWriter(out, UTF_8)).use { output ->
            registry.gauges.forEach { name, gauge ->
                output.println("gauge\t$name\t${gauge.value}")
            }
            registry.counters.forEach { name, counter ->
                output.println("counter\t$name\t${counter.count}")
            }
            registry.histograms.forEach { name, histogram ->
                output.println("histogram\t$name\t${histogram.count}\t${histogram.snapshot.values}")
            }
            registry.meters.forEach { name, metered ->
                output.println("metered\t$name\t${metered.count}\t1m=${metered.oneMinuteRate}")
            }
            registry.timers.forEach { name, timer ->
                output.println("timer\t$name\t${timer.count}\t1m=${timer.oneMinuteRate}\tmean=${Duration.ofNanos(timer.snapshot.mean.roundToLong())}")
            }
        }
    }
}
