package randomeventhelper.randomevents.prisonpete;

import com.google.common.collect.ImmutableMap;
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
	GOAT(5491, 5492, 11034),
	SHEEP(369, 5493, 10751);


	private final int npcId1;
	private final int npcId2;
	private final int modelID;

	private static final Map<Integer, BalloonAnimal> MODEL_ID_TO_BALLOON_MAP;
	private static final Map<Integer, BalloonAnimal> NPC_ID_TO_BALLOON_MAP;

	public boolean isMatch(NPC npc)
	{
		return npc != null && (npc.getId() == npcId1 || npc.getId() == npcId2);
	}

	static
	{
		MODEL_ID_TO_BALLOON_MAP = Maps.uniqueIndex(ImmutableSet.copyOf(values()), BalloonAnimal::getModelID);
	}

	static
	{
		ImmutableMap.Builder<Integer, BalloonAnimal> npcIDBalloonMap = new ImmutableMap.Builder<>();

		for (BalloonAnimal balloonAnimal : values())
		{
			npcIDBalloonMap.put(balloonAnimal.getNpcId1(), balloonAnimal);
			npcIDBalloonMap.put(balloonAnimal.getNpcId2(), balloonAnimal);
		}

		NPC_ID_TO_BALLOON_MAP = npcIDBalloonMap.build();
	}

	public static BalloonAnimal fromWidgetModel(int modelID)
	{
		return MODEL_ID_TO_BALLOON_MAP.get(modelID);
	}

	public static BalloonAnimal fromNpcID(int npcID)
	{
		return NPC_ID_TO_BALLOON_MAP.get(npcID);
	}
}
