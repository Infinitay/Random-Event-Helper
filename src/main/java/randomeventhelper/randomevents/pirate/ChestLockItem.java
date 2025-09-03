package randomeventhelper.randomevents.pirate;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChestLockItem
{
	COINS(0, "COINS", 7752),
	BOWL(1, "BOWL", 20104),
	BAR(2, "BAR", 7751),
	RING(3, "RING", 7753);


	private final int varbitValue;
	private final String label;
	private final int modelID;

	private static final Map<String, ChestLockItem> LABEL_CHEST_LOCK_ITEM_MAP;
	private static final Map<Integer, ChestLockItem> VARBIT_CHEST_LOCK_ITEM_MAP;

	static
	{
		LABEL_CHEST_LOCK_ITEM_MAP = Maps.uniqueIndex(ImmutableSet.copyOf(values()), ChestLockItem::getLabel);
	}

	static
	{
		VARBIT_CHEST_LOCK_ITEM_MAP = Maps.uniqueIndex(ImmutableSet.copyOf(values()), ChestLockItem::getVarbitValue);
	}

	public static ChestLockItem fromLabel(String label)
	{
		return LABEL_CHEST_LOCK_ITEM_MAP.get(label);
	}

	public static ChestLockItem fromVarbitValue(int varbitValue)
	{
		return VARBIT_CHEST_LOCK_ITEM_MAP.get(varbitValue);
	}
}
