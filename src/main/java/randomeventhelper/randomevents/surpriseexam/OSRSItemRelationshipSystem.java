package randomeventhelper.randomevents.surpriseexam;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.JaroWinklerDistance;

/**
 * Comprehensive OSRS Item Relationship System
 * Supports both hint-based selection and missing item detection
 */
@Slf4j
@Singleton
public class OSRSItemRelationshipSystem
{
	// Relationship mappings and similarity calculators
	private final Map<RelationshipType, Set<RandomEventItem>> relationships;
	private final JaroWinklerDistance jaroWinklerDistance;

	// Similarity thresholds
	private static final double EXACT_MATCH_THRESHOLD = 1.0;
	private static final double HIGH_SIMILARITY_THRESHOLD = 0.75;
	private static final double MEDIUM_SIMILARITY_THRESHOLD = 0.5;
	private static final double LOW_SIMILARITY_THRESHOLD = 0.25;

	// Riddle analysis patterns and keywords
	private final Set<String> STOP_WORDS = Set.of(
		"the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by",
		"i", "you", "he", "she", "it", "we", "they", "am", "is", "are", "was", "were", "be", "been", "being",
		"have", "has", "had", "do", "does", "did", "will", "would", "could", "should", "may", "might", "can",
		"like", "feel", "against", "than", "take", "get", "go", "come", "see", "know", "think", "say", "tell",
		"as", "pattern", "relation", "relationship", "item", "items", "match", "find", "solve", "answer", "question",
		"answers", "puzzle", "riddle", "clue", "hint"
	);

	// Context clues that might indicate certain themes (but don't hard-code specific riddles)
	private final Map<String, Set<String>> CONTEXT_CLUES = Map.ofEntries(
		Map.entry("water", Set.of("fishing", "fish", "seafood", "ocean", "sea")),
		Map.entry("sea", Set.of("fishing", "fish", "pirate", "nautical", "ocean")),
		Map.entry("dragon", Set.of("combat", "weapon", "battle", "fighting")),
		Map.entry("sword", Set.of("melee", "weapon", "combat", "blade")),
		Map.entry("bow", Set.of("range", "weapon", "arrow", "ammunition")),
		Map.entry("spell", Set.of("magic", "runes", "staff", "runecrafting")),
		Map.entry("food", Set.of("cooking", "chef", "meal", "kitchen")),
		Map.entry("grow", Set.of("farming", "plants", "crops", "agriculture")),
		Map.entry("mine", Set.of("mining", "pickaxe", "ore", "rocks")),
		Map.entry("protection", Set.of("armor", "helmet", "shield", "defense")),
		Map.entry("drink", Set.of("alcohol", "beverage", "beer", "cocktail", "thirsty")),
		Map.entry("light", Set.of("fire", "candle", "lantern", "tinder", "illuminate")),
		Map.entry("jewel", Set.of("jewelry", "gem", "necklace", "ring", "crafting", "amulet")),
		Map.entry("pirate", Set.of("sea", "yarr", "piracy", "treasure", "chest", "loot", "gold", "crime")),
		Map.entry("mask", Set.of("hide", "face", "disguise", "theft", "rob", "crime", "unrecognizable", "undercover", "conceal"))
	);

	// Synonym mapping for better word matching
	private final Map<String, Set<String>> SYNONYMS = Map.of(
		"weapon", Set.of("sword", "axe", "mace", "bow", "staff", "blade", "arm", "armament"),
		"fish", Set.of("seafood", "catch", "marine", "aquatic", "ocean", "sea"),
		"food", Set.of("meal", "dish", "cuisine", "edible", "consumable", "nourishment"),
		"armor", Set.of("protection", "gear", "equipment", "defense", "guard"),
		"magic", Set.of("spell", "enchantment", "sorcery", "wizardry", "mystical"),
		"pirate", Set.of("buccaneer", "seafarer", "mariner", "sailor", "nautical"),
		"drink", Set.of("beverage", "liquid", "fluid", "potion", "brew", "sip"),
		"tool", Set.of("equipment", "implement", "instrument", "utility", "gear"),
		"bow", Set.of("archery", "ranged", "range", "arrow", "archer", "crossbow")
	);

