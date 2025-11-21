package randomeventhelper;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import randomeventhelper.randomevents.surpriseexam.OSRSItemRelationshipSystem;
import randomeventhelper.randomevents.surpriseexam.RandomEventItem;

public class RelationshipSystemTest
{
	OSRSItemRelationshipSystem relationshipSystem = new OSRSItemRelationshipSystem();

	@Test
	public void testPatternMatching()
	{
		RelationshipSystemTestMatchingData puzzle1 = new RelationshipSystemTestMatchingData(
			"I feel like a fish out of water!",
			"[BATTLE_AXE (41176), NECKLACE (41216), CANDLE_LANTERN (41229), CAPE_OF_LEGENDS (41167), RAKE (41212), ONION (41226), HARPOON (41158), RING (27091), FIGHTER_BOOTS (41160), HAMMER (41183), MED_HELM (41189), BEER (41152), TROUT_COD_PIKE_SALMON_4 (41217), TROUT_COD_PIKE_SALMON_3 (41163), AIR_RUNE (41168)]",
			List.of(RandomEventItem.HARPOON, RandomEventItem.TROUT_COD_PIKE_SALMON_4, RandomEventItem.TROUT_COD_PIKE_SALMON_3)
		);
		List<RandomEventItem> puzzle1ActualItems = relationshipSystem.findItemsByHint(puzzle1.getHint(), puzzle1.getGivenItems(), 3).subList(0, 3);
		Assertions.assertThat(puzzle1ActualItems).containsExactlyInAnyOrderElementsOf(puzzle1.getExpectedMatchingItems());
	}

	@Test
	public void testNextMissingItem()
	{
		// initial sequence: BREAD, CAKE, PIE | given: PIZZA, ESSENCE, ARROW, BOOT | expected: PIZZA
		RelationshipSystemTestNextMissingItemData puzzle1 = new RelationshipSystemTestNextMissingItemData(
			"[BREAD (41172), CAKE (41202), PIE (41205)]",
			"[PIZZA (41185), RUNE_OR_ESSENCE (41182), ARROWS (41177), LEATHER_BOOTS (41220)]",
			RandomEventItem.PIZZA
		);
		RandomEventItem puzzle1ActualNextMissingItem = relationshipSystem.findMissingItem(puzzle1.getInitialSequenceItems(), puzzle1.getItemChoices());
		Assertions.assertThat(puzzle1ActualNextMissingItem).isEqualTo(puzzle1.getExpectedNextMissingItem());
	}

	@Data
	static class RelationshipSystemTestMatchingData
	{
		private String hint;
		private List<RandomEventItem> givenItems;
		private List<RandomEventItem> expectedMatchingItems;

		public RelationshipSystemTestMatchingData(String hint, String givenItemsDebugString, List<RandomEventItem> expectedMatchingItems)
		{
			this.hint = hint;
			this.givenItems = fromDebugString(givenItemsDebugString);
			this.expectedMatchingItems = expectedMatchingItems;
		}
	}

	@Data
	static class RelationshipSystemTestNextMissingItemData
	{
		private List<RandomEventItem> initialSequenceItems;
		private List<RandomEventItem> itemChoices;
		private RandomEventItem expectedNextMissingItem;

		public RelationshipSystemTestNextMissingItemData(String initialSequenceItemsDebugString, String givenItemsDebugString, RandomEventItem expectedNextMissingItem)
		{
			this.initialSequenceItems = fromDebugString(initialSequenceItemsDebugString);
			this.itemChoices = fromDebugString(givenItemsDebugString);
			this.expectedNextMissingItem = expectedNextMissingItem;
		}
	}

	/**
	 * Converts a string into a list of @{@link RandomEventItem}
	 *
	 * @param debugString A comma-separated string of @{@link RandomEventItem} names (eg. [BATTLE_AXE (41176), NECKLACE (41216), CANDLE_LANTERN (41229), CAPE_OF_LEGENDS (41167), RAKE (41212), ONION (41226), HARPOON (41158), RING (27091), FIGHTER_BOOTS (41160), HAMMER (41183), MED_HELM (41189), BEER (41152), TROUT_COD_PIKE_SALMON_4 (41217), TROUT_COD_PIKE_SALMON_3 (41163), AIR_RUNE (41168)]
	 * @return List of associated {@link RandomEventItem}s
	 */
	private static List<RandomEventItem> fromDebugString(String debugString)
	{
		// Sanitize the string to remove brackets
		debugString = debugString.replace("[", "").replace("]", "");
		List<RandomEventItem> randomEventItems = new ArrayList<>();
		String[] separatedItems = debugString.split(",");
		for (String randomEventItem : separatedItems)
		{
			String itemName = randomEventItem.trim().split(" ")[0];
			randomEventItems.add(RandomEventItem.valueOf(itemName));
		}
		return randomEventItems;
	}
}
