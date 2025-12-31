package randomeventhelper.randomevents.freakyforester;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.gameval.NpcID;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;
import randomeventhelper.RandomEventHelperConfig;
import randomeventhelper.RandomEventHelperPlugin;
import randomeventhelper.pluginmodulesystem.PluginModule;

@Slf4j
@Singleton
public class FreakyForesterHelper extends PluginModule
{
	@Inject
	private OverlayManager overlayManager;

	@Inject
	private RandomEventHelperConfig config;

	@Inject
	private FreakyForesterOverlay freakyForesterOverlay;

	private final String FREAKY_FORESTER_REGEX = "kill (a|the) (((?<numberOfTails>\\d)-tailed pheasant)|pheasant (with|that has) (?<numberOfTailsMulti>\\w+|\\d) tails)";
	private final Pattern FREAKY_FORESTER_PATTERN = Pattern.compile(FREAKY_FORESTER_REGEX, Pattern.CASE_INSENSITIVE);

	@Getter
	private PheasantMode pheasantHighlightMode;

	@Getter
	private int pheasantTailFeathers;

	@Getter
	private Set<NPC> pheasantNPCSet;

	@Getter
	private NPC nearestPheasantNPC;

	@Getter
	private NPC specificPheasantNPC;

	@Getter
	private NPC freakyForesterNPC;

	private final Map<Integer, Integer> PHEASANT_TAIL_NPCID_MAP = ImmutableMap.<Integer, Integer>builder()
		.put(1, NpcID.MACRO_PHEASANT_MODEL_1)
		.put(2, NpcID.MACRO_PHEASANT_MODEL_2)
		.put(3, NpcID.MACRO_PHEASANT_MODEL_3)
		.put(4, NpcID.MACRO_PHEASANT_MODEL_4)
		.build();

	@Inject
	public FreakyForesterHelper(OverlayManager overlayManager, RandomEventHelperConfig config, Client client)
	{
		super(overlayManager, config, client);
	}

	@Override
	public void onStartUp()
	{
		this.overlayManager.add(freakyForesterOverlay);
		this.pheasantHighlightMode = config.pheasantHighlightMode();
		this.pheasantTailFeathers = 0;
		this.pheasantNPCSet = Sets.newHashSet();
		this.freakyForesterNPC = null;
	}

	@Override
	public void onShutdown()
	{
		this.overlayManager.remove(freakyForesterOverlay);
		this.pheasantTailFeathers = 0;
		this.pheasantNPCSet = null;
		this.nearestPheasantNPC = null;
		this.specificPheasantNPC = null;
		this.freakyForesterNPC = null;
	}

