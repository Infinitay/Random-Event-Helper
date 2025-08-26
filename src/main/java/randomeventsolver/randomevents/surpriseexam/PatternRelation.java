package randomeventsolver.randomevents.surpriseexam;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import randomeventsolver.data.RandomEventItem;

@Getter
@AllArgsConstructor
public enum PatternRelation
{
	// Left off after beer
	RUNES(Set.of(RandomEventItem.AIR_RUNE)),
	RUNECRAFTING(Set.of(RandomEventItem.AIR_RUNE)),
	MAGIC(Set.of(RandomEventItem.AIR_RUNE)),
	COMBAT(Set.of(RandomEventItem.AIR_RUNE, RandomEventItem.SQUARE_SHIELD_1, RandomEventItem.ARROWS, RandomEventItem.BATTLE_AXE)),
	MELEE(Set.of(RandomEventItem.SQUARE_SHIELD_1, RandomEventItem.BATTLE_AXE)),
	SHIELD(Set.of(RandomEventItem.SQUARE_SHIELD_1)),
	COOKING(Set.of(RandomEventItem.APRON, RandomEventItem.BANANA, RandomEventItem.TUNA)),
	FOOD(Set.of(RandomEventItem.APRON, RandomEventItem.BANANA, RandomEventItem.TUNA)),
	CRAFTING(Set.of(RandomEventItem.APRON)),
	AMMO(Set.of(RandomEventItem.ARROWS)),
	RANGE(Set.of(RandomEventItem.ARROWS)),
	WEAPON(Set.of(RandomEventItem.ARROWS, RandomEventItem.BATTLE_AXE)),
	FRUITS_AND_VEGETABLES(Set.of(RandomEventItem.BANANA)),
	FISH(Set.of(RandomEventItem.TUNA)),
	DRINKS(Set.of(RandomEventItem.BEER)),
	ALCOHOL(Set.of(RandomEventItem.BEER));

	private final Set<RandomEventItem> itemModelIDSet;

	/*private static final Map<PatternRelation, Set<RandomEventItem>> PATTERN_RELATIONS_MAP;

	static
	{
		ImmutableMap.Builder<PatternRelation, Set<RandomEventItem>> itemModelIDBuilder = new ImmutableMap.Builder<>();

		for (PatternRelation randomEventItem : values())
		{
			itemModelIDBuilder.put(randomEventItem.getModelID(), randomEventItem);
		}

		ITEM_MODEL_ID_MAP = itemModelIDBuilder.build();
	}*/
}