	public OSRSItemRelationshipSystem()
	{
		this.relationships = initializeRelationships();
		this.jaroWinklerDistance = new JaroWinklerDistance();
	}

	private Map<RelationshipType, Set<RandomEventItem>> initializeRelationships()
	{
		Map<RelationshipType, Set<RandomEventItem>> map = new EnumMap<>(RelationshipType.class);

		// Production chains
		map.put(RelationshipType.COMBAT_ECOSYSTEM, Set.of(
			RandomEventItem.BONES, RandomEventItem.BATTLE_AXE, RandomEventItem.LONGSWORD,
			RandomEventItem.MACE, RandomEventItem.SCIMITAR, RandomEventItem.CROSSBOW,
			RandomEventItem.LONGBOW, RandomEventItem.SHORT_BOW, RandomEventItem.ARROWS,
			RandomEventItem.STAFF, RandomEventItem.POTION
		));

		map.put(RelationshipType.FISHING_TO_COOKING, Set.of(
			RandomEventItem.HARPOON, RandomEventItem.TUNA, RandomEventItem.SHARK,
			RandomEventItem.BASS, RandomEventItem.TROUT_COD_PIKE_SALMON_1,
			RandomEventItem.TROUT_COD_PIKE_SALMON_2, RandomEventItem.TROUT_COD_PIKE_SALMON_3,
			RandomEventItem.TROUT_COD_PIKE_SALMON_4, RandomEventItem.HERRING_OR_MACKEREL,
			RandomEventItem.SHRIMP, RandomEventItem.FISH
		));

		map.put(RelationshipType.MINING_SMITHING_CRAFTING, Set.of(
			RandomEventItem.PICKAXE, RandomEventItem.ORE, RandomEventItem.BAR,
			RandomEventItem.GEM_WITH_CROSS, RandomEventItem.HAMMER
		));

		map.put(RelationshipType.WOODCUTTING_FLETCHING, Set.of(
			RandomEventItem.AXE, RandomEventItem.LOGS, RandomEventItem.LONGBOW,
			RandomEventItem.SHORT_BOW, RandomEventItem.ARROWS, RandomEventItem.FEATHER
		));

		map.put(RelationshipType.FARMING_ECOSYSTEM, Set.of(
			RandomEventItem.RAKE, RandomEventItem.GARDENING_TROWEL, RandomEventItem.WATERING_CAN,
			RandomEventItem.SECATEURS, RandomEventItem.SPADE, RandomEventItem.PLANT_POT,
			RandomEventItem.SHEARS, RandomEventItem.BANANA, RandomEventItem.GRAPES,
			RandomEventItem.PINEAPPLE, RandomEventItem.STRAWBERRY, RandomEventItem.WATERMELON_SLICE,
			RandomEventItem.ONION
		));

		map.put(RelationshipType.COOKING_PRODUCTION, Set.of(
			RandomEventItem.KITCHEN_KNIFE, RandomEventItem.POT, RandomEventItem.TUNA,
			RandomEventItem.BREAD, RandomEventItem.CAKE, RandomEventItem.CHEESE,
			RandomEventItem.PIE, RandomEventItem.PIZZA, RandomEventItem.BANANA,
			RandomEventItem.GRAPES, RandomEventItem.ONION, RandomEventItem.CHEFS_HAT,
			RandomEventItem.APRON
		));

		map.put(RelationshipType.ALCOHOL_PRODUCTION, Set.of(
			RandomEventItem.GRAPES, RandomEventItem.BEER, RandomEventItem.GIN_OR_RUM,
			RandomEventItem.COCKTAIL_1, RandomEventItem.COCKTAIL_2, RandomEventItem.COCKTAIL_SHAKER,
			RandomEventItem.BOTTLE, RandomEventItem.JUG
		));

		map.put(RelationshipType.MAGIC_RUNECRAFTING, Set.of(
			RandomEventItem.RUNE_OR_ESSENCE, RandomEventItem.AIR_RUNE, RandomEventItem.EARTH_RUNE,
			RandomEventItem.FIRE_RUNE, RandomEventItem.WATER_RUNE, RandomEventItem.STAFF,
			RandomEventItem.BOOK, RandomEventItem.TIARA
		));

		map.put(RelationshipType.JEWELRY_CRAFTING, Set.of(
			RandomEventItem.GEM_WITH_CROSS, RandomEventItem.NECKLACE, RandomEventItem.RING,
			RandomEventItem.HOLY_SYMBOL, RandomEventItem.CAPE_OF_LEGENDS, RandomEventItem.TIARA
		));

		map.put(RelationshipType.LIGHT_FIRE_SYSTEM, Set.of(
			RandomEventItem.TINDERBOX, RandomEventItem.CANDLE_ON_STAND, RandomEventItem.CANDLE_LANTERN,
			RandomEventItem.LOGS
		));

		map.put(RelationshipType.CONTAINER_STORAGE, Set.of(
			RandomEventItem.BOTTLE, RandomEventItem.JUG, RandomEventItem.POT,
			RandomEventItem.PLANT_POT, RandomEventItem.COCKTAIL_SHAKER
		));

		// Thematic groups
		map.put(RelationshipType.PIRATE_THEME, Set.of(
			RandomEventItem.PIRATE_HAT, RandomEventItem.PIRATE_BOOTS, RandomEventItem.PIRATE_HOOK,
			RandomEventItem.EYE_PATCH, RandomEventItem.KEY, RandomEventItem.HIGHWAYMAN_MASK
		));

		map.put(RelationshipType.ENTERTAINMENT_THEME, Set.of(
			RandomEventItem.JESTER_HAT, RandomEventItem.MIME_MASK, RandomEventItem.FROG_MASK, RandomEventItem.HIGHWAYMAN_MASK
		));

		map.put(RelationshipType.PROFESSIONAL_THEME, Set.of(
			RandomEventItem.CHEFS_HAT, RandomEventItem.APRON, RandomEventItem.LEDERHOSEN_HAT
		));

		// Equipment categories
		map.put(RelationshipType.MELEE_WEAPONS, Set.of(
			RandomEventItem.BATTLE_AXE, RandomEventItem.LONGSWORD, RandomEventItem.MACE,
			RandomEventItem.SCIMITAR
		));

		map.put(RelationshipType.RANGED_WEAPONS, Set.of(
			RandomEventItem.CROSSBOW, RandomEventItem.LONGBOW, RandomEventItem.SHORT_BOW,
			RandomEventItem.ARROWS
		));

		map.put(RelationshipType.MAGIC_RUNES, Set.of(
			RandomEventItem.AIR_RUNE, RandomEventItem.EARTH_RUNE, RandomEventItem.FIRE_RUNE,
			RandomEventItem.WATER_RUNE
		));

		map.put(RelationshipType.HEAD_ARMOR, Set.of(
			RandomEventItem.FULL_HELM, RandomEventItem.MED_HELM, RandomEventItem.CHEFS_HAT,
			RandomEventItem.PIRATE_HAT, RandomEventItem.JESTER_HAT, RandomEventItem.LEDERHOSEN_HAT,
			RandomEventItem.TIARA, RandomEventItem.HIGHWAYMAN_MASK, RandomEventItem.MIME_MASK, RandomEventItem.FROG_MASK
		));

		map.put(RelationshipType.BODY_ARMOR, Set.of(
			RandomEventItem.PLATEBODY, RandomEventItem.APRON
		));

		map.put(RelationshipType.LEG_ARMOR, Set.of(
			RandomEventItem.PLATELEGS
		));

		map.put(RelationshipType.FOOT_ARMOR, Set.of(
			RandomEventItem.INSULATED_BOOTS, RandomEventItem.PIRATE_BOOTS, RandomEventItem.LEATHER_BOOTS,
			RandomEventItem.FIGHTER_BOOTS
		));

		map.put(RelationshipType.SHIELDS, Set.of(
			RandomEventItem.SQUARE_SHIELD_1, RandomEventItem.SQUARE_SHIELD_2,
			RandomEventItem.KITESHIELD, RandomEventItem.WOODEN_SHIELD
		));

		map.put(RelationshipType.MELEE_GEAR, Set.of(
			RandomEventItem.BATTLE_AXE, RandomEventItem.LONGSWORD, RandomEventItem.MACE,
			RandomEventItem.SCIMITAR, RandomEventItem.FULL_HELM, RandomEventItem.MED_HELM, RandomEventItem.PLATEBODY,
			RandomEventItem.PLATELEGS, RandomEventItem.SQUARE_SHIELD_1, RandomEventItem.SQUARE_SHIELD_2,
			RandomEventItem.KITESHIELD, RandomEventItem.WOODEN_SHIELD
		));

		map.put(RelationshipType.JEWELRY_ACCESSORIES, Set.of(
			RandomEventItem.NECKLACE, RandomEventItem.RING, RandomEventItem.HOLY_SYMBOL,
			RandomEventItem.CAPE_OF_LEGENDS
		));

		map.put(RelationshipType.FACE_ACCESSORIES, Set.of(
			RandomEventItem.EYE_PATCH, RandomEventItem.FROG_MASK, RandomEventItem.HIGHWAYMAN_MASK,
			RandomEventItem.MIME_MASK, RandomEventItem.PIRATE_HOOK
		));

		// Skill-based groupings
		map.put(RelationshipType.ALL_SKILLING_TOOLS, Set.of(
			RandomEventItem.PICKAXE, RandomEventItem.AXE, RandomEventItem.HARPOON,
			RandomEventItem.NEEDLE, RandomEventItem.THREAD, RandomEventItem.HAMMER,
			RandomEventItem.TINDERBOX, RandomEventItem.RAKE, RandomEventItem.SECATEURS,
			RandomEventItem.SHEARS, RandomEventItem.GARDENING_TROWEL, RandomEventItem.SPADE,
			RandomEventItem.WATERING_CAN, RandomEventItem.KITCHEN_KNIFE
		));

		map.put(RelationshipType.MINING_TOOLS, Set.of(
			RandomEventItem.PICKAXE, RandomEventItem.ORE, RandomEventItem.BAR,
			RandomEventItem.GEM_WITH_CROSS
		));

		map.put(RelationshipType.FISHING_TOOLS, Set.of(
			RandomEventItem.HARPOON, RandomEventItem.TUNA, RandomEventItem.SHARK,
			RandomEventItem.BASS, RandomEventItem.SHRIMP
		));

		map.put(RelationshipType.FARMING_TOOLS, Set.of(
			RandomEventItem.RAKE, RandomEventItem.SECATEURS, RandomEventItem.SHEARS,
			RandomEventItem.GARDENING_TROWEL, RandomEventItem.SPADE, RandomEventItem.WATERING_CAN,
			RandomEventItem.PLANT_POT
		));

		map.put(RelationshipType.CRAFTING_TOOLS, Set.of(
			RandomEventItem.NEEDLE, RandomEventItem.THREAD, RandomEventItem.HAMMER
		));

		// Food categories
		map.put(RelationshipType.FISH, Set.of(
			RandomEventItem.TUNA, RandomEventItem.TROUT_COD_PIKE_SALMON_1,
			RandomEventItem.TROUT_COD_PIKE_SALMON_2, RandomEventItem.TROUT_COD_PIKE_SALMON_3,
			RandomEventItem.TROUT_COD_PIKE_SALMON_4, RandomEventItem.HERRING_OR_MACKEREL,
			RandomEventItem.SHARK, RandomEventItem.SHRIMP, RandomEventItem.BASS, RandomEventItem.FISH
		));

		map.put(RelationshipType.FRUITS, Set.of(
			RandomEventItem.BANANA, RandomEventItem.GRAPES, RandomEventItem.PINEAPPLE,
			RandomEventItem.STRAWBERRY, RandomEventItem.WATERMELON_SLICE
		));

		map.put(RelationshipType.BAKING_FOOD, Set.of(
			RandomEventItem.BREAD, RandomEventItem.CAKE, RandomEventItem.CHEESE,
			RandomEventItem.PIE, RandomEventItem.PIZZA
		));

		map.put(RelationshipType.ALCOHOLIC_DRINKS, Set.of(
			RandomEventItem.BEER, RandomEventItem.GIN_OR_RUM, RandomEventItem.COCKTAIL_1,
			RandomEventItem.COCKTAIL_2
		));

		map.put(RelationshipType.DRINKS, Set.of(
			RandomEventItem.BEER, RandomEventItem.GIN_OR_RUM, RandomEventItem.COCKTAIL_1,
			RandomEventItem.COCKTAIL_2, RandomEventItem.CUP_OF_TEA, RandomEventItem.BOTTLE, RandomEventItem.JUG,
			RandomEventItem.POTION
		));

		// Functional groupings
		map.put(RelationshipType.COMBAT_CONSUMABLES, Set.of(
			RandomEventItem.BONES, RandomEventItem.POTION
		));

		map.put(RelationshipType.LIGHT_SOURCES, Set.of(
			RandomEventItem.CANDLE_ON_STAND, RandomEventItem.CANDLE_LANTERN
		));

		map.put(RelationshipType.RESOURCE_MATERIALS, Set.of(
			RandomEventItem.RUNE_OR_ESSENCE, RandomEventItem.ORE, RandomEventItem.LOGS,
			RandomEventItem.FEATHER, RandomEventItem.THREAD
		));

		return map;
	}