	@Override
	public boolean isEnabled()
	{
		return this.config.isFreakyForesterEnabled();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (configChanged.getGroup().equals("randomeventhelper"))
		{
			if (configChanged.getKey().equals("pheasantHighlightMode"))
			{
				this.pheasantHighlightMode = PheasantMode.valueOf(configChanged.getNewValue());
			}
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		String sanitizedChatMessage = Text.sanitizeMultilineText(chatMessage.getMessage());
		if (chatMessage.getType() == ChatMessageType.DIALOG)
		{
			if (this.isInFreakyForesterInstance())
			{
				Matcher freakyForesterMatcher = FREAKY_FORESTER_PATTERN.matcher(sanitizedChatMessage);

				if (freakyForesterMatcher.find())
				{
					String fullMatch = freakyForesterMatcher.group(0);
					if (freakyForesterMatcher.group("numberOfTails") != null)
					{
						// Should only match 2/3/4
						this.pheasantTailFeathers = Integer.parseInt(freakyForesterMatcher.group("numberOfTails"));
					}
					else
					{
						// Could be 2/3/4 or two/three/four
						try
						{
							this.pheasantTailFeathers = Integer.parseInt(freakyForesterMatcher.group("numberOfTailsMulti"));
						}
						catch (NumberFormatException e)
						{
							this.pheasantTailFeathers = this.convertWordToInt(freakyForesterMatcher.group("numberOfTailsMulti"));
						}
					}
					log.info("Freaky Forester requested a pheasant with {} tail feathers", this.pheasantTailFeathers);
					log.debug("Full match: {}", fullMatch);
					// <1, NpcID.MACRO_PHEASANT_MODEL_1>, <2, NpcID.MACRO_PHEASANT_MODEL_2>, <3, NpcID.MACRO_PHEASANT_MODEL_3>, <4, NpcID.MACRO_PHEASANT_MODEL_4>
					this.updateSpecificPheasant();
				}
				else
				{
					this.pheasantTailFeathers = 0;
				}
			}
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		NPC npc = npcSpawned.getNpc();
		if (this.isInFreakyForesterInstance())
		{
			if (PHEASANT_TAIL_NPCID_MAP.containsValue(npc.getId()))
			{
				if (this.pheasantNPCSet != null)
				{
					log.debug("A new pheasant NPC spawned, adding to the set.");
					this.pheasantNPCSet.add(npc);
					this.updateSpecificPheasant();
				}
				else
				{
					log.warn("Pheasant NPC set is null, skipping it.");
				}
			}
			else if (npc.getId() == NpcID.MACRO_FORESTER_M)
			{
				log.debug("Freaky Forester NPC spawned");
				this.freakyForesterNPC = npc;
			}
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		if (npcDespawned.getNpc().getId() == NpcID.MACRO_FORESTER_M && !this.isInFreakyForesterInstance())
		{
			log.debug("Freaky Forester NPC despawned, resetting pheasant NPCs.");
			this.pheasantTailFeathers = 0;
			this.pheasantNPCSet.clear();
			this.specificPheasantNPC = null;
			this.nearestPheasantNPC = null;
			this.freakyForesterNPC = null;
		}
		else if (this.PHEASANT_TAIL_NPCID_MAP.containsValue(npcDespawned.getNpc().getId()) && this.isInFreakyForesterInstance())
		{
			if (this.pheasantNPCSet != null && this.pheasantNPCSet.contains(npcDespawned.getNpc()))
			{
				log.debug("A pheasant NPC despawned, removing from the set.");
				this.pheasantNPCSet.remove(npcDespawned.getNpc());
				this.updateSpecificPheasant();
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		if (this.pheasantHighlightMode == PheasantMode.NEAREST && !this.pheasantNPCSet.isEmpty())
		{
			this.updateNearestPheasant();
		}
	}

	private boolean isInFreakyForesterInstance()
	{
		return RandomEventHelperPlugin.getRegionIDFromCurrentLocalPointInstanced(client) == 10314;
	}

	private int convertWordToInt(String word)
	{
		switch (word)
		{
			case "two":
				return 2;
			case "three":
				return 3;
			case "four":
				return 4;
			default:
				return 0;
		}
	}

	private void updateSpecificPheasant()
	{
		if (this.pheasantTailFeathers == 0)
		{
			this.specificPheasantNPC = null;
			return;
		}
		this.specificPheasantNPC = this.pheasantNPCSet.stream().filter(npc -> !npc.isDead() && npc.getId() == PHEASANT_TAIL_NPCID_MAP.getOrDefault(this.pheasantTailFeathers, -1)).min((pheasant1, pheasant2) -> {
			WorldPoint localPlayerWorldPoint = this.client.getLocalPlayer().getWorldLocation();
			return Double.compare(localPlayerWorldPoint.distanceTo2D(pheasant1.getWorldLocation()), localPlayerWorldPoint.distanceTo2D(pheasant2.getWorldLocation()));
		}).orElse(null);
	}

	private void updateNearestPheasant()
	{
		this.nearestPheasantNPC = this.pheasantNPCSet.stream().filter(npc -> !npc.isDead()).min((pheasant1, pheasant2) -> {
			WorldPoint localPlayerWorldPoint = this.client.getLocalPlayer().getWorldLocation();
			return Double.compare(localPlayerWorldPoint.distanceTo2D(pheasant1.getWorldLocation()), localPlayerWorldPoint.distanceTo2D(pheasant2.getWorldLocation()));
		}).orElse(null);
	}
}
