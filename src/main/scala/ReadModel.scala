package simpleCQRS

case class InventoryItemDetailsDto(
  id: UUID,
  name: String,
  currentCount: Int,
  version: Int)

case class InventoryItemDto(
  id: UUID,
  name: String)

trait ReadModelFacade {
  def inventoryItems: Seq[InventoryItemDto]
  def inventoryItemDetails(id: UUID): InventoryItemDetailsDto
}

class InMemReadModelFacade extends ReadModelFacade {
  def inventoryItems: Seq[InventoryItemDto] = BullShitDB.list
  def inventoryItemDetails(id: UUID): InventoryItemDetailsDto = BullShitDB.details(id)
}

class InventoryListView {
  def handle(e: InventoryItemCreated) {
    BullShitDB.addToList(InventoryItemDto(e.id, e.name))
  }

  def handle(e: InventoryItemRenamed) {
    BullShitDB.rename(e.id, e.newName)
  }

  def handle(e: InventoryItemDeactivated) {
    BullShitDB.remove(e.id)
  }
}

class InventoryItemDetailView {
  def handle(e: InventoryItemCreated) {
    BullShitDB.addDetails(e.id, InventoryItemDetailsDto(e.id, e.name, 0, 0))
  }
  def handle(e: InventoryItemRenamed) {
    val d = BullShitDB.detailsItem(e.id)
    BullShitDB.addDetails(e.id, d.copy(name = e.newName, version = e.version))
  }
  def handle(e: ItemsRemovedFromInventory) {
    val d = BullShitDB.detailsItem(e.id)
    val newDetails = d.copy(
      currentCount = d.currentCount - e.count,
      version = e.version)
    BullShitDB.addDetails(e.id, newDetails)
  }
  def handle(e: ItemsCheckedInToInventory) {
    val d = BullShitDB.detailsItem(e.id)
    val newDetails = d.copy(
      currentCount = d.currentCount + e.count,
      version = e.version)
    BullShitDB.addDetails(e.id, newDetails)
  }
  def handle(e: InventoryItemDeactivated) {
    BullShitDB.removeDetails(e.id)
  }
}

object BullShitDB {
  var list = Seq[InventoryItemDto]()
  var details = Map[UUID, InventoryItemDetailsDto]()

  def addToList(dto: InventoryItemDto) {
    list :+= dto
  }

  def rename(id: UUID, newName: String) {
    list.find(_.id == id).foreach { old =>
      list = list.filterNot(_ == old) :+ old.copy(name = newName)
    }
  }

  def remove(id: UUID) {
    list = list.filterNot(_.id == id)
  }

  def addDetails(id: UUID, dto: InventoryItemDetailsDto) {
    details = details.updated(id, dto)
  }

  def detailsItem(id: UUID) = {
    details.getOrElse(id, throw new Exception("didn't find the original inventory"))
  }

  def removeDetails(id: UUID) {
    details = details - id
  }

}