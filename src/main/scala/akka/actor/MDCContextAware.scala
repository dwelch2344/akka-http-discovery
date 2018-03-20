package akka.actor

import akka.util.Timeout
import com.lightbend.akka.sample.extension.{ContextExtensionImpl, Contextual}
import org.slf4j.MDC

import scala.concurrent.Future

// TODO move this to receive pipeline (and condense into Contextual trait)
// See: https://doc.akka.io/docs/akka/2.4-M3/contrib/receive-pipeline.html
trait MDCContextAware extends Actor with Contextual {
  import MDCContextAware._

  // This is why this needs to be in package akka.actor
  override protected[akka] def aroundReceive(receive: Actor.Receive, msg: Any): Unit = {
    val orig = MDC.getCopyOfContextMap
    try {
      msg match {
        case MdcMsg(mdc, origMsg) =>
          preProcess(mdc)
          super.aroundReceive(receive, origMsg)
        case _ =>
          super.aroundReceive(receive, msg)
      }
    } finally {
      postProcess(orig)
    }
  }

  private def preProcess(mdc: java.util.Map[String,String]): Unit ={
    if (mdc != null)
      MDC.setContextMap(mdc)
    else
      MDC.clear()
  }

  private def postProcess(orig: java.util.Map[String,String]): Unit ={
    if (orig != null)
      MDC.setContextMap(orig)
    else
      MDC.clear()
  }


}

object MDCContextAware {
  private case class MdcMsg(mdc: java.util.Map[String,String], msg: Any)

  object Implicits {

    /**
      * Add two new methods that allow MDC info to be passed to MDCContextAware actors.
      *
      * Do NOT use these methods to send to actors that are not MDCContextAware.
      */
    implicit class ContextLocalAwareActorRef(val ref: ActorRef) extends AnyVal {

      import akka.pattern.ask

      /**
        * Send a message to an actor that is MDCContextAware - it will propagate
        * the current MDC values.
        */
      def !>(msg: Any): Unit =
        ref ! MdcMsg(MDC.getCopyOfContextMap, msg)

      /**
        * "Ask" an actor that is MDCContextAware for something - it will propagate
        * the current MDC values
        */
      def ?>(msg: Any)(implicit timeout: Timeout): Future[Any] =
        ref ? MdcMsg(MDC.getCopyOfContextMap, msg)
    }
  }
}