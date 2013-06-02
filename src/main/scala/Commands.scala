package simpleCQRS


trait Command extends Message

case class DeactivateInventoryItem(
  inventoryItemId: UUID,
  originalVersion: Int
) extends Command

case class CreateInventoryItem(
  inventoryItemId: UUID,
  name: String
) extends Command

case class RenameInventoryItem(
  inventoryItemId: UUID,
  newName: String,
  originalVersion: Int
) extends Command

case class CheckInItemsToInventory(
  inventoryItemId: UUID,
  count: Int,
  originalVersion: Int
) extends Command

case class RemoveItemsFromInventory(
  inventoryItemId: UUID,
  count: Int,
  originalVersion: Int
) extends Command