	/**
	 * Problem 1: Given a riddle/hint and list of items, find items that match the relationship
	 * Analyzes riddles using pattern matching, keyword extraction, and similarity scoring
	 *
	 * @param riddle         The relationship riddle/hint
	 * @param availableItems List of 15 items to choose from
	 * @param minItems       Minimum number of items to select (default 3)
	 * @return List of items that match the riddle, sorted by relevance
	 */
	public List<RandomEventItem> findItemsByHint(String riddle, List<RandomEventItem> availableItems, int minItems)
	{
		return analyzeRiddleAndFindItems(riddle, availableItems, minItems);
	}

	/**
	 * Problem 2: Given 3 items, find the 4th item that completes the relationship
	 *
	 * @param knownItems The 3 known items
	 * @param candidates The 4 potential answers
	 * @return The item that best completes the relationship, or null if none found
	 */
	public RandomEventItem findMissingItem(List<RandomEventItem> knownItems, List<RandomEventItem> candidates)
	{
		if (knownItems.size() != 3)
		{
			throw new IllegalArgumentException("Expected exactly 3 known items");
		}

		// Try each candidate and see which one creates the best relationship
		for (RandomEventItem candidate : candidates)
		{
			List<RandomEventItem> testGroup = new ArrayList<>(knownItems);
			testGroup.add(candidate);

			// Check if this combination matches any relationship
			for (Map.Entry<RelationshipType, Set<RandomEventItem>> entry : relationships.entrySet())
			{
				Set<RandomEventItem> relationshipItems = entry.getValue();

				// Count how many items from our test group are in this relationship
				long matches = testGroup.stream()
					.filter(relationshipItems::contains)
					.count();

				// If all 4 items are in the relationship, this is likely the answer
				if (matches == 4)
				{
					return candidate;
				}
			}
		}

		// If no perfect match, try partial matching
		return findMissingItemByPartialMatch(knownItems, candidates);
	}

