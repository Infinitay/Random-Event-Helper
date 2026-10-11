package randomeventhelper.randomevents.prisonpete;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.NPC;

import java.util.Map;

@Getter
@AllArgsConstructor
public enum BalloonAnimal
{
	CAT(371, 5489, 10750),
	DOG(370, 5488, 11028),
	GOAT(5491,5492, 11034),
	SHEEP(369, 5493,10751);


	private final int npcId1;
	private final int npcId2;
	private final int modelID;

	private static final Map<Integer, BalloonAnimal> MODEL_BALLOON_MAP;

	public boolean isMatch(NPC npc)
	{
		return npc != null && (npc.getId() == npcId1 || npc.getId() == npcId2);
	}

	static
	{
		MODEL_BALLOON_MAP = Maps.uniqueIndex(ImmutableSet.copyOf(values()), BalloonAnimal::getModelID);
	}

	public static BalloonAnimal fromWidgetModel(int model)
	{
		return MODEL_BALLOON_MAP.get(model);
	}
}
