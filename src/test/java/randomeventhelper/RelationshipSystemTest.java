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

		RelationshipSystemTestMatchingData puzzle2 = new RelationshipSystemTestMatchingData(
			"Some professions use such strange headgear.",
			"[CAKE (41202), TROUT_COD_PIKE_SALMON_4 (41217), SHARK (41166), ARROWS (41177), TINDERBOX (41154), HERRING_OR_MACKEREL (41193), PLATELEGS (41179), ORE (41170), LEDERHOSEN_HAT (41164), LONGSWORD (41150), BONES (2674), POT (41223), PIRATE_HAT (41187), JESTER_HAT (41196), AXE (41184)]",
			List.of(RandomEventItem.LEDERHOSEN_HAT, RandomEventItem.PIRATE_HAT, RandomEventItem.JESTER_HAT)
		);
		List<RandomEventItem> puzzle2ActualItems = relationshipSystem.findItemsByHint(puzzle2.getHint(), puzzle2.getGivenItems(), 3).subList(0, 3);
		Assertions.assertThat(puzzle2ActualItems).containsExactlyInAnyOrderElementsOf(puzzle2.getExpectedMatchingItems());

		RelationshipSystemTestMatchingData puzzle3 = new RelationshipSystemTestMatchingData(
			"I shall unmask this pattern!",
			"[ONION (41226), SCIMITAR (41192), LONGBOW (41198), HIGHWAYMAN_MASK (41195), MIME_MASK (41191), BATTLE_AXE (41176), KITCHEN_KNIFE (41201), SPADE (41155), BAR (41153), TROUT_COD_PIKE_SALMON_3 (41163), JUG (41225), FROG_MASK (27101), POTION (41149), THREAD (41218), CANDLE_LANTERN (41229)]",
			List.of(RandomEventItem.MIME_MASK, RandomEventItem.FROG_MASK, RandomEventItem.HIGHWAYMAN_MASK)
		);
		List<RandomEventItem> puzzle3ActualItems = relationshipSystem.findItemsByHint(puzzle3.getHint(), puzzle3.getGivenItems(), 3).subList(0, 3);
		Assertions.assertThat(puzzle3ActualItems).containsExactlyInAnyOrderElementsOf(puzzle3.getExpectedMatchingItems());

		RelationshipSystemTestMatchingData puzzle4 = new RelationshipSystemTestMatchingData(
			"All this work is making me thirsty.",
			"[SHORT_BOW (41171), LONGSWORD (41150), COCKTAIL_1 (27097), HARPOON (41158), CUP_OF_TEA (41162), BOOK (41181), TROUT_COD_PIKE_SALMON_3 (41163), PLANT_POT (41208), LOGS (41232), TINDERBOX (41154), GEM_WITH_CROSS (41151), SECATEURS (41197), TROUT_COD_PIKE_SALMON_4 (41217), ORE (41170), BEER (41152)]",
			List.of(RandomEventItem.COCKTAIL_1, RandomEventItem.CUP_OF_TEA, RandomEventItem.BEER)
		);
		List<RandomEventItem> puzzle4ActualItems = relationshipSystem.findItemsByHint(puzzle4.getHint(), puzzle4.getGivenItems(), 3).subList(0, 3);
		Assertions.assertThat(puzzle4ActualItems).containsExactlyInAnyOrderElementsOf(puzzle4.getExpectedMatchingItems());

		RelationshipSystemTestMatchingData puzzle5 = new RelationshipSystemTestMatchingData(
			"The pen may be mightier than the sword, but against a dragon? I'll take a melee weapon.",
			"[BOTTLE (41175), BAR (41153), INSULATED_BOOTS (27104), SCIMITAR (41192), TROUT_COD_PIKE_SALMON_3 (41163), BATTLE_AXE (41176), CROSSBOW (41146), BEER (41152), NEEDLE (41199), RAKE (41212), RUNE_OR_ESSENCE (41182), BONES (2674), POT (41223), LONGSWORD (41150), LOGS (41232)]",
			List.of(RandomEventItem.SCIMITAR, RandomEventItem.BATTLE_AXE, RandomEventItem.LONGSWORD)
		);
		List<RandomEventItem> puzzle5ActualItems = relationshipSystem.findItemsByHint(puzzle5.getHint(), puzzle5.getGivenItems(), 3).subList(0, 3);
		Assertions.assertThat(puzzle5ActualItems).containsExactlyInAnyOrderElementsOf(puzzle5.getExpectedMatchingItems());

		RelationshipSystemTestMatchingData puzzle6 = new RelationshipSystemTestMatchingData(
			"This pattern is as sharp as an arrow.",
			"[CROSSBOW (41146), SCIMITAR (41192), BATTLE_AXE (41176), LONGBOW (41198), SHEARS (41227), TINDERBOX (41154), JUG (41225), FROG_MASK (27101), COCKTAIL_1 (27097), CAKE (41202), BOTTLE (41175), GARDENING_TROWEL (41210), LONGSWORD (41150), PIRATE_HAT (41187), ARROWS (41177)]",
			List.of(RandomEventItem.CROSSBOW, RandomEventItem.LONGBOW, RandomEventItem.ARROWS)
		);
		List<RandomEventItem> puzzle6ActualItems = relationshipSystem.findItemsByHint(puzzle6.getHint(), puzzle6.getGivenItems(), 3).subList(0, 3);
		Assertions.assertThat(puzzle6ActualItems).containsExactlyInAnyOrderElementsOf(puzzle6.getExpectedMatchingItems());

		RelationshipSystemTestMatchingData puzzle7 = new RelationshipSystemTestMatchingData(
			"Fancy a drink?",
			"[CUP_OF_TEA (41162), SECATEURS (41197), ORE (41170), TROUT_COD_PIKE_SALMON_4 (41217), BEER (41152), PLANT_POT (41208), TINDERBOX (41154), LOGS (41232), BOOK (41181), LONGSWORD (41150), HARPOON (41158), SHORT_BOW (41171), GEM_WITH_CROSS (41151), TROUT_COD_PIKE_SALMON_3 (41163), COCKTAIL_1 (27097)]",
			List.of(RandomEventItem.CUP_OF_TEA, RandomEventItem.BEER, RandomEventItem.COCKTAIL_1)
		);
		List<RandomEventItem> puzzle7ActualItems = relationshipSystem.findItemsByHint(puzzle7.getHint(), puzzle7.getGivenItems(), 3).subList(0, 3);
		Assertions.assertThat(puzzle7ActualItems).containsExactlyInAnyOrderElementsOf(puzzle7.getExpectedMatchingItems());
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

		RelationshipSystemTestNextMissingItemData puzzle2 = new RelationshipSystemTestNextMissingItemData(
			"[LONGSWORD (41150), FULL_HELM (41178), KITESHIELD (41200)]",
			"[EARTH_RUNE (41157), BAR (41153), PLATEBODY (27094), CAKE (41202)]",
			RandomEventItem.PLATEBODY
		);
		RandomEventItem puzzle2ActualNextMissingItem = relationshipSystem.findMissingItem(puzzle2.getInitialSequenceItems(), puzzle2.getItemChoices());
		Assertions.assertThat(puzzle2ActualNextMissingItem).isEqualTo(puzzle2.getExpectedNextMissingItem());

		RelationshipSystemTestNextMissingItemData puzzle3 = new RelationshipSystemTestNextMissingItemData(
			"[NECKLACE (41216), TIARA (41148), HOLY_SYMBOL (41159)]",
			"[HAMMER (41183), RING (27091), HERRING_OR_MACKEREL (41193)]",
			RandomEventItem.RING
		);
		RandomEventItem puzzle3ActualNextMissingItem = relationshipSystem.findMissingItem(puzzle3.getInitialSequenceItems(), puzzle3.getItemChoices());
		Assertions.assertThat(puzzle3ActualNextMissingItem).isEqualTo(puzzle3.getExpectedNextMissingItem());

		RelationshipSystemTestNextMissingItemData puzzle4 = new RelationshipSystemTestNextMissingItemData(
			"[GARDENING_TROWEL (41210), WATERING_CAN (41213), SPADE (41155)]",
			"[CAKE (41202), WATER_RUNE (41231), RAKE (41212), FIRE_RUNE (41215)]",
			RandomEventItem.RAKE
		);
		RandomEventItem puzzle4ActualNextMissingItem = relationshipSystem.findMissingItem(puzzle4.getInitialSequenceItems(), puzzle4.getItemChoices());
		Assertions.assertThat(puzzle4ActualNextMissingItem).isEqualTo(puzzle4.getExpectedNextMissingItem());

		RelationshipSystemTestNextMissingItemData puzzle5 = new RelationshipSystemTestNextMissingItemData(
			"[CHEFS_HAT (41203), APRON (41190), CAKE (41202)]",
			"[RAKE (41212), BREAD (41172), BAR (41153), ORE (41170)]",
			RandomEventItem.BREAD
		);
		RandomEventItem puzzle5ActualNextMissingItem = relationshipSystem.findMissingItem(puzzle5.getInitialSequenceItems(), puzzle5.getItemChoices());
		Assertions.assertThat(puzzle5ActualNextMissingItem).isEqualTo(puzzle5.getExpectedNextMissingItem());

		RelationshipSystemTestNextMissingItemData puzzle6 = new RelationshipSystemTestNextMissingItemData(
			"[LONGBOW (41198), ARROWS (41177), CROSSBOW (41146)]",
			"[TROUT_COD_PIKE_SALMON_1 (41204), FIRE_RUNE (41215), SHORT_BOW (41171), LONGSWORD (41150)]",
			RandomEventItem.SHORT_BOW
		);
		RandomEventItem puzzle6ActualNextMissingItem = relationshipSystem.findMissingItem(puzzle6.getInitialSequenceItems(), puzzle6.getItemChoices());
		Assertions.assertThat(puzzle6ActualNextMissingItem).isEqualTo(puzzle6.getExpectedNextMissingItem());

		RelationshipSystemTestNextMissingItemData puzzle7 = new RelationshipSystemTestNextMissingItemData(
			"[BREAD (41172), CAKE (41202), PIE (41205)]",
			"[LEATHER_BOOTS (41220), WATER_RUNE (41231), PIZZA (41185), ARROWS (41177)]",
			RandomEventItem.PIZZA
		);
		RandomEventItem puzzle7ActualNextMissingItem = relationshipSystem.findMissingItem(puzzle7.getInitialSequenceItems(), puzzle7.getItemChoices());
		Assertions.assertThat(puzzle7ActualNextMissingItem).isEqualTo(puzzle7.getExpectedNextMissingItem());
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