	/**
	 * Get all relationships that contain a specific item
	 */
	public List<RelationshipType> getRelationshipsForItem(RandomEventItem item)
	{
		return relationships.entrySet().stream()
			.filter(entry -> entry.getValue().contains(item))
			.map(Map.Entry::getKey)
			.collect(Collectors.toList());
	}

	/**
	 * Get all items in a specific relationship
	 */
	public Set<RandomEventItem> getItemsInRelationship(RelationshipType relationshipType)
	{
		return relationships.getOrDefault(relationshipType, Set.of());
	}

	/**
	 * Find relationship types that contain all given items
	 */
	public List<RelationshipType> findSharedRelationships(List<RandomEventItem> items)
	{
		return relationships.entrySet().stream()
			.filter(entry -> entry.getValue().containsAll(items))
			.map(Map.Entry::getKey)
			.collect(Collectors.toList());
	}

	// Helper methods

	/**
	 * Main riddle analysis and item matching algorithm
	 * Uses keyword extraction, context analysis, and similarity scoring - no hard-coded patterns
	 */
	private List<RandomEventItem> analyzeRiddleAndFindItems(String riddle, List<RandomEventItem> availableItems, int minItems)
	{
		Map<RelationshipType, Double> relationshipScores = new HashMap<>();

		// Step 1: Extract all meaningful words from riddle (remove stop words)
		Set<String> riddleKeywords = extractRiddleKeywords(riddle);

		// Step 2: Expand keywords using context clues and synonyms
		Set<String> expandedKeywords = expandWithContextAndSynonyms(riddleKeywords);

		// Step 3: Score each relationship type based on keyword matches
		for (RelationshipType type : RelationshipType.values())
		{
			double score = calculateRelationshipScore(type, expandedKeywords, riddleKeywords);
			if (score > 0)
			{
				relationshipScores.put(type, score);
			}
		}

		// Step 4: Score available items based on their relationship scores
		Map<RandomEventItem, Double> itemScores = new HashMap<>();

		for (RandomEventItem item : availableItems)
		{
			double maxItemScore = 0.0;
			List<RelationshipType> itemRelationships = getRelationshipsForItem(item);

			for (RelationshipType relationship : itemRelationships)
			{
				Double relationshipScore = relationshipScores.get(relationship);
				if (relationshipScore != null)
				{
					maxItemScore = Math.max(maxItemScore, relationshipScore);
				}
			}

			if (maxItemScore > 0)
			{
				itemScores.put(item, maxItemScore);
			}
		}

		// Step 5: Return top scoring items
		List<RandomEventItem> result = itemScores.entrySet().stream()
			.sorted(Map.Entry.<RandomEventItem, Double>comparingByValue().reversed())
			.map(Map.Entry::getKey)
			.collect(Collectors.toList());

		// Ensure we return at least minItems if available
		int returnCount = Math.max(minItems, Math.min(result.size(), 8));
		return result.subList(0, Math.min(returnCount, result.size()));
	}

