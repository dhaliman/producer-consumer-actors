import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior, Terminated}

object App {

  private case class Start()

  private def guardian(): Behavior[Start] = Behaviors.setup[Start] { context =>
    val focQueue = context.spawn[FOCQueue.QueueMessage](FOCQueue(1), "foc-queue")
    val focConsumer = context.spawn(FOCConsumer(focQueue, 20), "foc-consumer")
    val focProducer = context.spawn(FOCProducer(focQueue, 20), "foc-producer")

    context.watch[FOCConsumer.ConsumerMessage](focConsumer)

    Behaviors.receiveMessage[Start] {
      case Start() =>
        focProducer ! FOCProducer.Ack
        focConsumer ! FOCConsumer.Consume
        Behaviors.same
    }.receiveSignal {
      case (context, Terminated(_)) =>
        context.stop(focProducer)
        context.stop(focQueue)
        Behaviors.stopped
    }
  }

  def main(args: Array[String]): Unit = {
    val system: ActorSystem[Start] = ActorSystem(guardian(), "app")

    system ! Start()
  }
}
