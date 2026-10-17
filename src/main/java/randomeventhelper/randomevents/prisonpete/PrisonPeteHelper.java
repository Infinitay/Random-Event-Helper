package randomeventhelper.randomevents.prisonpete;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;
import randomeventhelper.RandomEventHelperConfig;
import randomeventhelper.RandomEventHelperPlugin;
import randomeventhelper.pluginmodulesystem.PluginModule;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
public class PrisonPeteHelper extends PluginModule
{
	@Inject
	private ClientThread clientThread;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private PrisonPeteOverlay prisonPeteOverlay;

	@Inject
	private PrisonPeteItemOverlay prisonPeteItemOverlay;

	@Getter
	private BalloonAnimal correctBalloonAnimal;

	@Getter
	private Set<NPC> correctBalloonAnimalNPCSet;

	private Map<NPC, BalloonAnimal> balloonAnimalNPCMap;

	@Getter
	private NPC prisonPeteNPC;

	@Getter
	private GameObject lever;

	@Getter
	private boolean isKeyInInventory;

	private NPC lastInteractedWithBalloonAnimalNPC;

	@Getter
	private boolean isLastPoppedBalloonAnimalCorrect;

	@Getter
	private Set<GameObject> energyBarriers;

	@Getter
	private boolean isBarriersDispelled;

	@Inject
	public PrisonPeteHelper(OverlayManager overlayManager, RandomEventHelperConfig config, Client client)
	{
		super(overlayManager, config, client);
	}

	@Override
	public void onStartUp()
	{
		this.overlayManager.add(prisonPeteOverlay);
		this.overlayManager.add(prisonPeteItemOverlay);
		this.initializeState();
		if (this.isLoggedIn())
		{
			this.clientThread.invokeLater(() -> {
				if (this.client.getWidget(InterfaceID.Prisonpete.BALLOON1) != null)
				{
					WidgetLoaded prisonPeteKeyHolderWidgetLoaded = new WidgetLoaded();
					prisonPeteKeyHolderWidgetLoaded.setGroupId(InterfaceID.PRISONPETE);
					this.eventBus.post(prisonPeteKeyHolderWidgetLoaded);
				}
			});
		}
	}

	@Override
	public void onShutdown()
	{
		this.overlayManager.remove(prisonPeteOverlay);
		this.overlayManager.remove(prisonPeteItemOverlay);
		this.correctBalloonAnimal = null;
		this.correctBalloonAnimalNPCSet = null;
		this.balloonAnimalNPCMap = null;
		this.prisonPeteNPC = null;
		this.lever = null;
		this.isKeyInInventory = false;
		this.energyBarriers = null;
		this.isBarriersDispelled = false;
	}

	@Override
	public boolean isEnabled()
	{
		return this.config.isPrisonPeteEnabled();
	}

