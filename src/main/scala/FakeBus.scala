package simpleCQRS

import reflect._


class FakeBus extends CommandSender with  EventPublisher {
  var routes = Map[Class[_], Seq[Message => Unit]]()

  def registerHandler[M <: Message : ClassTag](handler: M => Unit) {
    val oldHandlers = routes.getOrElse(cls[M], Nil)
    routes = routes.updated(cls, oldHandlers :+ handler.asInstanceOf[Message => Unit])
  }

  def send[C <: Command](cmd: C) {
    val handlers = routes.getOrElse(cmd.getClass, throw new NoHandlerException)
    if (handlers.length > 1) throw new MultipleHandlersException
    handlers.head(cmd)
  }

  def publish[E <: Event](evt: E) {
    val handlers = routes.getOrElse(evt.getClass, Nil)
    handlers.foreach(_(evt))
  }

  private def cls[T : ClassTag] = classTag[T].runtimeClass
}

class NoHandlerException extends Exception("no handler registered")
class MultipleHandlersException extends Exception("can not send to more than one handler")