package randomeventhelper.randomevents.surpriseexam;

public enum RelationshipType
{
	// Production chains
	COMBAT_ECOSYSTEM("combat, prayer, bones, weapons, battle, fighting"),
	FISHING_TO_COOKING("fishing, cooking, harpoon, fish, tuna, shark, food"),
	MINING_SMITHING_CRAFTING("mining, smithing, crafting, pickaxe, ore, bar, hammer"),
	WOODCUTTING_FLETCHING("woodcutting, fletching, axe, logs, bow, arrows, feather"),
	FARMING_ECOSYSTEM("farming, agriculture, rake, seeds, crops, harvest, plants"),
	COOKING_PRODUCTION("cooking, food, chef, kitchen, bread, cake, meals"),
	ALCOHOL_PRODUCTION("alcohol, brewing, cocktail, beer, rum, gin, drinks"),
	MAGIC_RUNECRAFTING("magic, runecrafting, runes, essence, spells, staff, abracadabra, hocus pocus"),
	JEWELRY_CRAFTING("jewelry, gems, necklace, ring, crafting, status, shiny, precious"),
	LIGHT_FIRE_SYSTEM("fire, light, candle, lantern, tinderbox, illumination"),
	CONTAINER_STORAGE("container, storage, bottle, jug, pot, holding"),

	// Thematic groups
	PIRATE_THEME("pirate, sea, nautical, hook, eyepatch, boots, hat, yarr, crime, strange"),
	ENTERTAINMENT_THEME("entertainment, performance, jester, mime, mask, fun, clown, fool, mask, face, strange"),
	PROFESSIONAL_THEME("profession, work, chef, trade, job, occupation"),

	// Equipment categories
	MELEE_WEAPONS("melee, sword, axe, mace, scimitar, close, combat, sharp"),
	RANGED_WEAPONS("ranged, bow, crossbow, arrows, ammunition, distance, sharp"),
	MAGIC_RUNES("runes, elemental, air, earth, fire, water, magic, abracadabra, hocus pocus"),
	HEAD_ARMOR("head, helmet, hat, protection, headwear, skull, mask, headgear"),
	BODY_ARMOR("body, chest, torso, platebody, apron, protection"),
	LEG_ARMOR("legs, platelegs, protection, lower, body"),
	FOOT_ARMOR("feet, boots, footwear, walking, protection"),
	SHIELDS("shield, defense, blocking, protection, guard"),
	MELEE_GEAR("melee, sword, scimitar, axe, mace, combat, sharp, helmet, platebody, platelegs, shield"),
	JEWELRY_ACCESSORIES("jewelry, accessories, necklace, ring, status, shiny, precious"),
	FACE_ACCESSORIES("face, mask, eyepatch, hook, covering, facial"),

	// Skill-based groupings
	ALL_SKILLING_TOOLS("tools, skills, gathering, resources, equipment, utility"),
	MINING_TOOLS("mining, pickaxe, ore, rocks, gems, underground"),
	FISHING_TOOLS("fishing, harpoon, fish, water, catching, sea, sea food"),
	FARMING_TOOLS("farming, gardening, rake, spade, plants, agriculture"),
	CRAFTING_TOOLS("crafting, needle, thread, making, creating, tailoring"),

	// Food categories
	FISH("fish, raw, uncooked, sea, food, sea food, seafood, fishing, water"),
	FRUITS("fruits, berries, fresh, healthy, vitamins, nature"),
	BAKING_FOOD("cooked, food, meals, prepared, baked, ready"),
	ALCOHOLIC_DRINKS("alcohol, drinks, beer, spirits, intoxicating, beverages, thirsty"),
	DRINKS("drink, beverage, refreshing, hydrate, quench, thirsty, liquid, sip"),

	// Functional groupings
	COMBAT_CONSUMABLES("combat, consumable, potion, bones, prayer, aid"),
	LIGHT_SOURCES("light, illumination, candle, lantern, brightness, glow"),
	RESOURCE_MATERIALS("resources, materials, raw, crafting, essence, components");

	private final String keywords;

	RelationshipType(String keywords)
	{
		this.keywords = keywords;
	}

	public String getKeywords()
	{
		return keywords;
	}

	public String[] getKeywordArray()
	{
		return keywords.split(", ");
	}
}
