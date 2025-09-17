package randomeventhelper.randomevents.pirate;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class PirateChestSolver
{
	private ChestLockItem activeLeftItem;
	private ChestLockItem activeCenterItem;
	private ChestLockItem activeRightItem;
	private ChestLockItem requiredLeftItem;
	private ChestLockItem requiredCenterItem;
	private ChestLockItem requiredRightItem;
	private boolean isSolved;

	private int leftSlotUseWidget;
	private int centerSlotUseWidget;
	private int rightSlotUseWidget;

	public PirateChestSolver()
	{
		this.activeLeftItem = null;
		this.activeCenterItem = null;
		this.activeRightItem = null;
		this.requiredLeftItem = null;
		this.requiredCenterItem = null;
		this.requiredRightItem = null;
		this.isSolved = false;
	}

	public void updateRequiredItem(ChestLockSlot chestLockSlot, ChestLockItem chestLockItem)
	{
		if (chestLockItem == null || chestLockSlot == null)
		{
			log.debug("Could not find ChestLockItem or ChestLockSlot for updateRequiredItem");
			return;
		}
		switch (chestLockSlot)
		{
			case LEFT:
				this.requiredLeftItem = chestLockItem;
				break;
			case CENTER:
				this.requiredCenterItem = chestLockItem;
				break;
			case RIGHT:
				this.requiredRightItem = chestLockItem;
				break;
			default:
				break;
		}

		log.debug("Updated required chest lock item: {} to {}", chestLockSlot, chestLockItem);
	}

	public void updateActiveItem(int varbitID, int varbitValue)
	{
		ChestLockItem chestLockItem = ChestLockItem.fromVarbitValue(varbitValue);
		ChestLockSlot chestLockSlot = ChestLockSlot.fromVarbitID(varbitID);
		if (chestLockItem == null || chestLockSlot == null)
		{
			log.debug("Could not find ChestLockItem or ChestLockSlot for varbitID: {}, varbitValue: {}", varbitID, varbitValue);
			return;
		}
		switch (chestLockSlot)
		{
			case LEFT:
				this.activeLeftItem = chestLockItem;
				break;
			case CENTER:
				this.activeCenterItem = chestLockItem;
				break;
			case RIGHT:
				this.activeRightItem = chestLockItem;
				break;
			default:
				break;
		}
		log.debug("Updated current active chest lock item: {} to {}", chestLockSlot, chestLockItem);
	}

	public boolean isChestCorrectlySet()
	{
		if (this.requiredLeftItem == null || this.requiredCenterItem == null || this.requiredRightItem == null)
		{
			log.debug("Cannot solve the pirate chest puzzle, required items are not all set.");
			return false;
		}
		if (this.activeLeftItem == null || this.activeCenterItem == null || this.activeRightItem == null)
		{
			log.debug("Cannot solve the pirate chest puzzle, active items are not all set.");
			return false;
		}
		if (this.activeLeftItem == this.requiredLeftItem &&
			this.activeCenterItem == this.requiredCenterItem &&
			this.activeRightItem == this.requiredRightItem)
		{
			this.isSolved = true;
			log.debug("Pirate chest locks are correctly set.");
			return true;
		}
		log.debug("Pirate chest locks are not correctly set.");
		return false;
	}

	public void solve()
	{
		if (this.isSolved)
		{
			log.debug("Pirate chest is already solved.");
			return;
		}
		if (this.requiredLeftItem == null || this.requiredCenterItem == null || this.requiredRightItem == null)
		{
			log.debug("Cannot solve the pirate chest puzzle, required items are not all set.");
			return;
		}
		if (this.activeLeftItem == null || this.activeCenterItem == null || this.activeRightItem == null)
		{
			log.debug("Cannot solve the pirate chest puzzle, active items are not all set.");
			return;
		}
		int size = ChestLockItem.values().length;
		if (this.activeLeftItem != this.requiredLeftItem)
		{
			int addDiff = (this.requiredLeftItem.getVarbitValue() - this.activeLeftItem.getVarbitValue() + size) % size;
			int subtractDiff = (this.activeLeftItem.getVarbitValue() - this.requiredLeftItem.getVarbitValue() + size) % size;
			if (addDiff <= subtractDiff)
			{
				this.leftSlotUseWidget = ChestLockSlot.LEFT.getAddWidgetID();
			}
			else
			{
				this.leftSlotUseWidget = ChestLockSlot.LEFT.getSubtractWidgetID();
			}
			String widgetToUse = (this.leftSlotUseWidget == ChestLockSlot.LEFT.getAddWidgetID()) ? "Add" : "Subtract";
			log.debug("Left slot: Active item {} -> Required item {}, Should use {} Widget ({})", this.activeLeftItem, this.requiredLeftItem, widgetToUse, this.leftSlotUseWidget);
		}
		else
		{
			this.leftSlotUseWidget = -1;
		}
		if (this.activeCenterItem != this.requiredCenterItem)
		{
			int addDiff = (this.requiredCenterItem.getVarbitValue() - this.activeCenterItem.getVarbitValue() + size) % size;
			int subtractDiff = (this.activeCenterItem.getVarbitValue() - this.requiredCenterItem.getVarbitValue() + size) % size;
			if (addDiff <= subtractDiff)
			{
				this.centerSlotUseWidget = ChestLockSlot.CENTER.getAddWidgetID();
			}
			else
			{
				this.centerSlotUseWidget = ChestLockSlot.CENTER.getSubtractWidgetID();
			}
			String widgetToUse = (this.centerSlotUseWidget == ChestLockSlot.CENTER.getAddWidgetID()) ? "Add" : "Subtract";
			log.debug("Center slot: Active item {} -> Required item {}, Should use {} Widget ({})", this.activeCenterItem, this.requiredCenterItem, widgetToUse, this.centerSlotUseWidget);
		}
		else
		{
			this.centerSlotUseWidget = -1;
		}
		if (this.activeRightItem != this.requiredRightItem)
		{
			int addDiff = (this.requiredRightItem.getVarbitValue() - this.activeRightItem.getVarbitValue() + size) % size;
			int subtractDiff = (this.activeRightItem.getVarbitValue() - this.requiredRightItem.getVarbitValue() + size) % size;
			if (addDiff <= subtractDiff)
			{
				this.rightSlotUseWidget = ChestLockSlot.RIGHT.getAddWidgetID();
			}
			else
			{
				this.rightSlotUseWidget = ChestLockSlot.RIGHT.getSubtractWidgetID();
			}
			String widgetToUse = (this.rightSlotUseWidget == ChestLockSlot.RIGHT.getAddWidgetID()) ? "Add" : "Subtract";
			log.debug("Right slot: Active item {} -> Required item {}, Should use {} Widget ({})", this.activeRightItem, this.requiredRightItem, widgetToUse, this.rightSlotUseWidget);
		}
		else
		{
			this.rightSlotUseWidget = -1;
		}
	}

	public void reset()
	{
		this.activeLeftItem = null;
		this.activeCenterItem = null;
		this.activeRightItem = null;
		this.requiredLeftItem = null;
		this.requiredCenterItem = null;
		this.requiredRightItem = null;
		this.isSolved = false;
	}
}
