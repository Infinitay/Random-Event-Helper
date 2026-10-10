package randomeventhelper.randomevents.prisonpete;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.OverlayManager;
import randomeventhelper.RandomEventHelperConfig;
import randomeventhelper.RandomEventHelperPlugin;
import randomeventhelper.pluginmodulesystem.PluginModule;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.stream.Collectors;

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

	@Getter
	private BalloonAnimal highlightedAnimal;

	@Getter
	private GameObject lever;

	@Getter
	private boolean invHasKey;


	@Inject
	public PrisonPeteHelper(OverlayManager overlayManager, RandomEventHelperConfig config, Client client)
	{
		super(overlayManager, config, client);
	}

	private void reset()
	{
		highlightedAnimal = null;
		lever = null;
		invHasKey = false;
	}

	@Override
	public void onStartUp()
	{
		this.overlayManager.add(prisonPeteOverlay);
		reset();
		if (this.isLoggedIn() )
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
		this.eventBus.unregister(this);
		this.overlayManager.remove(prisonPeteOverlay);
		reset();
	}

	@Override
	public boolean isEnabled()
	{
		return this.config.isPrisonPeteEnabled();
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		if (inPrisonRegion() && event.getGameObject().getId() == ObjectID.FAI_FALADOR_PARTY_NEG_ROOM_LEVER)
		{
			lever = event.getGameObject();
		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		if (lever == event.getGameObject())
		{
			reset();
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (inPrisonRegion() && event.getContainerId() == InventoryID.INV)
		{
			invHasKey = event.getItemContainer().contains(ItemID.PRISONPETE_KEY);
			highlightedAnimal = (invHasKey) ? null : highlightedAnimal;
		}
	}

	public boolean inPrisonRegion()
	{
		return RandomEventHelperPlugin.getRegionIDFromCurrentLocalPointInstanced(client) == 8261;
	}

	public int getEnergyBarriersLeft()
	{
		return (int) Arrays.stream(client.getScene().getTiles()[0]).flatMap(j -> Arrays.stream(j)).flatMap(j -> Arrays.stream(j.getGameObjects()))
			.filter(obj -> obj != null)
			.distinct()
			.map(obj -> client.getObjectDefinition(obj.getId()))
			.filter(od -> od.getImpostorIds() != null)
			.map(od -> od.getImpostor())
			.filter(obj -> obj != null)
			.filter(od -> od.getId() == 20839).count();
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
					this.highlightedAnimal = BalloonAnimal.fromWidgetModel(balloonWidget.getModelId());
				}
			});
		}
	}
}
