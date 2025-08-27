package randomeventhelper.randomevents.gravedigger;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.runelite.api.GameObject;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.gameval.VarbitID;

@Data
public class Grave
{
	private GraveNumber graveNumber;
	private GameObject graveStone;
	private GameObject emptyGrave;
	private GameObject filledGrave;
	private Coffin requiredCoffin;
	private Coffin placedCoffin;

	public Grave(GraveNumber graveNumber, Coffin requiredCoffin)
	{
		this.graveNumber = graveNumber;
		this.graveStone = null;
		this.emptyGrave = null;
		this.filledGrave = null;
		this.requiredCoffin = requiredCoffin;
		this.placedCoffin = null;
	}

	public Grave(GraveNumber graveNumber)
	{
		this.graveNumber = graveNumber;
		this.graveStone = null;
		this.emptyGrave = null;
		this.filledGrave = null;
		this.requiredCoffin = null;
		this.placedCoffin = null;
	}

	@Getter
	@AllArgsConstructor
	public enum GraveNumber
	{
		ONE(
			ObjectID.MACRO_DIGGER_GRAVESTONE_1, ObjectID.MACRO_DIGGER_OPEN_GRAVE_EMPTY_1, ObjectID.MACRO_DIGGER_OPEN_GRAVE_COFFIN_1,
			VarbitID.MACRO_DIGGER_GRAVE_1, VarbitID.MACRO_DIGGER_COFFIN_1
		),
		TWO(
			ObjectID.MACRO_DIGGER_GRAVESTONE_2, ObjectID.MACRO_DIGGER_OPEN_GRAVE_EMPTY_2, ObjectID.MACRO_DIGGER_OPEN_GRAVE_COFFIN_2,
			VarbitID.MACRO_DIGGER_GRAVE_2, VarbitID.MACRO_DIGGER_COFFIN_2
		),
		THREE(
			ObjectID.MACRO_DIGGER_GRAVESTONE_3, ObjectID.MACRO_DIGGER_OPEN_GRAVE_EMPTY_3, ObjectID.MACRO_DIGGER_OPEN_GRAVE_COFFIN_3,
			VarbitID.MACRO_DIGGER_GRAVE_3, VarbitID.MACRO_DIGGER_COFFIN_3
		),
		FOUR(
			ObjectID.MACRO_DIGGER_GRAVESTONE_4, ObjectID.MACRO_DIGGER_OPEN_GRAVE_EMPTY_4, ObjectID.MACRO_DIGGER_OPEN_GRAVE_COFFIN_4,
			VarbitID.MACRO_DIGGER_GRAVE_4, VarbitID.MACRO_DIGGER_COFFIN_4
		),
		FIVE(
			ObjectID.MACRO_DIGGER_GRAVESTONE_5, ObjectID.MACRO_DIGGER_OPEN_GRAVE_EMPTY_5, ObjectID.MACRO_DIGGER_OPEN_GRAVE_COFFIN_5,
			VarbitID.MACRO_DIGGER_GRAVE_5, VarbitID.MACRO_DIGGER_COFFIN_5
		);

		private final int graveStoneObjectID;
		private final int emptyGraveObjectID;
		private final int filledGraveObjectID;
		private final int graveTypeVarbitID;
		private final int placedCoffinVarbitID;

		// Set of all the gravestone object IDs
//		private static final Set<Integer> GRAVESTONE_OBJECT_ID_SET = Stream.of(values()).map(GraveNumber::getGraveStoneObjectID).collect(ImmutableSet.toImmutableSet());
//		private static final Set<Integer> EMPTY_GRAVE_OBJECT_ID_SET = Stream.of(values()).map(GraveNumber::getEmptyGraveObjectID).collect(ImmutableSet.toImmutableSet());
//		private static final Set<Integer> FILLED_GRAVE_OBJECT_ID_SET = Stream.of(values()).map(GraveNumber::getFilledGraveObjectID).collect(ImmutableSet.toImmutableSet());

		private static final Map<Integer, GraveNumber> GRAVESTONE_OBJECT_ID_GRAVENUMBER_MAP;
		private static final Map<Integer, GraveNumber> EMPTY_GRAVE_OBJECT_ID_GRAVENUMBER_MAP;
		private static final Map<Integer, GraveNumber> FILLED_GRAVE_OBJECT_ID_GRAVENUMBER_MAP;

		static
		{
			GRAVESTONE_OBJECT_ID_GRAVENUMBER_MAP = Maps.uniqueIndex(ImmutableSet.copyOf(values()), GraveNumber::getGraveStoneObjectID);

			EMPTY_GRAVE_OBJECT_ID_GRAVENUMBER_MAP = Maps.uniqueIndex(ImmutableSet.copyOf(values()), GraveNumber::getEmptyGraveObjectID);

			FILLED_GRAVE_OBJECT_ID_GRAVENUMBER_MAP = Maps.uniqueIndex(ImmutableSet.copyOf(values()), GraveNumber::getFilledGraveObjectID);
		}

		public static boolean isGravestoneObjectID(int objectID)
		{
			return GRAVESTONE_OBJECT_ID_GRAVENUMBER_MAP.containsKey(objectID);
		}

		public static boolean isEmptyGraveObjectID(int objectID)
		{
			return EMPTY_GRAVE_OBJECT_ID_GRAVENUMBER_MAP.containsKey(objectID);
		}

		public static boolean isFilledGraveObjectID(int objectID)
		{
			return FILLED_GRAVE_OBJECT_ID_GRAVENUMBER_MAP.containsKey(objectID);
		}

		public static GraveNumber getGraveNumberFromGravestoneObjectID(int objectID)
		{
			return GRAVESTONE_OBJECT_ID_GRAVENUMBER_MAP.get(objectID);
		}

		public static GraveNumber getGraveNumberFromEmptyGraveObjectID(int objectID)
		{
			return EMPTY_GRAVE_OBJECT_ID_GRAVENUMBER_MAP.get(objectID);
		}

		public static GraveNumber getGraveNumberFromFilledGraveObjectID(int objectID)
		{
			return FILLED_GRAVE_OBJECT_ID_GRAVENUMBER_MAP.get(objectID);
		}
	}
}
