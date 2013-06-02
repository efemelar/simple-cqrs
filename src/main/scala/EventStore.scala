package simpleCQRS


trait EventStore {
  def eventsForAggregate(id: UUID): Seq[Event]
  def saveEvents(id: UUID, events: Seq[Event], expectedVersion: Int)
}



class InMemEventStore(
  publish: Event => Unit) extends EventStore {

  import collection.mutable

  private case class EventDescriptor(
    id: UUID,
    eventData: Event,
    version: Int)

  private val current = mutable.Map[UUID, Seq[EventDescriptor]]()

  def saveEvents(aggregateId: UUID, events: Seq[Event], expectedVersion: Int) {
    val eventDescriptors =
      current.getOrElse(aggregateId, Seq.empty)

    if (eventDescriptors.nonEmpty &&
        eventDescriptors.last.version != expectedVersion
        && expectedVersion != -1) {
      throw new ConcurrencyException
    }

    events.zipWithIndex.foreach { case (event, i) =>
      val version = expectedVersion + i + 1
      current.update(
        aggregateId,
        eventDescriptors :+ EventDescriptor(aggregateId, event, version)
      )
      publish(event.withVersion(version))
    }
  }

  def eventsForAggregate(id: UUID) = {
    if (!current.contains(id)) throw new AggregateNotFoundException(id)
    current(id).map(_.eventData)
  }
}

class ConcurrencyException extends Exception("ConcurrencyException")

class AggregateNotFoundException(id: UUID)
  extends Exception("No aggregate with id "+id)