package randomeventhelper.randomevents.pirate;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarbitID;

@Getter
@AllArgsConstructor
public enum ChestLockSlot
{
	LEFT(VarbitID.PIRATE_COMBILOCK_LEFT, InterfaceID.PirateCombilock.LEFT_UP, InterfaceID.PirateCombilock.LEFT_DOWN, InterfaceID.PirateCombilock.LEFT_TEXT),
	CENTER(VarbitID.PIRATE_COMBILOCK_CENTRE, InterfaceID.PirateCombilock.CENTRE_UP, InterfaceID.PirateCombilock.CENTRE_DOWN, InterfaceID.PirateCombilock.CENTRE_TEXT),
	RIGHT(VarbitID.PIRATE_COMBILOCK_RIGHT, InterfaceID.PirateCombilock.RIGHT_UP, InterfaceID.PirateCombilock.RIGHT_DOWN, InterfaceID.PirateCombilock.RIGHT_TEXT);

	public final int varbitID;
	private final int addWidgetID;
	private final int subtractWidgetID;
	private final int requiredItemLabelWidgetID;

	private static final Map<Integer, ChestLockSlot> VARBIT_CHEST_LOCK_SLOT_MAP;

	static
	{
		VARBIT_CHEST_LOCK_SLOT_MAP = Maps.uniqueIndex(ImmutableSet.copyOf(values()), ChestLockSlot::getVarbitID);
	}

	public static ChestLockSlot fromVarbitID(int varbitID)
	{
		return VARBIT_CHEST_LOCK_SLOT_MAP.get(varbitID);
	}
}