	/**
	 * Expand keywords using both context clues and synonyms dynamically
	 */
	private Set<String> expandWithContextAndSynonyms(Set<String> keywords)
	{
		Set<String> expandedKeywords = new HashSet<>(keywords);

		// Add context-based expansions
		for (String keyword : keywords)
		{
			Set<String> contextWords = CONTEXT_CLUES.get(keyword.toLowerCase());
			if (contextWords != null)
			{
				expandedKeywords.addAll(contextWords);
			}
		}

		// Add synonym-based expansions
		for (String keyword : new HashSet<>(expandedKeywords))
		{ // Copy to avoid concurrent modification
			Set<String> synonyms = SYNONYMS.get(keyword.toLowerCase());
			if (synonyms != null)
			{
				expandedKeywords.addAll(synonyms);
			}

			// Check reverse synonym mapping
			for (Map.Entry<String, Set<String>> entry : SYNONYMS.entrySet())
			{
				if (entry.getValue().contains(keyword.toLowerCase()))
				{
					expandedKeywords.add(entry.getKey());
					expandedKeywords.addAll(entry.getValue());
				}
			}
		}

		return expandedKeywords;
	}

	/**
	 * Calculate relationship score based on keyword matches and similarity
	 * Now focuses purely on dynamic keyword matching without hard-coded patterns
	 */
	private double calculateRelationshipScore(RelationshipType type, Set<String> expandedKeywords, Set<String> originalKeywords)
	{
		double score = 0.0;
		String[] relationshipKeywords = type.getKeywordArray();

		for (String relKeyword : relationshipKeywords)
		{
			String cleanRelKeyword = relKeyword.trim().toLowerCase();
			double bestMatchScore = 0.0;

			// Check against all expanded keywords
			for (String riddleKeyword : expandedKeywords)
			{
				String cleanRiddleKeyword = riddleKeyword.toLowerCase();

				if (cleanRelKeyword.equals(cleanRiddleKeyword))
				{
					bestMatchScore = Math.max(bestMatchScore, 5.0); // Exact match
				}
				else
				{
					// Use similarity for partial matches - convert distance to similarity
					double distance = jaroWinklerDistance.apply(cleanRiddleKeyword, cleanRelKeyword);
					double similarity = 1.0 - distance; // Convert distance to similarity
					// Log similarity for debugging
					log.debug("Similarity between '{}' and '{}': {}", cleanRiddleKeyword, cleanRelKeyword, similarity);

					if (similarity <= (1.0 - EXACT_MATCH_THRESHOLD))
					{
						bestMatchScore = Math.max(bestMatchScore, 5.0);
					}
					else if (similarity <= (1.0 - HIGH_SIMILARITY_THRESHOLD))
					{
						bestMatchScore = Math.max(bestMatchScore, 4.0 * similarity);
					}
					else if (similarity <= (1.0 - MEDIUM_SIMILARITY_THRESHOLD))
					{
						bestMatchScore = Math.max(bestMatchScore, 3.0 * similarity);
					}
					else if (similarity <= (1.0 - LOW_SIMILARITY_THRESHOLD))
					{
						bestMatchScore = Math.max(bestMatchScore, 2.0 * similarity);
					}

					// Check substring matches
					if (cleanRelKeyword.contains(cleanRiddleKeyword) || cleanRiddleKeyword.contains(cleanRelKeyword))
					{
						bestMatchScore = Math.max(bestMatchScore, 3.0);
					}
				}
			}

			// Bonus for matches with original (non-expanded) keywords
			for (String originalKeyword : originalKeywords)
			{
				String cleanOriginal = originalKeyword.toLowerCase();
				if (cleanRelKeyword.equals(cleanOriginal))
				{
					bestMatchScore *= 1.5; // 50% bonus for direct riddle word matches
				}
				else if (cleanRelKeyword.contains(cleanOriginal) || cleanOriginal.contains(cleanRelKeyword))
				{
					bestMatchScore *= 1.2; // 20% bonus for partial direct matches
				}
			}

			score += bestMatchScore;
		}

		return score;
	}

