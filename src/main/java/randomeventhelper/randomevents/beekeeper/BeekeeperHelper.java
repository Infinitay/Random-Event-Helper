package randomeventhelper.randomevents.beekeeper;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.OverlayManager;
import randomeventhelper.RandomEventHelperConfig;
import randomeventhelper.pluginmodulesystem.PluginModule;

@Slf4j
@Singleton
public class BeekeeperHelper extends PluginModule
{
	@Inject
	private ClientThread clientThread;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private BeekeeperOverlay beekeeperOverlay;

	@Getter
	private ImmutableList<Widget> beehiveAnswerWidgets;

	@Inject
	public BeekeeperHelper(OverlayManager overlayManager, RandomEventHelperConfig config, Client client)
	{
		super(overlayManager, config, client);
	}

	@Override
	public void onStartUp()
	{
		this.overlayManager.add(beekeeperOverlay);
		this.beehiveAnswerWidgets = null;
	}

	@Override
	public void onShutdown()
	{
		this.overlayManager.remove(beekeeperOverlay);
		this.beehiveAnswerWidgets = null;
	}

	@Override
	public boolean isEnabled()
	{
		return this.config.isBeekeeperEnabled();
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded)
	{
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
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed widgetClosed)
	{
		if (widgetClosed.getGroupId() == InterfaceID.BEEHIVE)
		{
			log.debug("Beehive widget closed, resetting beehive answer widgets.");
			this.beehiveAnswerWidgets = null;
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		if (npcDespawned.getNpc().getId() == NpcID.MACRO_BEEKEEPER)
		{
			log.debug("Beekeeper NPC despawned, resetting beehive answer widgets.");
			this.beehiveAnswerWidgets = null;
		}
	}
}
