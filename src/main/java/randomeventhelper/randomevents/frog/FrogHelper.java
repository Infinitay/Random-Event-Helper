package randomeventhelper.randomevents.frog;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.gameval.NpcID;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.OverlayManager;
import randomeventhelper.RandomEventHelperConfig;
import randomeventhelper.pluginmodulesystem.PluginModule;

@Slf4j
@Singleton
public class FrogHelper extends PluginModule
{
	@Inject
	private FrogOverlay frogOverlay;

	// https://github.com/runelite/runelite/blob/32a65f18981ef8abdf8369dad59bc1d3679f562b/runelite-client/src/main/java/net/runelite/client/plugins/randomevents/RandomEventPlugin.java#L56
	private final Set<String> EVENT_OPTIONS = ImmutableSet.of(
		"Talk-to",
		"Dismiss"
	);

	@Getter
	private NPC crownedFrogNPC;

	private NPC frogCrierNPC;

	@Getter
	private boolean isEventActiveForPlayer;

	@Inject
	public FrogHelper(OverlayManager overlayManager, RandomEventHelperConfig config, Client client)
	{
		super(overlayManager, config, client);
	}

	@Override
	public void onStartUp()
	{
		this.overlayManager.add(frogOverlay);
		this.crownedFrogNPC = null;
		this.frogCrierNPC = null;
		this.isEventActiveForPlayer = false;
	}

	@Override
	public void onShutdown()
	{
		this.overlayManager.remove(frogOverlay);
		this.crownedFrogNPC = null;
		this.frogCrierNPC = null;
		this.isEventActiveForPlayer = false;
	}

	@Override
	public boolean isEnabled()
	{
		return this.config.isKissTheFrogEnabled();
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		// https://oldschool.runescape.wiki/w/Frog_(Kiss_the_frog)#Crown
		if (npcSpawned.getNpc().getId() == NpcID.MACRO_FROG_SULKING)
		{
			log.debug("Crowned Frog NPC spawned");
			if (this.crownedFrogNPC != null)
			{
				log.debug("Another Crowned Frog NPC has spawned, checking to see which is closer to the local player");
				WorldPoint localPlayerWorldPoint = this.client.getLocalPlayer().getWorldLocation();
				int existingCrownedFrogDistanceToPlayer = this.crownedFrogNPC.getWorldLocation().distanceTo(localPlayerWorldPoint);
				int newCrownedFrogDistanceToPlayer = npcSpawned.getNpc().getWorldLocation().distanceTo(localPlayerWorldPoint);
				if (newCrownedFrogDistanceToPlayer < existingCrownedFrogDistanceToPlayer)
				{
					log.debug("New Crowned Frog NPC is closer to the player therefore replacing the existing one");
				}
				else
				{
					log.debug("Existing Crowned Frog NPC is closer to the player therefore ignoring the new one");
					return;
				}
			}
			log.debug("Found Crowned Frog NPC on spawn");
			this.crownedFrogNPC = npcSpawned.getNpc();
			this.checkIsEventActiveForPlayer();
		}

		if (npcSpawned.getNpc().getId() == NpcID.MACRO_FROG_CRIER)
		{
			log.debug("Frog Crier NPC spawned | interacting: {}", npcSpawned.getNpc().isInteracting() ? npcSpawned.getNpc().getInteracting().getName() : "NULL");
			// From basic tests, the Frog Crier when spawned is not interacting with the player (NULL), so this shouldn't even be true... but you never know with race conditions
			if (npcSpawned.getNpc().isInteracting() && npcSpawned.getNpc().getInteracting().equals(this.client.getLocalPlayer()))
			{
				log.debug("Found Frog Crier NPC interacting with the local player on spawn");
				this.frogCrierNPC = npcSpawned.getNpc();
				this.checkIsEventActiveForPlayer();
			}
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		if (this.crownedFrogNPC != null && npcDespawned.getNpc().equals(this.crownedFrogNPC))
		{
			log.debug("Known Crowned Frog NPC despawned");
			this.crownedFrogNPC = null;
			this.checkIsEventActiveForPlayer();
		}

		if (this.frogCrierNPC != null && npcDespawned.getNpc().equals(this.frogCrierNPC))
		{
			log.debug("Known Frog Crier NPC despawned");
			this.frogCrierNPC = null;
			this.checkIsEventActiveForPlayer();
		}
	}

	@Subscribe
	public void onInteractingChanged(InteractingChanged interactingChanged)
	{
		if (interactingChanged.getSource() instanceof NPC)
		{
			NPC interactingSourceNPC = (NPC) interactingChanged.getSource();
			if (interactingSourceNPC.getId() == NpcID.MACRO_FROG_CRIER)
			{
				Actor interactingTargetActor = interactingChanged.getTarget();
				if (interactingTargetActor != null && interactingTargetActor.equals(this.client.getLocalPlayer()))
				{
					log.debug("Frog Crier NPC is now interacting with the local player");
					this.frogCrierNPC = interactingSourceNPC;
					this.checkIsEventActiveForPlayer();
				}
				else if (this.frogCrierNPC != null && interactingSourceNPC.equals(this.frogCrierNPC) && (interactingTargetActor == null))
				{
					log.debug("Frog Crier NPC is no longer interacting with the local player");
					this.frogCrierNPC = null;
				}
			}
		}
	}

	/**
	 * Checks whether the spawned event is valid by confirming if there is a frogCrierNPC and crownedFrogNPC
	 */
	private boolean checkIsEventActiveForPlayer()
	{
		if (this.frogCrierNPC != null && this.crownedFrogNPC != null)
		{
			log.debug("Found both the Frog Crier NPC and Crowned Frog NPC which indicates the Kiss the Frog random event is for the current player");
			this.isEventActiveForPlayer = true;
			return true;
		}
		else if (this.crownedFrogNPC != null && this.isInFrogLandInstance())
		{
			log.debug("Found the Crowned Frog NPC and the player is in the Frog Land instance which indicates the Kiss the Frog random event is for the current player");
			this.isEventActiveForPlayer = true;
			return true;
		}
		else
		{
			log.debug("Could not find both the Frog Crier NPC and Crowned Frog NPC which indicates the Kiss the Frog random event is not active for the current player");
			this.isEventActiveForPlayer = false;
			return false;
		}
	}

	private boolean isInFrogLandInstance()
	{
		return RandomEventHelperPlugin.getRegionIDFromCurrentLocalPointInstanced(client) == 9802;
	}
}