	/**
	 * Extract meaningful keywords from riddle text (removing stop words)
	 */
	private Set<String> extractRiddleKeywords(String riddle)
	{
		Set<String> keywords = new HashSet<>();

		// Clean and split the riddle
		String cleanRiddle = riddle.toLowerCase()
			.replaceAll("[^a-zA-Z\\s]", " ") // Remove punctuation
			.replaceAll("\\s+", " ") // Normalize whitespace
			.trim();

		String[] words = cleanRiddle.split("\\s+");

		for (String word : words)
		{
			if (!STOP_WORDS.contains(word) && word.length() > 2)
			{
				keywords.add(word);
			}
		}

		return keywords;
	}

	/**
	 * Expand keywords using synonym mapping
	 */
	private Set<String> expandWithSynonyms(Set<String> keywords)
	{
		Set<String> expandedKeywords = new HashSet<>(keywords);

		for (String keyword : keywords)
		{
			Set<String> synonyms = SYNONYMS.get(keyword);
			if (synonyms != null)
			{
				expandedKeywords.addAll(synonyms);
			}

			// Also check if any synonym maps match this keyword
			for (Map.Entry<String, Set<String>> entry : SYNONYMS.entrySet())
			{
				if (entry.getValue().contains(keyword))
				{
					expandedKeywords.add(entry.getKey());
					expandedKeywords.addAll(entry.getValue());
				}
			}
		}

		return expandedKeywords;
	}

