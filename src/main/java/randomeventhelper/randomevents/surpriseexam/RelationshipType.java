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
	MAGIC_RUNECRAFTING("magic, runecrafting, runes, essence, spells, staff"),
	JEWELRY_CRAFTING("jewelry, gems, necklace, ring, crafting, status"),
	LIGHT_FIRE_SYSTEM("fire, light, candle, lantern, tinderbox, illumination"),
	CONTAINER_STORAGE("container, storage, bottle, jug, pot, holding"),

	// Thematic groups
	PIRATE_THEME("pirate, sea, nautical, hook, eyepatch, boots, hat, yarr, crime"),
	ENTERTAINMENT_THEME("entertainment, performance, jester, mime, mask, fun, clown, fool, mask, face"),
	PROFESSIONAL_THEME("profession, work, chef, trade, job, occupation"),

	// Equipment categories
	MELEE_WEAPONS("melee, sword, axe, mace, scimitar, close, combat, sharp"),
	RANGED_WEAPONS("ranged, bow, crossbow, arrows, ammunition, distance, sharp"),
	MAGIC_RUNES("runes, elemental, air, earth, fire, water, magic"),
	HEAD_ARMOR("head, helmet, hat, protection, headwear, skull, mask"),
	BODY_ARMOR("body, chest, torso, platebody, apron, protection"),
	LEG_ARMOR("legs, platelegs, protection, lower, body"),
	FOOT_ARMOR("feet, boots, footwear, walking, protection"),
	SHIELDS("shield, defense, blocking, protection, guard"),
	JEWELRY_ACCESSORIES("jewelry, accessories, necklace, ring, cape, status"),
	FACE_ACCESSORIES("face, mask, eyepatch, hook, covering, facial"),

	// Skill-based groupings
	ALL_SKILLING_TOOLS("tools, skills, gathering, resources, equipment, utility"),
	MINING_TOOLS("mining, pickaxe, ore, rocks, gems, underground"),
	FISHING_TOOLS("fishing, harpoon, fish, water, catching, sea"),
	FARMING_TOOLS("farming, gardening, rake, spade, plants, agriculture"),
	CRAFTING_TOOLS("crafting, needle, thread, making, creating, tailoring"),

	// Food categories
	RAW_FISH("fish, raw, uncooked, seafood, fishing, water"),
	FRUITS("fruits, berries, fresh, healthy, vitamins, nature"),
	COOKED_FOODS("cooked, food, meals, prepared, baked, ready"),
	ALCOHOLIC_DRINKS("alcohol, drinks, beer, spirits, intoxicating, beverages"),

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
