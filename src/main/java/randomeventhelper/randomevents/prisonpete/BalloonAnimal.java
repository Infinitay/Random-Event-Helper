package randomeventhelper.randomevents.prisonpete;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public enum BalloonAnimal
{
	CAT(5489, 10750),
	DOG(370, 11028),
	GOAT(5491, 11034),
	SHEEP(369,10751);


	private final int npcId;
	private final int modelID;

	private static final Map<Integer, BalloonAnimal> MODEL_BALLOON_MAP;

	static
	{
		MODEL_BALLOON_MAP = Maps.uniqueIndex(ImmutableSet.copyOf(values()), BalloonAnimal::getModelID);
	}

	public static BalloonAnimal fromWidgetModel(int model)
	{
		return MODEL_BALLOON_MAP.get(model);
	}
}