	private RandomEventItem findMissingItemByPartialMatch(List<RandomEventItem> knownItems, List<RandomEventItem> candidates)
	{
		Map<RandomEventItem, Double> candidateScores = new HashMap<>();

		for (RandomEventItem candidate : candidates)
		{
			double totalScore = 0.0;
			List<RandomEventItem> testGroup = new ArrayList<>(knownItems);
			testGroup.add(candidate);

			// Check each relationship for how well the 4-item group fits
			for (Set<RandomEventItem> relationshipItems : relationships.values())
			{
				long matches = testGroup.stream()
					.filter(relationshipItems::contains)
					.count();

				// Score based on completeness and relationship strength
				double relationshipScore = 0.0;
				if (matches == 4)
				{
					relationshipScore = 10.0; // Perfect complete set
				}
				else if (matches == 3)
				{
					// Check if the candidate is the missing piece
					long knownMatches = knownItems.stream()
						.filter(relationshipItems::contains)
						.count();
					if (knownMatches == 2 && relationshipItems.contains(candidate))
					{
						relationshipScore = 8.0; // Good candidate for completion
					}
					else if (knownMatches == 3 && relationshipItems.contains(candidate))
					{
						relationshipScore = 6.0; // Candidate adds to existing strong group
					}
					else
					{
						relationshipScore = 4.0; // Some connection
					}
				}
				else if (matches == 2)
				{
					relationshipScore = 2.0; // Weak connection
				}
				else if (matches == 1)
				{
					relationshipScore = 0.5; // Very weak connection
				}

				totalScore += relationshipScore;
			}

			candidateScores.put(candidate, totalScore);
		}

		// Return the highest scoring candidate
		return candidateScores.entrySet().stream()
			.max(Map.Entry.comparingByValue())
			.map(Map.Entry::getKey)
			.orElse(null);
	}