	private void initializeState()
	{
		this.correctBalloonAnimal = null;
		this.correctBalloonAnimalNPCSet = Sets.newHashSet();
		this.balloonAnimalNPCMap = Maps.newHashMap();
		this.prisonPeteNPC = null;
		this.lever = null;
		this.isKeyInInventory = false;
		this.energyBarriers = Sets.newHashSet();
		this.isBarriersDispelled = false;
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		if (inPrisonRegion() && event.getGameObject().getId() == ObjectID.FAI_FALADOR_PARTY_NEG_ROOM_LEVER)
		{
			this.lever = event.getGameObject();
		}
		switch (event.getGameObject().getId())
		{
			case ObjectID.PRISONPETE_DOOR_1:
			case ObjectID.PRISONPETE_DOOR_2:
			case ObjectID.PRISONPETE_DOOR_3:
				log.debug("Energy barrier spawned: (ID: {})", event.getGameObject().getId());
				this.energyBarriers.add(event.getGameObject());
				this.isBarriersDispelled = false;
				break;
		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		if (inPrisonRegion() && event.getGameObject().getId() == ObjectID.FAI_FALADOR_PARTY_NEG_ROOM_LEVER)
		{
			// Shouldn't happen, but just in case
			this.lever = null;
			log.debug("Lever despawned: (ID: {})", event.getGameObject().getId());
		}
		switch (event.getGameObject().getId())
		{
			case ObjectID.PRISONPETE_DOOR_1:
			case ObjectID.PRISONPETE_DOOR_2:
			case ObjectID.PRISONPETE_DOOR_3:
				log.debug("Energy barrier despawned: (ID: {})", event.getGameObject().getId());
				this.energyBarriers.remove(event.getGameObject());
				break;
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		if (!inPrisonRegion())
		{
			return;
		}
		if (BalloonAnimal.fromNpcID(npcSpawned.getNpc().getId()) != null)
		{
			this.balloonAnimalNPCMap.put(npcSpawned.getNpc(), BalloonAnimal.fromNpcID(npcSpawned.getNpc().getId()));
		}
		else if (npcSpawned.getNpc().getId() == NpcID.PRISONPETE_PETE)
		{
			this.prisonPeteNPC = npcSpawned.getNpc();
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		if (!inPrisonRegion() && npcDespawned.getNpc().getId() == NpcID.PRISONPETE_PETE)
		{
			log.debug("Prison Pete NPC despawned, resetting state.");
			this.correctBalloonAnimal = null;
			this.correctBalloonAnimalNPCSet.clear();
			this.balloonAnimalNPCMap.clear();
			this.prisonPeteNPC = null;
			this.lever = null;
			this.isKeyInInventory = false;
			this.energyBarriers.clear();
			this.isBarriersDispelled = false;
		}
		else if (inPrisonRegion() && npcDespawned.getNpc().equals(this.lastInteractedWithBalloonAnimalNPC) && BalloonAnimal.fromNpcID(npcDespawned.getNpc().getId()) != null)
		{
			this.balloonAnimalNPCMap.remove(npcDespawned.getNpc());
			if (this.correctBalloonAnimal != null && this.correctBalloonAnimal.isMatch(npcDespawned.getNpc()))
			{
				log.debug("Correct balloon animal NPC despawned, player has the correct key");
				this.isLastPoppedBalloonAnimalCorrect = true;
				this.correctBalloonAnimalNPCSet.remove(npcDespawned.getNpc());
			}
			else
			{
				log.debug("Wrong balloon animal NPC despawned, player does not have the correct key");
				this.isLastPoppedBalloonAnimalCorrect = false;
			}
		}
	}

	@Subscribe
	public void onInteractingChanged(InteractingChanged interactingChanged)
	{
		if (interactingChanged.getSource() instanceof Player && interactingChanged.getSource().equals(this.client.getLocalPlayer()) && interactingChanged.getTarget() instanceof NPC)
		{
			NPC interactedNPC = (NPC) interactingChanged.getTarget();
			if (BalloonAnimal.fromNpcID(interactedNPC.getId()) != null)
			{
				this.lastInteractedWithBalloonAnimalNPC = interactedNPC;
				log.debug("Player is now interacting with balloon animal NPC: {} (ID: {})", interactedNPC.getName(), interactedNPC.getId());
			}
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (inPrisonRegion() && event.getContainerId() == InventoryID.INV)
		{
			// Don't make any assumptions about whether the player turned in the key or not, so don't reset here
			this.isKeyInInventory = event.getItemContainer().contains(ItemID.PRISONPETE_KEY);

			if (this.lastInteractedWithBalloonAnimalNPC != null)
			{
				if (this.correctBalloonAnimal != null && this.correctBalloonAnimal.isMatch(this.lastInteractedWithBalloonAnimalNPC))
				{
					log.debug("Correct balloon animal NPC despawned, player has the correct key");
					this.isLastPoppedBalloonAnimalCorrect = true;
				}
				else
				{
					log.debug("Wrong balloon animal NPC despawned, player does not have the correct key");
					this.isLastPoppedBalloonAnimalCorrect = false;
				}
			}
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		String sanitizedChatMessage = Text.sanitizeMultilineText(chatMessage.getMessage());
		if (chatMessage.getType() == ChatMessageType.DIALOG)
		{
			if (this.inPrisonRegion() && sanitizedChatMessage.startsWith("Prison Pete"))
			{
				sanitizedChatMessage = sanitizedChatMessage.replace("Prison Pete|", "");
				boolean shouldResetCorrectBalloonAnimal = false;

				if (sanitizedChatMessage.startsWith("Aww"))
				{
					log.debug("Player gave the wrong key to Prison Pete, resetting state.");
					shouldResetCorrectBalloonAnimal = true;
				}
				else if (sanitizedChatMessage.startsWith("Hooray"))
				{
					log.debug("Player gave the correct key to Prison Pete, resetting state.");
					shouldResetCorrectBalloonAnimal = true;
				}
				else if (sanitizedChatMessage.startsWith("You did it") || sanitizedChatMessage.startsWith("Come on"))
				{
					log.debug("Player finished the Prison Pete random event");
					shouldResetCorrectBalloonAnimal = true;
					this.lastInteractedWithBalloonAnimalNPC = null;
					this.balloonAnimalNPCMap.clear();
					this.lever = null;
					this.isKeyInInventory = false;
					this.isBarriersDispelled = true;
				}

				if (shouldResetCorrectBalloonAnimal)
				{
					this.correctBalloonAnimal = null;
					this.correctBalloonAnimalNPCSet.clear();
				}
			}
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded)
	{
		if (widgetLoaded.getGroupId() == InterfaceID.PRISONPETE)
		{
			log.debug("Player opened balloon lever widget");
			this.clientThread.invokeLater(() -> {
				Widget balloonWidget = this.client.getWidget(InterfaceID.Prisonpete.BALLOON1);
				if (balloonWidget != null && !balloonWidget.isHidden())
				{
					this.correctBalloonAnimal = BalloonAnimal.fromWidgetModel(balloonWidget.getModelId());
					if (this.correctBalloonAnimal != null)
					{
						log.debug("Identified the correct balloon animal as {}", this.correctBalloonAnimal);
						this.balloonAnimalNPCMap.entrySet().stream()
							.filter(npcBalloonAnimalEntry -> npcBalloonAnimalEntry.getValue().equals(this.correctBalloonAnimal))
							.map(Map.Entry::getKey)
							.forEach(this.correctBalloonAnimalNPCSet::add);
					}
					else
					{
						log.debug("Could not identify the correct balloon animal from model ID {}", balloonWidget.getModelId());
					}
				}
			});
		}
	}

	public boolean inPrisonRegion()
	{
		return RandomEventHelperPlugin.getRegionIDFromCurrentLocalPointInstanced(client) == 8261;
	}
}
