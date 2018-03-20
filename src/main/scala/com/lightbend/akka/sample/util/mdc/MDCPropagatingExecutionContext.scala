package com.lightbend.akka.sample.util.mdc

import java.util.concurrent.TimeUnit

import akka.dispatch._
import brave.Span
import brave.propagation.TraceContextOrSamplingFlags
import com.lightbend.akka.sample.extension.ContextExtensionImpl
import com.typesafe.config.Config
import org.slf4j.MDC

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{Duration, FiniteDuration}


/**
  * Setup your application.conf with this...
  * <code>
    akka {
      actor {
        default-dispatcher = {
          type = "com.lightbend.akka.sample.util.mdc.MDCPropagatingDispatcherConfigurator"
        }
      }
    }
  * </code>
  */
// see http://code.hootsuite.com/logging-contextual-info-in-an-asynchronous-scala-application/
// also for Akka dispatching: http://yanns.github.io/blog/2014/05/04/slf4j-mapped-diagnostic-context-mdc-with-play-framework/
trait MDCPropagatingExecutionContext extends ExecutionContext {
  // name the self-type "self" so we can refer to it inside the nested class
  self =>

  override def prepare(): ExecutionContext = new ExecutionContext {
    // Save the call-site MDC state
    val context = MDC.getCopyOfContextMap
    val tracer = ContextExtensionImpl.tracer
    val currentSpan = Option[Span](tracer.currentSpan())

    def execute(r: Runnable): Unit = self.execute( () => doMdcRun(r) )

    def doMdcRun(r: Runnable) = {
      // Save the existing execution-site MDC state
      val oldContext = MDC.getCopyOfContextMap
      try {
        // Set the call-site MDC state into the execution-site MDC
        if (context != null)
          MDC.setContextMap(context)
        else
          MDC.clear()

//        r.run()
        doExtensionRun(r)
      } finally {
        // Restore the existing execution-site MDC state
        if (oldContext != null)
          MDC.setContextMap(oldContext)
        else
          MDC.clear()
      }
    }

    def doExtensionRun(r: Runnable) = {
      // resume the span if present
      val oldSpan = Option(tracer.currentSpan())
      currentSpan match {
        case Some(span) => tracer.nextSpan( TraceContextOrSamplingFlags.create(span.context()) )
        case None => ; // TODO should we clear the span here?
      }
      try {
        r.run()
      }finally {
        oldSpan match {
          case Some(span) => tracer.nextSpan( TraceContextOrSamplingFlags.create(span.context()) )
          case None => ; // TODO should we clear the span here?
        }
      }
    }

    def reportFailure(t: Throwable): Unit = self.reportFailure(t)
  }
}

object MDCPropagatingExecutionContext {
  object Implicits {
    // Convenience wrapper around the Scala global ExecutionContext so you can just do:
    // import MDCPropagatingExecutionContext.Implicits.global
    implicit lazy val global = MDCPropagatingExecutionContextWrapper(ExecutionContext.Implicits.global)
  }
}

/**
  * Wrapper around an existing ExecutionContext that makes it propagate MDC information.
  */
class MDCPropagatingExecutionContextWrapper(wrapped: ExecutionContext)
  extends ExecutionContext with MDCPropagatingExecutionContext {

  override def execute(r: Runnable): Unit = wrapped.execute(r)

  override def reportFailure(t: Throwable): Unit = wrapped.reportFailure(t)
}

object MDCPropagatingExecutionContextWrapper {
  def apply(wrapped: ExecutionContext): MDCPropagatingExecutionContextWrapper = {
    new MDCPropagatingExecutionContextWrapper(wrapped)
  }
}


// code adapted from second link

class MDCPropagatingDispatcher(_configurator: MessageDispatcherConfigurator,
                               id: String,
                               throughput: Int,
                               throughputDeadlineTime: Duration,
                               executorServiceFactoryProvider: ExecutorServiceFactoryProvider,
                               shutdownTimeout: FiniteDuration)
  extends Dispatcher(_configurator, id, throughput, throughputDeadlineTime, executorServiceFactoryProvider, shutdownTimeout )
    with MDCPropagatingExecutionContext { self =>

}

class MDCPropagatingDispatcherConfigurator(config: Config, prerequisites: DispatcherPrerequisites)
  extends MessageDispatcherConfigurator(config, prerequisites) {

  private val instance = new MDCPropagatingDispatcher(
    this,
    config.getString("id"),
    config.getInt("throughput"),
    FiniteDuration(config.getDuration("throughput-deadline-time", TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS),
    configureExecutor(),
    FiniteDuration(config.getDuration("shutdown-timeout", TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS))

  override def dispatcher(): MessageDispatcher = instance
}