	/**
	 * Get similarity score between a hint and relationship keywords
	 *
	 * @param hint             The input hint
	 * @param relationshipType The relationship to check
	 * @return Similarity score between 0.0 and 1.0+ (can exceed 1.0 for exact matches with bonuses)
	 */
	public double getRelationshipSimilarity(String hint, RelationshipType relationshipType)
	{
		String lowerHint = hint.toLowerCase().trim();
		String[] keywords = relationshipType.getKeywordArray();
		double maxSimilarity = 0.0;

		for (String keyword : keywords)
		{
			String cleanKeyword = keyword.trim().toLowerCase();

			// Check for exact match first
			if (cleanKeyword.equals(lowerHint))
			{
				return 2.0; // Bonus for exact match
			}

			// Calculate Jaro-Winkler similarity (convert distance to similarity)
			double distance = jaroWinklerDistance.apply(lowerHint, cleanKeyword);
			double similarity = 1.0 - distance;

			// Boost partial matches
			if (cleanKeyword.contains(lowerHint) || lowerHint.contains(cleanKeyword))
			{
				similarity = Math.max(similarity, 0.8);
			}

			maxSimilarity = Math.max(maxSimilarity, similarity);
		}

		return maxSimilarity;
	}

	/**
	 * Find the best matching relationships for a hint, sorted by similarity
	 *
	 * @param hint          The input hint
	 * @param minSimilarity Minimum similarity threshold (default 0.6)
	 * @return List of relationships sorted by similarity score
	 */
	public List<Map.Entry<RelationshipType, Double>> findBestRelationshipMatches(String hint, double minSimilarity)
	{
		Map<RelationshipType, Double> scores = new HashMap<>();

		for (RelationshipType type : RelationshipType.values())
		{
			double similarity = getRelationshipSimilarity(hint, type);
			if (similarity >= minSimilarity)
			{
				scores.put(type, similarity);
			}
		}

		return scores.entrySet().stream()
			.sorted(Map.Entry.<RelationshipType, Double>comparingByValue().reversed())
			.collect(Collectors.toList());
	}

	/**
	 * Overloaded method with default similarity threshold
	 */
	public List<Map.Entry<RelationshipType, Double>> findBestRelationshipMatches(String hint)
	{
		return findBestRelationshipMatches(hint, LOW_SIMILARITY_THRESHOLD);
	}

	/**
	 * Debug method to analyze how a riddle is processed (now pattern-agnostic)
	 */
	public void analyzeRiddle(String riddle)
	{
		log.debug("Analyzing riddle: {}", riddle);

		// Show extracted keywords
		Set<String> riddleKeywords = extractRiddleKeywords(riddle);
		log.debug("Extracted Keywords: {}", riddleKeywords);

		// Show expanded keywords
		Set<String> expandedKeywords = expandWithContextAndSynonyms(riddleKeywords);
		log.debug("Expanded Keywords: {}", expandedKeywords);

		// Show top relationship matches
		Map<RelationshipType, Double> scores = new HashMap<>();
		for (RelationshipType type : RelationshipType.values())
		{
			double score = calculateRelationshipScore(type, expandedKeywords, riddleKeywords);
			if (score > 0)
			{
				scores.put(type, score);
			}
		}

		log.debug("Top Relationship Matches:");
		scores.entrySet().stream()
			.sorted(Map.Entry.<RelationshipType, Double>comparingByValue().reversed())
			.limit(5)
			.forEach(entry ->
				log.debug("  {} - {}", String.format("%.2f", entry.getValue()), entry.getKey().name())
			);
	}

	/**
	 * Test method for any riddle (not just hard-coded examples)
	 */
	public void testRiddle(String riddle, List<RandomEventItem> availableItems)
	{
		log.debug("--- Debug Analysis ---");
		analyzeRiddle(riddle);

		List<RandomEventItem> results = findItemsByHint(riddle, availableItems, 3);
		log.debug("Recommended items: {}", results);
	}
}
