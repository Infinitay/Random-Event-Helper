package randomeventsolver;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;
import randomeventsolver.data.RandomEventItem;

@Slf4j
@PluginDescriptor(
	name = "Random Event Solver"
)
public class RandomEventSolverPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ItemManager itemManager;


	@Inject
	private RandomEventSolverConfig config;

	@Inject
	private RandomEventSolverOverlay overlay;

	@Getter
	private ImmutableSet<RandomEventItem> patternCardAnswers;

	@Getter
	private ImmutableSet<Widget> patternCardAnswerWidgets;

	@Getter
	private RandomEventItem patternNextAnswer;

	@Getter
	private Widget patternNextAnswerWidget;

	private OSRSItemRelationshipSystem relationshipSystem;

	@Getter
	private ImmutableList<Widget> beehiveAnswerWidgets;

	private static final int[] PATTERNCARDS_INTERFACEIDS_AVAILABLE_CARD_MODELS = {
		InterfaceID.PatternCards.CARD_0,
		InterfaceID.PatternCards.CARD_1,
		InterfaceID.PatternCards.CARD_2,
		InterfaceID.PatternCards.CARD_3,
		InterfaceID.PatternCards.CARD_4,
		InterfaceID.PatternCards.CARD_5,
		InterfaceID.PatternCards.CARD_6,
		InterfaceID.PatternCards.CARD_7,
		InterfaceID.PatternCards.CARD_8,
		InterfaceID.PatternCards.CARD_9,
		InterfaceID.PatternCards.CARD_10,
		InterfaceID.PatternCards.CARD_11,
		InterfaceID.PatternCards.CARD_12,
		InterfaceID.PatternCards.CARD_13,
		InterfaceID.PatternCards.CARD_14
	};

	private static final int[] PATTERNCARDS_INTERFACEIDS_AVAILABLE_CARD_SELECTS = {
		InterfaceID.PatternCards.SELECT_0,
		InterfaceID.PatternCards.SELECT_1,
		InterfaceID.PatternCards.SELECT_2,
		InterfaceID.PatternCards.SELECT_3,
		InterfaceID.PatternCards.SELECT_4,
		InterfaceID.PatternCards.SELECT_5,
		InterfaceID.PatternCards.SELECT_6,
		InterfaceID.PatternCards.SELECT_7,
		InterfaceID.PatternCards.SELECT_8,
		InterfaceID.PatternCards.SELECT_9,
		InterfaceID.PatternCards.SELECT_10,
		InterfaceID.PatternCards.SELECT_11,
		InterfaceID.PatternCards.SELECT_12,
		InterfaceID.PatternCards.SELECT_13,
		InterfaceID.PatternCards.SELECT_14
	};

	private static final int[] PATTERNNEXT_INTERFACEIDS_INITIAL_PATTERN = {
		InterfaceID.PatternNext._0,
		InterfaceID.PatternNext._1,
		InterfaceID.PatternNext._2
	};

	private static final int[] PATTERNNEXT_INTERFACEIDS_CHOICES = {
		InterfaceID.PatternNext.SELECT_0,
		InterfaceID.PatternNext.SELECT_1,
		InterfaceID.PatternNext.SELECT_2,
		InterfaceID.PatternNext.SELECT_3
	};

	private final String FREAKY_FORESTER_REGEX = "Could you kill a pheasant with (?<numberOfTails>\\d+) tails";
	private final Pattern FREAKY_FORESTER_PATTERN = Pattern.compile(FREAKY_FORESTER_REGEX, Pattern.CASE_INSENSITIVE);

	private int pheasantTailFeathers;

	@Getter
	private Set<NPC> pheasantNPC;

	private final Map<Integer, Integer> PHEASANT_TAIL_NPCID_MAP = ImmutableMap.<Integer, Integer>builder()
		.put(1, NpcID.MACRO_PHEASANT_MODEL_1)
		.put(2, NpcID.MACRO_PHEASANT_MODEL_2)
		.put(3, NpcID.MACRO_PHEASANT_MODEL_3)
		.put(4, NpcID.MACRO_PHEASANT_MODEL_4)
		.build();

	@Override
	protected void startUp() throws Exception
	{
		this.overlayManager.add(overlay);
		this.patternCardAnswers = null;
		this.patternCardAnswerWidgets = null;
		this.patternNextAnswer = null;
		this.patternNextAnswerWidget = null;
		this.relationshipSystem = new OSRSItemRelationshipSystem();
	}

	@Override
	protected void shutDown() throws Exception
	{
		this.overlayManager.remove(overlay);
		this.patternCardAnswers = null;
		this.patternCardAnswerWidgets = null;
		this.patternNextAnswer = null;
		this.patternNextAnswerWidget = null;
		this.relationshipSystem = null;
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{

	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded)
	{
		// log.debug("Widget loaded with group ID: {}", widgetLoaded.getGroupId());
		if (widgetLoaded.getGroupId() == InterfaceID.PATTERN_CARDS)
		{

			this.clientThread.invokeLater(() -> {
				Widget examHintWidget = this.client.getWidget(InterfaceID.PatternCards.HINT);
				if (examHintWidget != null)
				{
					String examHint = examHintWidget.getText();
					log.debug("Exam hint widget loaded with text: {}", examHint);
					if (examHint != null && !examHint.isEmpty())
					{
						List<RandomEventItem> answerItems = this.relationshipSystem.findItemsByHint(examHint, this.getPatternCardMap().values().asList(), 3);
						log.debug("Found answer items for exam hint '{}': {}", examHint, answerItems);
						if (answerItems.size() >= 3)
						{
							this.patternCardAnswers = ImmutableSet.copyOf(answerItems);
							this.patternCardAnswerWidgets = answerItems.subList(0, 3).stream()
								.map(item -> {
									Integer interfaceID = this.getKeyForValue(this.getPatternCardMap(), item);
									Widget interfaceWidget = this.client.getWidget(interfaceID);
									return interfaceWidget != null ? this.getPatternCardSelectionWidgetFromModel(interfaceWidget) : null;
								})
								.filter(Objects::nonNull)
								.collect(ImmutableSet.toImmutableSet());
							log.debug("Pattern card answers set to: {}", this.patternCardAnswers);
							log.debug("Pattern card answer widgets set to: {}", this.patternCardAnswerWidgets);
						}
						else
						{
							log.warn("Found {} items for exam hint '{}', expected 3.", answerItems.size(), examHint);
							this.patternCardAnswers = null;
							this.patternCardAnswerWidgets = null;
						}
					}
					else
					{
						log.warn("Exam hint widget text is empty or null.");
						this.patternCardAnswers = null;
					}
				}
			});
		}

		if (widgetLoaded.getGroupId() == InterfaceID.PATTERN_NEXT)
		{
			this.clientThread.invokeLater(() -> {
				Widget whatsNextTextWidget = this.client.getWidget(InterfaceID.PatternNext.UNIVERSE_TEXT12);
				if (whatsNextTextWidget != null)
				{
					String whatsNextText = whatsNextTextWidget.getText();
					log.debug("What's next widget text loaded: {}", whatsNextText);
					if (whatsNextText != null && !whatsNextText.isEmpty())
					{
						List<RandomEventItem> initialSelectionItems = this.getPatternNextInitialSelectionMap().values().asList();
						List<RandomEventItem> choicesItems = this.getPatternNextChoicesMap().values().asList();
						RandomEventItem answerItem = this.relationshipSystem.findMissingItem(initialSelectionItems, choicesItems);
						if (answerItem != null)
						{
							this.patternNextAnswer = answerItem;
							Integer interfaceID = getKeyForValue(this.getPatternNextChoicesMap(), answerItem);
							this.patternNextAnswerWidget = interfaceID != null ? this.client.getWidget(interfaceID) : null;
							log.debug("Pattern next answer set to: {}", this.patternNextAnswer);
							log.debug("Pattern next answer widget set to: {}", this.patternNextAnswerWidget);
						}
						else
						{
							log.warn("No valid answer found for what's next text '{}'.", whatsNextText);
							this.patternNextAnswer = null;
							this.patternNextAnswerWidget = null;
						}
					}
					else
					{
						log.warn("Next hint widget text is empty or null.");
						this.patternNextAnswer = null;
						this.patternNextAnswerWidget = null;
					}
				}
			});
		}

		if (widgetLoaded.getGroupId() == InterfaceID.BEEHIVE)
		{
			this.clientThread.invokeLater(() -> {
				Widget exampleHiveWidget = this.client.getWidget(InterfaceID.Beehive.EXAMPLE);
				if (exampleHiveWidget != null)
				{
					// Number the placeholder texts to help users
					Widget destination1LayerWidget = this.client.getWidget(InterfaceID.Beehive.UNIVERSE_TEXT8);
					Widget destination2LayerWidget = this.client.getWidget(InterfaceID.Beehive.UNIVERSE_TEXT10);
					Widget destination3LayerWidget = this.client.getWidget(InterfaceID.Beehive.UNIVERSE_TEXT12);
					Widget destination4LayerWidget = this.client.getWidget(InterfaceID.Beehive.UNIVERSE_TEXT14);
					if (destination1LayerWidget != null && destination2LayerWidget != null && destination3LayerWidget != null && destination4LayerWidget != null)
					{
						destination1LayerWidget.setText("1. " + destination1LayerWidget.getText());
						destination2LayerWidget.setText("2. " + destination2LayerWidget.getText());
						destination3LayerWidget.setText("3. " + destination3LayerWidget.getText());
						destination4LayerWidget.setText("4. " + destination4LayerWidget.getText());
					}
					else
					{
						log.warn("One or more beehive destination layer widgets are null.");
					}

					// Lid model ID = 28806, Body model ID = 28428, entrance layer model ID = 28803, legs layer model ID = 28808
					// The following widgets are the initial (jumbled) layers of the beehive that we have to rearrange
					Widget start1LayerWidget = this.client.getWidget(InterfaceID.Beehive.START_1);
					Widget start2LayerWidget = this.client.getWidget(InterfaceID.Beehive.START_2);
					Widget start3LayerWidget = this.client.getWidget(InterfaceID.Beehive.START_3);
					Widget start4LayerWidget = this.client.getWidget(InterfaceID.Beehive.START_4);
					if (start1LayerWidget != null && start2LayerWidget != null && start3LayerWidget != null && start4LayerWidget != null)
					{
						int start1ModelID = start1LayerWidget.getModelId();
						int start2ModelID = start2LayerWidget.getModelId();
						int start3ModelID = start3LayerWidget.getModelId();
						int start4ModelID = start4LayerWidget.getModelId();
						log.debug("Beehive start layer model IDs: {}, {}, {}, {}", start1ModelID, start2ModelID, start3ModelID, start4ModelID);
						// Use this set as the correct order of the beehive layers from top to bottom (Lid, Body, Entrance, Legs)
						BiMap<Widget, Integer> startingLayerMap = ImmutableBiMap.<Widget, Integer>builder()
							.put(start1LayerWidget, start1ModelID)
							.put(start2LayerWidget, start2ModelID)
							.put(start3LayerWidget, start3ModelID)
							.put(start4LayerWidget, start4ModelID)
							.build();
						Widget[] correctBeehiveOrderWidgets = new Widget[4];
						for (Integer modelID : startingLayerMap.values())
						{
							switch (modelID)
							{
								case 28806: // Lid
									correctBeehiveOrderWidgets[0] = startingLayerMap.inverse().get(28806);
									break;
								case 28428: // Body
									correctBeehiveOrderWidgets[1] = startingLayerMap.inverse().get(28428);
									break;
								case 28803: // Entrance
									correctBeehiveOrderWidgets[2] = startingLayerMap.inverse().get(28803);
									break;
								case 28808: // Legs
									correctBeehiveOrderWidgets[3] = startingLayerMap.inverse().get(28808);
									break;
								default:
									log.warn("Unexpected beehive layer model ID: {}", modelID);
									break;
							}
						}
						this.beehiveAnswerWidgets = ImmutableList.copyOf(correctBeehiveOrderWidgets);
						log.debug("Correct beehive order widgets: {}", this.beehiveAnswerWidgets);
					}
					else
					{
						log.warn("One or more beehive start layer widgets are null.");
						this.beehiveAnswerWidgets = null;
					}
				}
			});
		}

		if (widgetLoaded.getGroupId() == InterfaceID.CHAT_LEFT)
		{
			this.clientThread.invokeLater(() -> {
				Widget chatboxTextWidget = this.client.getWidget(InterfaceID.ChatLeft.TEXT);
				if (chatboxTextWidget != null)
				{
					String chatboxText = Text.sanitizeMultilineText(chatboxTextWidget.getText());
					if (chatboxText != null && !chatboxText.isEmpty())
					{
						log.debug("Chatbox text loaded: {}", chatboxText);
						Matcher matcher = FREAKY_FORESTER_PATTERN.matcher(chatboxText);

						if (matcher.find())
						{
							String fullMatch = matcher.group(0);
							this.pheasantTailFeathers = Integer.parseInt(matcher.group("numberOfTails"));

							System.out.println("Full match: " + fullMatch);
							System.out.println("Number of tails: " + this.pheasantTailFeathers);
							// <1, NpcID.MACRO_PHEASANT_MODEL_1>, <2, NpcID.MACRO_PHEASANT_MODEL_2>, <3, NpcID.MACRO_PHEASANT_MODEL_3>, <4, NpcID.MACRO_PHEASANT_MODEL_4>
							this.pheasantNPC = this.client.getTopLevelWorldView().npcs().stream().filter(npc -> npc.getId() == PHEASANT_TAIL_NPCID_MAP.get(this.pheasantTailFeathers)).collect(Collectors.toSet());
						}
					}
					else
					{
						log.warn("Chatbox text is empty or null.");
						this.pheasantNPC = new HashSet<>();
						this.pheasantTailFeathers = 0;
					}
				}
				else
				{
					log.warn("Chatbox text widget is null.");
					this.pheasantNPC = new HashSet<>();
					this.pheasantTailFeathers = 0;
				}
			});
		}
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed widgetClosed)
	{
		// log.debug("Widget closed with group ID: {}", widgetClosed.getGroupId());
		if (widgetClosed.getGroupId() == InterfaceID.PATTERN_CARDS)
		{
			log.debug("Pattern cards widget closed, resetting pattern card answers.");
			this.patternCardAnswers = null;
			this.patternCardAnswerWidgets = null;
		}

		if (widgetClosed.getGroupId() == InterfaceID.PATTERN_NEXT)
		{
			log.debug("Pattern next widget closed, resetting pattern next answer.");
			this.patternNextAnswer = null;
			this.patternNextAnswerWidget = null;
		}

		if (widgetClosed.getGroupId() == InterfaceID.BEEHIVE)
		{
			log.debug("Beehive widget closed, resetting beehive answer widgets.");
			this.beehiveAnswerWidgets = null;
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		NPC npc = npcSpawned.getNpc();
		if (this.client.getLocalPlayer().getWorldLocation().getRegionID() == 10314)
		{
			if (npc.getId() == PHEASANT_TAIL_NPCID_MAP.get(this.pheasantTailFeathers))
			{
				log.debug("A new pheasant NPC spawned with {} tail feathers, adding to the set.", this.pheasantTailFeathers);
				if (this.pheasantNPC != null)
				{
					this.pheasantNPC.add(npc);
				}
				else
				{
					log.warn("Pheasant NPC set is null, skipping it.");
				}
			}
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		if (npcDespawned.getNpc().getId() == NpcID.MACRO_FORESTER_M)
		{
			log.debug("Freaky Forester NPC despawned, resetting pheasant NPCs.");
			this.pheasantNPC = new HashSet<>();
			this.pheasantTailFeathers = 0;
		}
	}

	@Provides
	RandomEventSolverConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RandomEventSolverConfig.class);
	}

	private ImmutableMultimap<Integer, RandomEventItem> getPatternCardMap()
	{
		ImmutableMultimap.Builder<Integer, RandomEventItem> builder = ImmutableMultimap.builder();
		for (int patternCardSelectionInterfaceID : PATTERNCARDS_INTERFACEIDS_AVAILABLE_CARD_MODELS)
		{
			int modelID = Objects.requireNonNull(this.client.getWidget(patternCardSelectionInterfaceID)).getModelId();
			RandomEventItem randomEventItem = RandomEventItem.fromModelID(modelID);
			if (randomEventItem == null)
			{
				log.warn("No RandomEventItem found for model ID: {}", modelID);
				continue;
			}
			builder.put(patternCardSelectionInterfaceID, randomEventItem);
		}
		return builder.build();
	}

	private ImmutableMap<Integer, RandomEventItem> getPatternNextInitialSelectionMap()
	{
		ImmutableMap.Builder<Integer, RandomEventItem> builder = ImmutableMap.builder();
		for (int patternNextInitialPatternInterfaceID : PATTERNNEXT_INTERFACEIDS_INITIAL_PATTERN)
		{
			int modelID = Objects.requireNonNull(this.client.getWidget(patternNextInitialPatternInterfaceID)).getModelId();
			RandomEventItem randomEventItem = RandomEventItem.fromModelID(modelID);
			if (randomEventItem == null)
			{
				log.warn("No RandomEventItem found for model ID: {}", modelID);
				continue;
			}
			builder.put(patternNextInitialPatternInterfaceID, randomEventItem);
		}
		return builder.build();
	}

	private ImmutableMultimap<Integer, RandomEventItem> getPatternNextChoicesMap()
	{
		ImmutableMultimap.Builder<Integer, RandomEventItem> builder = ImmutableMultimap.builder();
		for (int patternNextChoiceInterfaceID : PATTERNNEXT_INTERFACEIDS_CHOICES)
		{
			int modelID = Objects.requireNonNull(this.client.getWidget(patternNextChoiceInterfaceID)).getModelId();
			RandomEventItem randomEventItem = RandomEventItem.fromModelID(modelID);
			if (randomEventItem == null)
			{
				log.warn("No RandomEventItem found for model ID: {}", modelID);
				continue;
			}
			builder.put(patternNextChoiceInterfaceID, randomEventItem);
		}
		return builder.build();
	}

	private Integer getKeyForValue(Multimap<Integer, RandomEventItem> map, RandomEventItem value)
	{
		for (Map.Entry<Integer, RandomEventItem> entry : map.entries())
		{
			if (entry.getValue().equals(value))
			{
				return entry.getKey();
			}
		}
		return null;
	}

	public Widget getPatternCardSelectionWidgetFromModel(Widget modelWidget)
	{
		// This method retrieves the widget for the pattern card selection based on the model widget.
		// PATTERNCARDS_INTERFACEIDS_AVAILABLE_CARD_MODELS[i] = PATTERNCARDS_INTERFACEIDS_AVAILABLE_CARD_SELECTS[i]
		// So if modelWidget corresponds to PATTERNCARDS_INTERFACEIDS_AVAILABLE_CARD_MODELS[i] then it should return PATTERNCARDS_INTERFACEIDS_AVAILABLE_CARD_SELECTS[i]
		for (int i = 0; i < PATTERNCARDS_INTERFACEIDS_AVAILABLE_CARD_MODELS.length; i++)
		{
			if (modelWidget.getId() == PATTERNCARDS_INTERFACEIDS_AVAILABLE_CARD_MODELS[i])
			{
				return this.client.getWidget(PATTERNCARDS_INTERFACEIDS_AVAILABLE_CARD_SELECTS[i]);
			}
		}
		log.warn("No matching selection widget found for model widget ID: {}", modelWidget.getId());
		return null;
	}

	@Getter
	@AllArgsConstructor
	enum PatternCardAnswers
	{

		//		HINT_1("I feel like a fish out of water!", ImmutableSet.of(RandomEventItem.SALMON, RandomEventItem.TROUT, RandomEventItem.HARPOON)),
//		HINT_2("Sea food, catch the food, spot the pattern.", ImmutableSet.of(RandomEventItem.SALMON, RandomEventItem.TROUT, RandomEventItem.HARPOON)),
//		HINT_3("I'm fishing for answers.", ImmutableSet.of(RandomEventItem.SALMON, RandomEventItem.TROUT, RandomEventItem.HARPOON)),
		HINT_4("The pen may be mightier than the sword, but against a dragon? I'll take a melee weapon.", ImmutableSet.of());

		private final String hint;
		private final ImmutableSet<RandomEventItem> answerModels;
	}

	@Getter
	@AllArgsConstructor
	enum PatternNextRelations
	{
		FRUITS(ImmutableSet.of(RandomEventItem.BANANA, RandomEventItem.ONION, RandomEventItem.PINEAPPLE, RandomEventItem.WATERMELON_SLICE)),
		MAGIC(ImmutableSet.of(RandomEventItem.AIR_RUNE, RandomEventItem.EARTH_RUNE, RandomEventItem.FIRE_RUNE, RandomEventItem.STAFF));

		private final ImmutableSet<RandomEventItem> models;
	}
}
