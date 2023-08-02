import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.Behaviors

object FOCConsumer {

  trait ConsumerMessage

  final case object Consume extends ConsumerMessage

  case class Ack(element: Int) extends ConsumerMessage

  def apply(queueRef: ActorRef[FOCQueue.QueueMessage], maxElement: Int): Behaviors.Receive[ConsumerMessage] = Behaviors.receive[ConsumerMessage] {
    case (context, Consume) =>
      queueRef ! FOCQueue.TakeElement(context.self)
      context.log.info("Request sent to take element from queue.")
      Behaviors.same
    case (context, Ack(element)) =>
      context.log.info("Element received from queue {}.", element)
      if (maxElement == element)
        Behaviors.stopped[ConsumerMessage]
      else {
        context.self ! Consume
        Behaviors.same
      }
  }
}
