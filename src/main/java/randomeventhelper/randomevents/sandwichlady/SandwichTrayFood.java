package randomeventhelper.randomevents.sandwichlady;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SandwichTrayFood
{
	MEAT_PIE(10730, "Meat pie"),
	KEBAB(10729, "Kebab"),
	CHOCOLATE_BAR(10728, "Chocolate bar"),
	BAGUETTE(10726, "Baguette"),
	TRIANGLE_SANDWICH(10732, "Triangle sandwich"),
	SQUARE_SANDWICH(10731, "Square sandwich"),
	ROLL(10727, "Bread roll");

	private final int modelID;
	private final String name;

	private static final Map<Integer, SandwichTrayFood> FOOD_MODEL_ID_MAP;
	private static final Map<String, SandwichTrayFood> FOOD_NAME_MAP;

	static
	{
		ImmutableMap.Builder<Integer, SandwichTrayFood> foodModelIDBuilder = new ImmutableMap.Builder<>();

		for (SandwichTrayFood sandwichTrayFood : values())
		{
			foodModelIDBuilder.put(sandwichTrayFood.getModelID(), sandwichTrayFood);
		}

		FOOD_MODEL_ID_MAP = foodModelIDBuilder.build();
	}

	static
	{
		ImmutableMap.Builder<String, SandwichTrayFood> foodNameBuilder = new ImmutableMap.Builder<>();

		for (SandwichTrayFood sandwichTrayFood : values())
		{
			foodNameBuilder.put(sandwichTrayFood.getName().toLowerCase(), sandwichTrayFood);
		}

		FOOD_NAME_MAP = foodNameBuilder.build();
	}

	public static SandwichTrayFood fromModelID(int modelID)
	{
		return FOOD_MODEL_ID_MAP.get(modelID);
	}

	public static SandwichTrayFood fromName(String name)
	{
		return FOOD_NAME_MAP.get(name.toLowerCase());
	}
}
