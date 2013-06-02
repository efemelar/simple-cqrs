package ui

import simpleCQRS._

object Boot extends App {
  val bus      = new FakeBus
  val storage  = new InMemEventStore(bus.publish)
  val repo     = new EventStoreRepository[InventoryItem](storage)
  val commands = new InventoryCommandHandlers(repo)

  bus.registerHandler[CheckInItemsToInventory](commands.handle)
  bus.registerHandler[CreateInventoryItem](commands.handle)
  bus.registerHandler[DeactivateInventoryItem](commands.handle)
  bus.registerHandler[RemoveItemsFromInventory](commands.handle)
  bus.registerHandler[RenameInventoryItem](commands.handle)


  val detail = new InventoryItemDetailView
  bus.registerHandler[InventoryItemCreated](detail.handle)
  bus.registerHandler[ItemsRemovedFromInventory](detail.handle)
  bus.registerHandler[ItemsCheckedInToInventory](detail.handle)
  bus.registerHandler[InventoryItemDeactivated](detail.handle)


  val list = new InventoryListView
  bus.registerHandler[InventoryItemCreated](list.handle)
  bus.registerHandler[InventoryItemRenamed](list.handle)
  bus.registerHandler[InventoryItemDeactivated](list.handle)
}