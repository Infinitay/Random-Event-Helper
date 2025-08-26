package randomeventsolver;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.Arrays;
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
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.DynamicObject;
import net.runelite.api.GameObject;
import net.runelite.api.GroundObject;
import net.runelite.api.Item;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;
import randomeventsolver.data.Coffin;
import randomeventsolver.data.Grave;
import randomeventsolver.data.RandomEventItem;
import randomeventsolver.randomevents.surpriseexam.SurpriseExamHelper;
import randomeventsolver.randomevents.surpriseexam.SurpriseExamOverlay;

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

	@Inject
	private RandomEventSolverItemOverlay itemOverlay;

	@Inject
	private SurpriseExamHelper surpriseExamHelper;

	@Getter
	private ImmutableList<Widget> beehiveAnswerWidgets;

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

	@Getter
	private GameObject activePinballPost;

	private final Set<Integer> PINBALL_POST_OBJECTS_SET = ImmutableSet.of(ObjectID.PINBALL_POST_TREE_INACTIVE, ObjectID.PINBALL_POST_IRON_INACTIVE, ObjectID.PINBALL_POST_COAL_INACTIVE, ObjectID.PINBALL_POST_FISHING_INACTIVE, ObjectID.PINBALL_POST_ESSENCE_INACTIVE);
	private Set<GameObject> pinballPostsSet = new HashSet<>();

	@Override
	protected void startUp() throws Exception
	{
		this.overlayManager.add(overlay);
		this.overlayManager.add(itemOverlay);
		if (config.isSurpriseExamEnabled())
		{
			surpriseExamHelper.startUp();
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		this.overlayManager.remove(overlay);
		this.overlayManager.remove(itemOverlay);
		this.exerciseMatsAnswerList.clear();
		this.exerciseMatsMultimap.clear();
		this.exerciseVarbitMatMultimap.clear();
		log.debug("Config isSurpriseExamEnabled: {}", config.isSurpriseExamEnabled());
		surpriseExamHelper.shutDown();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (configChanged.getGroup().equals("randomeventhelper"))
		{
			log.debug("Config changed: {} | New value: {}", configChanged.getKey(), configChanged.getNewValue());
			if (configChanged.getKey().equals("isSurpriseExamEnabled"))
			{
				if (config.isSurpriseExamEnabled())
				{
					surpriseExamHelper.startUp();
				}
				else
				{
					surpriseExamHelper.shutDown();
				}
			}
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged animationChanged)
	{
		Actor actor = animationChanged.getActor();
		if (actor instanceof NPC)
		{
			log.debug("NPC Animation changed: {} - New Animation ID: {}", ((NPC) actor).getName(), actor.getAnimation());
		}
		else if (actor instanceof GameObject)
		{
			log.debug("GameObject Animation changed: {} - New Animation ID: {}", ((GameObject) actor).getId(), actor.getAnimation());
		}
		else if (actor instanceof Player)
		{
			log.debug("Player Animation changed: {} - New Animation ID: {}", ((Player) actor).getName(), actor.getAnimation());
		}
		else if (actor instanceof DynamicObject)
		{
			log.debug("DynamicObject Animation changed: {} - New Animation ID: {}", ((DynamicObject) actor).getModel().getSceneId(), actor.getAnimation());
		}
		else
		{
			log.debug("Unknown Actor Animation changed: {} - New Animation ID: {}", actor.getName(), actor.getAnimation());
		}
	}

	private boolean initiallyEnteredGraveDiggerArea;

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
//		log.debug("isInstanced: {} | WorldLocation Region ID: {} | LocalLocation Region ID: {}", this.client.getTopLevelWorldView().isInstance(), this.client.getLocalPlayer().getWorldLocation().getRegionID(), this.getRegionIDFromCurrentLocalPointInstanced());
		if (this.getRegionIDFromCurrentLocalPointInstanced() == 7758)
		{
			for (GameObject pinballObject : this.pinballPostsSet)
			{
				if (pinballObject != null && pinballObject.getRenderable() instanceof DynamicObject)
				{
					DynamicObject dynamicPinballObject = (DynamicObject) pinballObject.getRenderable();
					if (dynamicPinballObject != null && dynamicPinballObject.getAnimation().getId() == 4005)
					{
						this.activePinballPost = pinballObject;
						log.debug("Active pinball post found with ID: {}", this.activePinballPost.getId());
						break; // Exit the loop once we find the active post
					}
				}
			}

			// There is an edgecase where when you're at the grave digger random event if a varb is still 0 then it won't fire.
			// So lets handle this by checking to see if a player is in the grave digger random event area via NPC Leo spawn
			// And by using a separate variable to make sure not to run this constantly every game tick
			if (this.initiallyEnteredGraveDiggerArea)
			{
				for (Grave.GraveNumber graveNumber : Grave.GraveNumber.values())
				{
					VarbitChanged graveTypeVarbitChangedEvent = new VarbitChanged();
					graveTypeVarbitChangedEvent.setVarbitId(graveNumber.getGraveTypeVarbitID());
					graveTypeVarbitChangedEvent.setValue(this.client.getVarbitValue(graveNumber.getGraveTypeVarbitID()));
					VarbitChanged placedCoffinVarbitChangedEvent = new VarbitChanged();
					placedCoffinVarbitChangedEvent.setVarbitId(graveNumber.getPlacedCoffinVarbitID());
					placedCoffinVarbitChangedEvent.setValue(this.client.getVarbitValue(graveNumber.getPlacedCoffinVarbitID()));
					this.onVarbitChanged(graveTypeVarbitChangedEvent);
					this.onVarbitChanged(placedCoffinVarbitChangedEvent);
					this.initiallyEnteredGraveDiggerArea = false;
				}

			}
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded)
	{
		// log.debug("Widget loaded with group ID: {}", widgetLoaded.getGroupId());

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
						Matcher freakyForesterMatcher = FREAKY_FORESTER_PATTERN.matcher(chatboxText);

						if (freakyForesterMatcher.find())
						{
							String fullMatch = freakyForesterMatcher.group(0);
							this.pheasantTailFeathers = Integer.parseInt(freakyForesterMatcher.group("numberOfTails"));

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
		else if (this.getRegionIDFromCurrentLocalPointInstanced() == 7758)
		{
			if (npc.getId() == NpcID.MACRO_GRAVEDIGGER)
			{
				log.debug("Grave Digger Leo NPC spawned in grave digger random event area.");
				this.initiallyEnteredGraveDiggerArea = true;
				// Take this opportunity to initialize the BufferedImage for the coffin items
				for (Coffin coffin : Coffin.values())
				{
					coffinItemImageMap.put(coffin, coffin.getItemImage(this.itemManager));
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
		else if (npcDespawned.getNpc().getId() == NpcID.PINBALL_TROLL_LFT && npcDespawned.getNpc().getId() == NpcID.PINBALL_TROLL_RHT)
		{
			log.debug("A pinball troll despawned, resetting active pinball post.");
			this.activePinballPost = null;
			this.pinballPostsSet = new HashSet<>();
		}
		else if (npcDespawned.getNpc().getId() == NpcID.MACRO_DRILLDEMON)
		{
			log.debug("Drill Demon NPC despawned, resetting exercise mats and mappings.");
			this.exerciseMatsAnswerList.clear();
			this.exerciseMatsMultimap.clear();
			this.exerciseVarbitMatMultimap.clear();
		}
		else if (npcDespawned.getNpc().getId() == NpcID.MACRO_GRAVEDIGGER)
		{
			log.debug("Grave Digger Leo NPC despawned, resetting grave digger area state.");
			this.initiallyEnteredGraveDiggerArea = false;
			this.graveMap.clear();
			this.coffinsInInventory.clear();
			this.coffinItemImageMap.clear();
		}
	}

	@Getter
	private Map<Coffin, BufferedImage> coffinItemImageMap = Maps.newHashMap();

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned gameObjectSpawned)
	{
		GameObject gameObject = gameObjectSpawned.getGameObject();
		// Pinball and grave digger random even locations are in region 7758
		if (this.getRegionIDFromCurrentLocalPointInstanced() == 7758)
		{
			if (PINBALL_POST_OBJECTS_SET.contains(gameObject.getId()))
			{
				log.debug("A new pinball post object spawned with ID: {}, adding to the set.", gameObject.getId());
				this.pinballPostsSet.add(gameObject);
			}
			else if (Grave.GraveNumber.isGravestoneObjectID(gameObject.getId()))
			{
				Grave.GraveNumber graveNumber = Grave.GraveNumber.getGraveNumberFromGravestoneObjectID(gameObject.getId());
				if (graveNumber != null)
				{
					log.debug("A new gravestone object ({}) spawned with ID: {}, updating grave map.", graveNumber.name(), gameObject.getId());
					Grave grave = this.graveMap.get(graveNumber);
					if (grave == null)
					{
						grave = new Grave(graveNumber);
					}
					grave.setGraveStone(gameObject);
					this.graveMap.put(graveNumber, grave);
				}
				else
				{
					log.warn("Gravestone object ID {} does not map to a known grave number.", gameObject.getId());
				}
			}
			else if (Grave.GraveNumber.isEmptyGraveObjectID(gameObject.getId()))
			{
				Grave.GraveNumber graveNumber = Grave.GraveNumber.getGraveNumberFromEmptyGraveObjectID(gameObject.getId());
				if (graveNumber != null)
				{
					log.debug("A new empty grave object ({}) spawned with ID: {}, updating grave map.", graveNumber.name(), gameObject.getId());
					Grave grave = this.graveMap.get(graveNumber);
					if (grave == null)
					{
						grave = new Grave(graveNumber);
					}
					grave.setEmptyGrave(gameObject);
					this.graveMap.put(graveNumber, grave);
				}
				else
				{
					log.warn("Empty grave object ID {} does not map to a known grave number.", gameObject.getId());
				}
			}
			else if (Grave.GraveNumber.isFilledGraveObjectID(gameObject.getId()))
			{
				Grave.GraveNumber graveNumber = Grave.GraveNumber.getGraveNumberFromFilledGraveObjectID(gameObject.getId());
				if (graveNumber != null)
				{
					log.debug("A new filled grave object ({}) spawned with ID: {}, updating grave map.", graveNumber.name(), gameObject.getId());
					Grave grave = this.graveMap.get(graveNumber);
					if (grave == null)
					{
						grave = new Grave(graveNumber);
					}
					grave.setFilledGrave(gameObject);
					this.graveMap.put(graveNumber, grave);
				}
				else
				{
					log.warn("Filled grave object ID {} does not map to a known grave number.", gameObject.getId());
				}
			}
		}
	}

	private Set<String> DRILL_DEMON_EXERCISE_STRINGS = ImmutableSet.of(DrillExercise.JOG.drillSergeantText, DrillExercise.SIT_UP.drillSergeantText, DrillExercise.PUSH_UP.drillSergeantText, DrillExercise.STAR_JUMP.drillSergeantText);

	@Getter
	private List<GroundObject> exerciseMatsAnswerList = Lists.newArrayList();

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		log.debug("Chat message ({}) received: {}", chatMessage.getType(), chatMessage.getMessage());
		String sanitizedChatMessage = Text.sanitizeMultilineText(chatMessage.getMessage());
		if (chatMessage.getType() == ChatMessageType.GAMEMESSAGE)
		{
			if (this.getRegionIDFromCurrentLocalPointInstanced() == 7758 && sanitizedChatMessage.equals("You may now leave the game area."))
			{
				log.debug("Pinball game has ended so resetting active pinball post and pinball posts set.");
				this.activePinballPost = null;
				this.pinballPostsSet = new HashSet<>();
			}
		}
		else if (chatMessage.getType() == ChatMessageType.DIALOG)
		{
			if (this.client.getLocalPlayer().getWorldLocation().getRegionID() == 12619)
			{
				this.exerciseMatsAnswerList.clear();
				DrillExercise exercise = DrillExercise.getExerciseFromText(sanitizedChatMessage);
				if (exercise != null)
				{
					log.debug("Drill Demon requested exercise: {}", exercise.name());
					this.exerciseMatsAnswerList = Lists.newArrayList(this.exerciseVarbitMatMultimap.get(exercise.getVarbitValue()));
					log.debug("Drill Demon exercise mats list set to: {}", this.exerciseMatsAnswerList);
				}
				else
				{
					log.warn("Drill Demon requested unknown exercise: {}", sanitizedChatMessage);
					this.exerciseMatsAnswerList.clear();
				}
			}
		}
	}

	// <Grave Number, Grave>
	@Getter
	private Map<Grave.GraveNumber, Grave> graveMap = Maps.newHashMap();

	@Subscribe
	public void onVarbitChanged(VarbitChanged varbitChanged)
	{
		switch (varbitChanged.getVarbitId())
		{
			case VarbitID.MACRO_DRILLDEMON_POST_1:
				this.updateExerciseMappings(varbitChanged.getValue(), 1);
				break;
			case VarbitID.MACRO_DRILLDEMON_POST_2:
				this.updateExerciseMappings(varbitChanged.getValue(), 2);
				break;
			case VarbitID.MACRO_DRILLDEMON_POST_3:
				this.updateExerciseMappings(varbitChanged.getValue(), 3);
				break;
			case VarbitID.MACRO_DRILLDEMON_POST_4:
				this.updateExerciseMappings(varbitChanged.getValue(), 4);
				break;
			case VarbitID.MACRO_DIGGER_GRAVE_1: // Grave type/Gravestone
				this.updateRequiredCoffin(Grave.GraveNumber.ONE, varbitChanged.getValue());
				break;
			case VarbitID.MACRO_DIGGER_COFFIN_1: // Placed coffin into the grave
				this.updatePlacedCoffin(Grave.GraveNumber.ONE, varbitChanged.getValue());
				break;
			case VarbitID.MACRO_DIGGER_GRAVE_2:
				this.updateRequiredCoffin(Grave.GraveNumber.TWO, varbitChanged.getValue());
				break;
			case VarbitID.MACRO_DIGGER_COFFIN_2:
				this.updatePlacedCoffin(Grave.GraveNumber.TWO, varbitChanged.getValue());
				break;
			case VarbitID.MACRO_DIGGER_GRAVE_3:
				this.updateRequiredCoffin(Grave.GraveNumber.THREE, varbitChanged.getValue());
				break;
			case VarbitID.MACRO_DIGGER_COFFIN_3:
				this.updatePlacedCoffin(Grave.GraveNumber.THREE, varbitChanged.getValue());
				break;
			case VarbitID.MACRO_DIGGER_GRAVE_4:
				this.updateRequiredCoffin(Grave.GraveNumber.FOUR, varbitChanged.getValue());
				break;
			case VarbitID.MACRO_DIGGER_COFFIN_4:
				this.updatePlacedCoffin(Grave.GraveNumber.FOUR, varbitChanged.getValue());
				break;
			case VarbitID.MACRO_DIGGER_GRAVE_5:
				this.updateRequiredCoffin(Grave.GraveNumber.FIVE, varbitChanged.getValue());
				break;
			case VarbitID.MACRO_DIGGER_COFFIN_5:
				this.updatePlacedCoffin(Grave.GraveNumber.FIVE, varbitChanged.getValue());
				break;
			default:
				break;
		}
	}

	private void updateRequiredCoffin(Grave.GraveNumber graveNumber, int requiredCoffinVarbitValue)
	{
		log.debug("Grave {} required coffin varbit changed to value: {}", graveNumber.name(), requiredCoffinVarbitValue);
		Coffin coffin = Coffin.getCoffinFromVarbitValue(requiredCoffinVarbitValue);
		if (coffin != null)
		{
			log.debug("Grave {} requires {} coffin", graveNumber.name(), coffin.name());
			Grave grave = this.graveMap.getOrDefault(graveNumber, new Grave(graveNumber));
			grave.setRequiredCoffin(coffin);
			this.graveMap.put(graveNumber, grave);
		}
		else
		{
			log.warn("Grave {} required coffin varbit changed to unknown coffin value: {}", graveNumber.name(), requiredCoffinVarbitValue);
		}
	}

	private void updatePlacedCoffin(Grave.GraveNumber graveNumber, int placedCoffinVarbitValue)
	{
		log.debug("Grave {} placed coffin varbit changed to value: {}", graveNumber.name(), placedCoffinVarbitValue);
		Coffin coffin = Coffin.getCoffinFromVarbitValue(placedCoffinVarbitValue);
		if (coffin != null)
		{
			log.debug("Found {} coffin placed into Grave {}", coffin.name(), graveNumber.name());
			Grave grave = this.graveMap.getOrDefault(graveNumber, new Grave(graveNumber));
			grave.setPlacedCoffin(coffin);
			this.graveMap.put(graveNumber, grave);
		}
		else
		{
			log.warn("Grave {} placed coffin varbit changed to unknown coffin value: {}", graveNumber.name(), placedCoffinVarbitValue);
		}
	}

	private void updateExerciseMappings(int exerciseVarbitValue, int postNumber)
	{
		DrillExercise exercise = DrillExercise.VARBIT_TO_EXERCISE_MAP.get(exerciseVarbitValue);
		if (exercise != null)
		{
			log.debug("Drill Demon exercise of Post_{} changed to: {} ({})", postNumber, exercise.getVarbitValue(), exercise.name());
			this.exerciseVarbitMatMultimap.replaceValues(exerciseVarbitValue, this.exerciseMatsMultimap.get(postNumber));
		}
		else
		{
			log.warn("Drill Demon exercise varbit changed to unknown value: {}", exerciseVarbitValue);
			this.exerciseVarbitMatMultimap.replaceValues(exerciseVarbitValue, ImmutableSet.of());
		}
	}

	// <Post Number, Mat>
	private Multimap<Integer, GroundObject> exerciseMatsMultimap = HashMultimap.create(4, 2);

	// <Exercise Varbit, Mat>
	private Multimap<Integer, GroundObject> exerciseVarbitMatMultimap = HashMultimap.create(4, 2);

	@Subscribe
	public void onGroundObjectSpawned(GroundObjectSpawned groundObjectSpawned)
	{
		if (this.client.getLocalPlayer().getWorldLocation().getRegionID() == 12619)
		{
			switch (groundObjectSpawned.getGroundObject().getId())
			{
				case ObjectID.BARRACK_MAT_1:
					exerciseMatsMultimap.put(1, groundObjectSpawned.getGroundObject());
					log.debug("Added exercise mat with ID {} to post 1", groundObjectSpawned.getGroundObject().getId());
					break;
				case ObjectID.BARRACK_MAT_2:
					exerciseMatsMultimap.put(2, groundObjectSpawned.getGroundObject());
					log.debug("Added exercise mat with ID {} to post 2", groundObjectSpawned.getGroundObject().getId());
					break;
				case ObjectID.BARRACK_MAT_3:
					exerciseMatsMultimap.put(3, groundObjectSpawned.getGroundObject());
					log.debug("Added exercise mat with ID {} to post 3", groundObjectSpawned.getGroundObject().getId());
					break;
				case ObjectID.BARRACK_MAT_4:
					exerciseMatsMultimap.put(4, groundObjectSpawned.getGroundObject());
					log.debug("Added exercise mat with ID {} to post 4", groundObjectSpawned.getGroundObject().getId());
					break;
				default:
					break;
			}
		}
	}

	private Multiset<Integer> previousInventory = HashMultiset.create();
	private Multiset<Integer> currentInventoryItems = HashMultiset.create();
	@Getter
	private Set<Integer> coffinsInInventory = new HashSet<>();

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged itemContainerChanged)
	{
		// In case of unequipping an item -> INVENTORY -> EQUIPMENT changes
		if (itemContainerChanged.getContainerId() == InventoryID.INV)
		{
			this.currentInventoryItems.clear();
			List<Item> itemStream = Arrays.stream(itemContainerChanged.getItemContainer().getItems()).filter(item -> item.getId() != -1).collect(Collectors.toList());
			itemStream.forEach(item -> this.currentInventoryItems.add(item.getId(), this.itemManager.getItemComposition(item.getId()).isStackable() ? 1 : item.getQuantity()));

			Multiset<Integer> currentInventory = HashMultiset.create();
			List<Item> inventoryItems = Arrays.stream(itemContainerChanged.getItemContainer().getItems()).filter(item -> item.getId() != -1).collect(Collectors.toList());
			inventoryItems.forEach(item -> currentInventory.add(item.getId(), item.getQuantity()));

			// Remember that for set operations difference A - B != B - A
			Multiset<Integer> addedItems = Multisets.difference(currentInventory, this.previousInventory);
			Multiset<Integer> removedItems = Multisets.difference(this.previousInventory, currentInventory);
			log.debug("Added Items: " + addedItems);
			log.debug("Removed Items: " + removedItems);

			for (Integer itemID : addedItems.elementSet())
			{
				if (Coffin.getCoffinFromItemID(itemID) != null)
				{
					this.coffinsInInventory.add(itemID);
					log.debug("Found {} coffin in inventory", Coffin.getCoffinFromItemID(itemID).name());
				}
			}

			this.previousInventory = currentInventory;
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded menuEntryAdded)
	{
		if (this.getRegionIDFromCurrentLocalPointInstanced() == 7758)
		{
			if (Coffin.getCoffinFromItemID(menuEntryAdded.getItemId()) != null && menuEntryAdded.getOption().equals("Check"))
			{
				menuEntryAdded.getMenuEntry().setDeprioritized(true);
			}
		}
	}

	@Provides
	RandomEventSolverConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RandomEventSolverConfig.class);
	}

	// Accounts for local instances too such as inside the pinball random event
	private int getRegionIDFromCurrentLocalPointInstanced()
	{
		return WorldPoint.fromLocalInstance(this.client, this.client.getLocalPlayer().getLocalLocation()).getRegionID();
	}

	@Getter
	@AllArgsConstructor
	enum DrillExercise
	{
		JOG(1, "Get yourself over there and jog on that mat, private!"),
		SIT_UP(2, "Get on that mat and give me sit ups, private!"),
		PUSH_UP(3, "Drop and give me push ups on that mat, private!"),
		STAR_JUMP(4, "I want to see you on that mat doing star jumps, private!");

		private final int varbitValue;
		private final String drillSergeantText;

		private static final Map<Integer, DrillExercise> VARBIT_TO_EXERCISE_MAP = Maps.uniqueIndex(ImmutableList.copyOf(values()), DrillExercise::getVarbitValue);

		private static DrillExercise getExerciseFromText(String text)
		{
			for (DrillExercise exercise : values())
			{
				if (text.toLowerCase().endsWith(exercise.getDrillSergeantText().toLowerCase()))
				{
					return exercise;
				}
			}
			return null;
		}
	}
}
