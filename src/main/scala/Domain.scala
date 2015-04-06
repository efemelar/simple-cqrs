package simpleCQRS

class InventoryItem (private var _id : UUID,
                     private var name: String)
  extends AggregateRoot[InventoryItem] {

  def this() = this(null, null)

  private var _activated = false

  private var _count = 0

  def apply = {
    case e: InventoryItemCreated => {
      _id = e.id
      _activated = true
    }
    case e: InventoryItemDeactivated => {
      _activated = false
    }
    case e:ItemsCheckedInToInventory => {
      _count += e.count
    }
    case e:ItemsRemovedFromInventory => {
      _count -= e.count
    }
    case e:InventoryItemRenamed => {
      name = e.newName
    }
    case e:Event => throw new IllegalArgumentException(s"Unknown event : $e")
  }

  def init() = {
    applyChange(InventoryItemCreated(id, name))
  }

  def changeName(newName: String) {
    require(!(newName == null || newName.isEmpty), "newName")
    applyChange(InventoryItemRenamed(_id, newName))
  }

  def remove(count: Int) {
    require(count > 0, "cant remove negative count from inventory")
    applyChange(ItemsRemovedFromInventory(_id, count))
  }

  def checkIn(count: Int) {
    require(count > 0, "must have a count greater than 0 to add to inventory")
    applyChange(ItemsCheckedInToInventory(_id, count))
  }

  def deactivate() {
    require(_activated, "already deactivated")
    applyChange(InventoryItemDeactivated(_id))
  }

  override def id = _id
}

abstract class AggregateRoot[T <: AggregateRoot[T]] { this: T =>

  private var changes: List[Event] = Nil
  private var version: Int = 0

  def id: UUID

  def apply: PartialFunction[Event, Unit]

  def uncommittedChanges = changes

  def markChangesAsCommitted() {
    changes = Nil
  }

  def loadFromHistory(history: Seq[Event]) =
    history.foldLeft(this)(_.applyChange(_, false))

  protected def applyChange(e: Event): T =
    applyChange(e, true)

  private def applyChange(e: Event, isNew: Boolean): T = {
    this.apply(e)
    if (isNew) changes :+= e
    this
  }
}

trait Repository[T <: AggregateRoot[T]] {
  def save(aggregate: AggregateRoot[T], expectedVersion: Int)
  def getById(id: UUID): T
}

import scala.reflect._

class EventStoreRepository[T <: AggregateRoot[T] : ClassTag](storage: EventStore)
  extends Repository[T] {

  def save(ar: AggregateRoot[T], expectedVersion: Int) {
    storage.saveEvents(ar.id, ar.uncommittedChanges, expectedVersion)
  }

  def getById(id: UUID) =
    newAggregate.loadFromHistory(storage.eventsForAggregate(id))

  private def newAggregate = classTag[T].runtimeClass.newInstance.asInstanceOf[T]
}

