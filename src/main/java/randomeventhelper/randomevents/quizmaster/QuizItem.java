package randomeventhelper.randomevents.quizmaster;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum QuizItem
{
	TROUT_COD_PIKE_SALMON(Type.FOOD, 8829),
	TUNA(Type.FOOD, 8830),
	LONGSWORD(Type.WEAPON, 8836),
	BATTLE_AXE(Type.WEAPON, 8828),
	MED_HELM(Type.ARMOUR, 8833),
	KITESHIELD(Type.ARMOUR, 8832),
	SECATEURS(Type.TOOL, 8835),
	SPADE(Type.TOOL, 8837),
	RING(Type.JEWELRY, 8834),
	NECKLACE(Type.JEWELRY, 8831);

	private final Type type;
	private final int modelID;

	enum Type
	{
		FOOD,
		WEAPON,
		ARMOUR,
		TOOL,
		JEWELRY,
	}

	private static final Map<Integer, QuizItem> MODEL_ID_QUIZ_ITEM_MAP;
	private static final Map<QuizItem, Type> QUIZ_ITEM_TYPE_MAP;

	static
	{
		MODEL_ID_QUIZ_ITEM_MAP = Maps.uniqueIndex(ImmutableSet.copyOf(values()), QuizItem::getModelID);
	}

	static
	{
		ImmutableMap.Builder<QuizItem, Type> quizItemTypeMap = new ImmutableMap.Builder<>();

		for (QuizItem quizItem : values())
		{
			quizItemTypeMap.put(quizItem, quizItem.getType());
		}

		QUIZ_ITEM_TYPE_MAP = quizItemTypeMap.build();
	}

	public static QuizItem fromModelID(int modelID)
	{
		return MODEL_ID_QUIZ_ITEM_MAP.get(modelID);
	}
}
