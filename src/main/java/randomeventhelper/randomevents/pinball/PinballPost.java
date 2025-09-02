package randomeventhelper.randomevents.pinball;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.gameval.ObjectID;

@Getter
@AllArgsConstructor
public enum PinballPost
{
	AIR(0, ObjectID.PINBALL_POST_TREE_INACTIVE),
	EARTH(1, ObjectID.PINBALL_POST_IRON_INACTIVE),
	FIRE(2, ObjectID.PINBALL_POST_COAL_INACTIVE),
	NATURE(3, ObjectID.PINBALL_POST_FISHING_INACTIVE),
	WATER(4, ObjectID.PINBALL_POST_ESSENCE_INACTIVE);

	@Getter
	@AllArgsConstructor
	enum PinballVarbit
	{
		AIR(0),
		EARTH(1),
		FIRE(2),
		NATURE(3),
		WATER(4);

		private final int value;
	}

	private final int varbitValue;
	private final int objectID;

	private static final Map<Integer, PinballPost> VARBIT_PINBALL_POST_MAP;
	private static final Map<Integer, PinballPost> OBJECT_ID_PINBALL_POST_MAP;

	static
	{
		VARBIT_PINBALL_POST_MAP = Maps.uniqueIndex(ImmutableSet.copyOf(values()), PinballPost::getVarbitValue);
	}

	static
	{
		OBJECT_ID_PINBALL_POST_MAP = Maps.uniqueIndex(ImmutableSet.copyOf(values()), PinballPost::getObjectID);
	}

	public static PinballPost fromVarbitValue(int varbitValue)
	{
		return VARBIT_PINBALL_POST_MAP.get(varbitValue);
	}

	public static PinballPost fromObjectID(int objectID)
	{
		return OBJECT_ID_PINBALL_POST_MAP.get(objectID);
	}
}
