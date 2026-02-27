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

		RelationshipSystemTestMatchingData puzzle8 = new RelationshipSystemTestMatchingData(
			"Tools for warriors who hate ranging or magic.",
			"[CROSSBOW (41146), LOGS (41232), RAKE (41212), INSULATED_BOOTS (27104), BONES (2674), BAR (41153), BATTLE_AXE (41176), NEEDLE (41199), SCIMITAR (41192), BEER (41152), RUNE_OR_ESSENCE (41182), BOTTLE (41175), LONGSWORD (41150), TROUT_COD_PIKE_SALMON_3 (41163), POT (41223)]",
			List.of(RandomEventItem.BATTLE_AXE, RandomEventItem.SCIMITAR, RandomEventItem.LONGSWORD)
		);
		List<RandomEventItem> puzzle8ActualItems = relationshipSystem.findItemsByHint(puzzle8.getHint(), puzzle8.getGivenItems(), 3).subList(0, 3);
		Assertions.assertThat(puzzle8ActualItems).containsExactlyInAnyOrderElementsOf(puzzle8.getExpectedMatchingItems());

		RelationshipSystemTestMatchingData puzzle9 = new RelationshipSystemTestMatchingData(
			"Tools for fighters who hate melee and magic.",
			"[SCIMITAR (41192), LONGBOW (41198), BOTTLE (41175), SHEARS (41227), PIRATE_HAT (41187), ARROWS (41177), CROSSBOW (41146), LONGSWORD (41150), CAKE (41202), GARDENING_TROWEL (41210), JUG (41225), BATTLE_AXE (41176), TINDERBOX (41154), FROG_MASK (27101), COCKTAIL_1 (27097)]",
			List.of(RandomEventItem.LONGBOW, RandomEventItem.ARROWS, RandomEventItem.CROSSBOW)
		);
		List<RandomEventItem> puzzle9ActualItems = relationshipSystem.findItemsByHint(puzzle9.getHint(), puzzle9.getGivenItems(), 3).subList(0, 3);
		Assertions.assertThat(puzzle9ActualItems).containsExactlyInAnyOrderElementsOf(puzzle9.getExpectedMatchingItems());

		RelationshipSystemTestMatchingData puzzle10 = new RelationshipSystemTestMatchingData(
			"Abracadabra! Hocus pocus!",
			"[FIRE_RUNE (41215), CUP_OF_TEA (41162), BOTTLE (41175), WATER_RUNE (41231), BATTLE_AXE (41176), INSULATED_BOOTS (27104), PIRATE_HAT (41187), TROUT_COD_PIKE_SALMON_3 (41163), MED_HELM (41189), TINDERBOX (41154), PIRATE_HOOK (41228), NEEDLE (41199), STAFF (41174), EYE_PATCH (41165), GARDENING_TROWEL (41210)]",
			List.of(RandomEventItem.FIRE_RUNE, RandomEventItem.WATER_RUNE, RandomEventItem.STAFF)
		);
		List<RandomEventItem> puzzle10ActualItems = relationshipSystem.findItemsByHint(puzzle10.getHint(), puzzle10.getGivenItems(), 3).subList(0, 3);
		Assertions.assertThat(puzzle10ActualItems).containsExactlyInAnyOrderElementsOf(puzzle10.getExpectedMatchingItems());

		RelationshipSystemTestMatchingData puzzle11 = new RelationshipSystemTestMatchingData(
			"Ooooh, shiny things! Precious things...",
			"[TINDERBOX (41154), STAFF (41174), HOLY_SYMBOL (41159), WATERING_CAN (41213), FIRE_RUNE (41215), TROUT_COD_PIKE_SALMON_3 (41163), WATER_RUNE (41231), RING (27091), CHEESE (41161), PICKAXE (41194), AXE (41184), NECKLACE (41216), HARPOON (41158), BOTTLE (41175), BONES (2674)]",
			List.of(RandomEventItem.HOLY_SYMBOL, RandomEventItem.RING, RandomEventItem.NECKLACE)
		);
		List<RandomEventItem> puzzle11ActualItems = relationshipSystem.findItemsByHint(puzzle11.getHint(), puzzle11.getGivenItems(), 3).subList(0, 3);
		Assertions.assertThat(puzzle11ActualItems).containsExactlyInAnyOrderElementsOf(puzzle11.getExpectedMatchingItems());

		RelationshipSystemTestMatchingData puzzle12 = new RelationshipSystemTestMatchingData(
			"There is no better feeling than wearing all your bangles, bobbles and fineries.",
			"[BONES (2674), AXE (41184), BOTTLE (41175), CHEESE (41161), RING (27091), HARPOON (41158), WATERING_CAN (41213), STAFF (41174), NECKLACE (41216), TROUT_COD_PIKE_SALMON_3 (41163), FIRE_RUNE (41215), WATER_RUNE (41231), HOLY_SYMBOL (41159), TINDERBOX (41154), PICKAXE (41194)]",
			List.of(RandomEventItem.RING, RandomEventItem.NECKLACE, RandomEventItem.HOLY_SYMBOL)
		);
		List<RandomEventItem> puzzle12ActualItems = relationshipSystem.findItemsByHint(puzzle12.getHint(), puzzle12.getGivenItems(), 3).subList(0, 3);
		Assertions.assertThat(puzzle12ActualItems).containsExactlyInAnyOrderElementsOf(puzzle12.getExpectedMatchingItems());

		// Needs more info - https://github.com/Infinitay/Random-Event-Helper/issues/33#issuecomment-3786983409
		/*
		RelationshipSystemTestMatchingData puzzle13 = new RelationshipSystemTestMatchingData(
			"Piracy is a crime, but go find the pattern anyway.",
			"[CUP_OF_TEA (41162), ORE (41170), KEY (29232), LONGSWORD (41150), WATER_RUNE (41231), STAFF (41174), NECKLACE (41216), PIRATE_HOOK (41228), EYE_PATCH (41165), FIRE_RUNE (41215), BASS (41180), STAFF (41174), BONES (2674), PIE (41205), PIRATE_HAT (41187)]",
			List.of(RandomEventItem.PIRATE_HOOK, RandomEventItem.EYE_PATCH, RandomEventItem.KEY)
		);
		List<RandomEventItem> puzzle13ActualItems = relationshipSystem.findItemsByHint(puzzle13.getHint(), puzzle13.getGivenItems(), 3).subList(0, 3);
		Assertions.assertThat(puzzle13ActualItems).containsExactlyInAnyOrderElementsOf(puzzle13.getExpectedMatchingItems());*/

		RelationshipSystemTestMatchingData puzzle14 = new RelationshipSystemTestMatchingData(
			"I'm feeling dehydrated.",
			"[SHORT_BOW (41171), CUP_OF_TEA (41162), PLANT_POT (41208), SECATEURS (41197), LONGSWORD (41150), TINDERBOX (41154), COCKTAIL_1 (27097), TROUT_COD_PIKE_SALMON_4 (41217), HARPOON (41158), LOGS (41232), BEER (41152), ORE (41170), GEM_WITH_CROSS (41151), TROUT_COD_PIKE_SALMON_3 (41163), BOOK (41181)]",
			List.of(RandomEventItem.BEER, RandomEventItem.CUP_OF_TEA, RandomEventItem.COCKTAIL_1)
		);
		List<RandomEventItem> puzzle14ActualItems = relationshipSystem.findItemsByHint(puzzle14.getHint(), puzzle14.getGivenItems(), 3).subList(0, 3);
		Assertions.assertThat(puzzle14ActualItems).containsExactlyInAnyOrderElementsOf(puzzle14.getExpectedMatchingItems());

		RelationshipSystemTestMatchingData puzzle15 = new RelationshipSystemTestMatchingData(
			"Sea food, catch the food, spot the pattern.",
			"[CANDLE_LANTERN (41229), BEER (41152), BATTLE_AXE (41176), NECKLACE (41216), RAKE (41212), FIGHTER_BOOTS (41160), HARPOON (41158), MED_HELM (41189), RING (27091), ONION (41226), TROUT_COD_PIKE_SALMON_3 (41163), TROUT_COD_PIKE_SALMON_4 (41217), HAMMER (41183), CAPE_OF_LEGENDS (41167), AIR_RUNE (41168)]",
			List.of(RandomEventItem.TROUT_COD_PIKE_SALMON_4, RandomEventItem.TROUT_COD_PIKE_SALMON_3, RandomEventItem.HARPOON)
		);
		List<RandomEventItem> puzzle15ActualItems = relationshipSystem.findItemsByHint(puzzle15.getHint(), puzzle15.getGivenItems(), 3).subList(0, 3);
		Assertions.assertThat(puzzle15ActualItems).containsExactlyInAnyOrderElementsOf(puzzle15.getExpectedMatchingItems());

		RelationshipSystemTestMatchingData puzzle16 = new RelationshipSystemTestMatchingData(
			"Piracy is a crime, but go find the pattern anyway.",
			"[LONGSWORD (41150), BONES (2674), STAFF (41174), KEY (29232), FIRE_RUNE (41215), PIRATE_HOOK (41228), BASS (41180), PIRATE_HAT (41187), CUP_OF_TEA (41162), EYE_PATCH (41165), PIE (41205), NECKLACE (41216), WATER_RUNE (41231), STAFF (41174), ORE (41170)]",
			List.of(RandomEventItem.PIRATE_HAT, RandomEventItem.PIRATE_HOOK, RandomEventItem.EYE_PATCH)
		);
		List<RandomEventItem> puzzle16ActualItems = relationshipSystem.findItemsByHint(puzzle16.getHint(), puzzle16.getGivenItems(), 3).subList(0, 3);
		Assertions.assertThat(puzzle16ActualItems).containsExactlyInAnyOrderElementsOf(puzzle16.getExpectedMatchingItems());

		RelationshipSystemTestMatchingData puzzle17 = new RelationshipSystemTestMatchingData(
			"This pattern is igniting my brain.",
			"[TROUT_COD_PIKE_SALMON_3, CANDLE_LANTERN, CUP_OF_TEA, LONGSWORD, LEDERHOSEN_HAT, TUNA, WATERING_CAN, LOGS, GEM_WITH_CROSS, HARPOON, PICKAXE, TINDERBOX, BOTTLE, KEY,HAMMER]",
			List.of(RandomEventItem.CANDLE_LANTERN, RandomEventItem.LOGS, RandomEventItem.TINDERBOX)
		);
		List<RandomEventItem> puzzle17ActualItems = relationshipSystem.findItemsByHint(puzzle17.getHint(), puzzle17.getGivenItems(), 3).subList(0, 3);
		Assertions.assertThat(puzzle17ActualItems).containsExactlyInAnyOrderElementsOf(puzzle17.getExpectedMatchingItems());
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
			"[HAMMER (41183), RING (27091), BEER (41152), HERRING_OR_MACKEREL (41193)]",
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

		RelationshipSystemTestNextMissingItemData puzzle8 = new RelationshipSystemTestNextMissingItemData(
			"[LONGBOW (41198), ARROWS (41177), CROSSBOW (41146)]",
			"[LONGSWORD (41150), FIRE_RUNE (41215), SHORT_BOW (41171), TROUT_COD_PIKE_SALMON_1 (41204)]",
			RandomEventItem.SHORT_BOW
		);
		RandomEventItem puzzle8ActualNextMissingItem = relationshipSystem.findMissingItem(puzzle8.getInitialSequenceItems(), puzzle8.getItemChoices());
		Assertions.assertThat(puzzle8ActualNextMissingItem).isEqualTo(puzzle8.getExpectedNextMissingItem());

		RelationshipSystemTestNextMissingItemData puzzle9 = new RelationshipSystemTestNextMissingItemData(
			"[CUP_OF_TEA (41162), COCKTAIL_2 (28421), GIN_OR_RUM (41219)]",
			"[LEDERHOSEN_HAT (41164), PIE (41205), BEER (41152), SHORT_BOW (41171)]",
			RandomEventItem.BEER
		);
		RandomEventItem puzzle9ActualNextMissingItem = relationshipSystem.findMissingItem(puzzle9.getInitialSequenceItems(), puzzle9.getItemChoices());
		Assertions.assertThat(puzzle9ActualNextMissingItem).isEqualTo(puzzle9.getExpectedNextMissingItem());

		RelationshipSystemTestNextMissingItemData puzzle10 = new RelationshipSystemTestNextMissingItemData(
			"[KITESHIELD (41200), WOODEN_SHIELD (41221), SQUARE_SHIELD_1 (41188)]",
			"[SQUARE_SHIELD_2 (41169), COCKTAIL_SHAKER (27096), ORE (41170), CAKE (41202)]",
			RandomEventItem.SQUARE_SHIELD_2
		);
		RandomEventItem puzzle10ActualNextMissingItem = relationshipSystem.findMissingItem(puzzle10.getInitialSequenceItems(), puzzle10.getItemChoices());
		Assertions.assertThat(puzzle10ActualNextMissingItem).isEqualTo(puzzle10.getExpectedNextMissingItem());

		RelationshipSystemTestNextMissingItemData puzzle11 = new RelationshipSystemTestNextMissingItemData(
			"[TINDERBOX (41154), LOGS (41232), CANDLE_ON_STAND (27102)]",
			"[CANDLE_LANTERN (41229), PICKAXE (41194), PINEAPPLE (41214), ARROWS (41177)]",
			RandomEventItem.CANDLE_LANTERN
		);
		RandomEventItem puzzle11ActualNextMissingItem = relationshipSystem.findMissingItem(puzzle11.getInitialSequenceItems(), puzzle11.getItemChoices());
		Assertions.assertThat(puzzle11ActualNextMissingItem).isEqualTo(puzzle11.getExpectedNextMissingItem());

		RelationshipSystemTestNextMissingItemData puzzle12 = new RelationshipSystemTestNextMissingItemData(
			"[PLATEBODY (27094), BAR (41153), ORE (41170)]",
			"[LONGBOW (41198), HERRING_OR_MACKEREL (41193), CAPE_OF_LEGENDS (41167), PICKAXE (41194)]",
			RandomEventItem.PICKAXE
		);
		RandomEventItem puzzle12ActualNextMissingItem = relationshipSystem.findMissingItem(puzzle12.getInitialSequenceItems(), puzzle12.getItemChoices());
		Assertions.assertThat(puzzle12ActualNextMissingItem).isEqualTo(puzzle12.getExpectedNextMissingItem());

		RelationshipSystemTestNextMissingItemData puzzle13 = new RelationshipSystemTestNextMissingItemData(
			"[BANANA (41222), STRAWBERRY (41230), GRAPES (41207)]",
			"[PINEAPPLE (41214), ARROWS (41177), RING (27091), HAMMER (41183)]",
			RandomEventItem.PINEAPPLE
		);
		RandomEventItem puzzle13ActualNextMissingItem = relationshipSystem.findMissingItem(puzzle13.getInitialSequenceItems(), puzzle13.getItemChoices());
		Assertions.assertThat(puzzle13ActualNextMissingItem).isEqualTo(puzzle13.getExpectedNextMissingItem());

		RelationshipSystemTestNextMissingItemData puzzle14 = new RelationshipSystemTestNextMissingItemData(
			"[TUNA (41209), HARPOON (41158), TROUT_COD_PIKE_SALMON_2 (41206)]",
			"[SHARK (41166), CAKE (41202), COCKTAIL_1 (27097), LONGSWORD (41150)]",
			RandomEventItem.SHARK
		);
		RandomEventItem puzzle14ActualNextMissingItem = relationshipSystem.findMissingItem(puzzle14.getInitialSequenceItems(), puzzle14.getItemChoices());
		Assertions.assertThat(puzzle14ActualNextMissingItem).isEqualTo(puzzle14.getExpectedNextMissingItem());

		RelationshipSystemTestNextMissingItemData puzzle15 = new RelationshipSystemTestNextMissingItemData(
			"[BANANA (41222), ONION (41226), PINEAPPLE (41214)]",
			"[PICKAXE (41194), TROUT_COD_PIKE_SALMON_1 (41204), WATERMELON_SLICE (41156), FEATHER (41224)]",
			RandomEventItem.WATERMELON_SLICE
		);
		RandomEventItem puzzle15ActualNextMissingItem = relationshipSystem.findMissingItem(puzzle15.getInitialSequenceItems(), puzzle15.getItemChoices());
		Assertions.assertThat(puzzle15ActualNextMissingItem).isEqualTo(puzzle15.getExpectedNextMissingItem());

		RelationshipSystemTestNextMissingItemData puzzle16 = new RelationshipSystemTestNextMissingItemData(
			"[SCIMITAR (41192), MACE (41211), BATTLE_AXE (41176)]",
			"[LOGS (41232), SPADE (41155), LONGSWORD (41150), CUP_OF_TEA (41162)]",
			RandomEventItem.LONGSWORD
		);
		RandomEventItem puzzle16ActualNextMissingItem = relationshipSystem.findMissingItem(puzzle16.getInitialSequenceItems(), puzzle16.getItemChoices());
		Assertions.assertThat(puzzle16ActualNextMissingItem).isEqualTo(puzzle16.getExpectedNextMissingItem());

		RelationshipSystemTestNextMissingItemData puzzle17 = new RelationshipSystemTestNextMissingItemData(
			"[SHRIMP (41147), FISH (41173), HERRING_OR_MACKEREL (41193)]",
			"[PINEAPPLE (41214), LEATHER_BOOTS (41220), SHARK (41166), EARTH_RUNE (41157)]",
			RandomEventItem.SHARK
		);
		RandomEventItem puzzle17ActualNextMissingItem = relationshipSystem.findMissingItem(puzzle17.getInitialSequenceItems(), puzzle17.getItemChoices());
		Assertions.assertThat(puzzle17ActualNextMissingItem).isEqualTo(puzzle17.getExpectedNextMissingItem());
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
