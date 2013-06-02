package object simpleCQRS {
  type UUID = java.util.UUID

  def randomUUID = java.util.UUID.randomUUID

  trait CommandSender {
    def send[C <: Command](cmd: C)
  }

  trait EventPublisher {
    def publish[E <: Event](evt: E)
  }

}