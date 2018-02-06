import akka.NotUsed
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source, SourceQueueWithComplete}

object WebSocket {

  private var browserConnections: List[TextMessage => Unit] = List()

  def listen(): Flow[Message, Message, NotUsed] = {

    val inbound: Sink[Message, Any] = Sink.foreach(_ => ())
    val outbound: Source[Message, SourceQueueWithComplete[Message]] = Source.queue[Message](16, OverflowStrategy.fail)

    Flow.fromSinkAndSourceMat(inbound, outbound)((_, outboundMat) => {
      browserConnections ::= outboundMat.offer
      NotUsed
    })
  }

  def sendText(text: String): Unit = {
    for (connection <- browserConnections) connection(TextMessage.Strict(text))
  }
}
