import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

import scala.collection.mutable

object FOCQueue {

  trait QueueMessage

  final case class AppendElement(element: Int, producerRef: ActorRef[FOCProducer.ProducerMessage]) extends QueueMessage

  final case class TakeElement(consumerRef: ActorRef[FOCConsumer.ConsumerMessage]) extends QueueMessage

  def apply(maxQueueSize: Int): Behavior[QueueMessage] = Behaviors.setup[QueueMessage] {
    val queue = mutable.Queue[Int]()
    var consumerWaitingAck: Option[ActorRef[FOCConsumer.ConsumerMessage]] = None
    var producerWaitingAck: Option[ActorRef[FOCProducer.ProducerMessage]] = None

    return Behaviors.receive[QueueMessage] {
      /**
       * On receiving AppendElement message, FOCQueue actor does the following:
       * - Appends new element to the queue.
       * - Sends an ACK message to FOCProducer if the queue size is less than max size.
       * - Caches FOCProducer ActorRef if queue size is full to send ACK message when queue is not full.
       * - If there is a cached FOCConsumer, sends itself TakeElement message with cached ConsumerFOC ActorRef.
       */
      case (context, AppendElement(element, producerRef)) =>
        context.log.info("Element {} received from Producer", element)
        queue.enqueue(element)
        if (queue.size < maxQueueSize) {
          producerRef ! FOCProducer.Ack
          context.log.info("Acknowledgement sent to Producer")
        } else {
          producerWaitingAck = Some(producerRef)
          context.log.info("Queue is full, making Producer wait.")
        }

        if (consumerWaitingAck.nonEmpty) {
          context.self ! TakeElement(consumerWaitingAck.get)
          consumerWaitingAck = None
        }

        Behaviors.same

      /**
       * On receiving TakeElement message, FOCQueue actor does the following:
       * - Dequeues an element and sends it to FOCConsumer if queue is not empty.
       * - Caches FOCConsumer ActorRef if queue is empty to send an element when there are elements to send.
       * - If there is a cached FOCProducer, sends an ACK to it because now the queue has space in it.
       */
      case (context, TakeElement(consumerRef)) =>
        if (queue.nonEmpty) {
          val element = queue.dequeue()
          consumerRef ! FOCConsumer.Ack(element)
          context.log.info("Element {} sent to Consumer.", element)
        } else {
          consumerWaitingAck = Some(consumerRef)
          context.log.info("Queue is empty, making Consumer wait.")
        }

        if (producerWaitingAck.nonEmpty) {
          producerWaitingAck.get ! FOCProducer.Ack
          context.log.info("Acknowledgement sent to Producer")
          producerWaitingAck = None
        }
        Behaviors.same
    }
  }
}
