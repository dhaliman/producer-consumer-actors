import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object FOCProducer {

  trait ProducerMessage

  case class AppendToQueue(element: Int) extends ProducerMessage

  case object Ack extends ProducerMessage

  def apply(queue: ActorRef[FOCQueue.QueueMessage], maxElement: Int): Behavior[ProducerMessage] = Behaviors.setup[ProducerMessage] {
    var nextNumber = 0

    return Behaviors.receive[ProducerMessage] {
      case (context, AppendToQueue(element: Int)) =>
        queue ! FOCQueue.AppendElement(element, context.self)
        context.log.info("Element {} has been sent.", element)
        nextNumber = nextNumber + 1
        if (nextNumber > maxElement)
          Behaviors.stopped
        else
          Behaviors.same
      case (context, Ack) =>
        context.log.info("Acknowledgement received.")
        context.self ! AppendToQueue(nextNumber)
        Behaviors.same
    }
  }
}
