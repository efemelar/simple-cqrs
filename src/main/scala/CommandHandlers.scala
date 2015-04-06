package simpleCQRS

class InventoryCommandHandlers(repository: Repository[InventoryItem]) {

  def handle(cmd: CreateInventoryItem) {
    val item = new InventoryItem(cmd.inventoryItemId, cmd.name)
    item.init()
    repository.save(item, -1)
  }

  def handle(cmd: DeactivateInventoryItem) {
    val item = repository.getById(cmd.inventoryItemId)
    item.deactivate()
    repository.save(item, cmd.originalVersion)
  }

  def handle(cmd: RemoveItemsFromInventory) {
    val item = repository.getById(cmd.inventoryItemId)
    item.remove(cmd.count)
    repository.save(item, cmd.originalVersion)
  }

  def handle(cmd: CheckInItemsToInventory) {
    var item = repository.getById(cmd.inventoryItemId)
    item.checkIn(cmd.count)
    repository.save(item, cmd.originalVersion)
  }

  def handle(cmd: RenameInventoryItem) {
    var item = repository.getById(cmd.inventoryItemId)
    item.changeName(cmd.newName)
    repository.save(item, cmd.originalVersion)
  }
}