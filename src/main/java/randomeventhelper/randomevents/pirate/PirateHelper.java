package randomeventhelper.randomevents.pirate;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;
import randomeventhelper.RandomEventHelperConfig;
import randomeventhelper.RandomEventHelperPlugin;
import randomeventhelper.pluginmodulesystem.PluginModule;

@Slf4j
@Singleton
public class PirateHelper extends PluginModule
{
	@Inject
	private ClientThread clientThread;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private PirateOverlay pirateOverlay;

	public static final int CONFIRM_BUTTON_WIDGET_ID = InterfaceID.PirateCombilock.CONFIRM;

	@Getter
	private PirateChestSolver pirateChestSolver;

	private boolean initiallyLoaded;

	@Getter
	private Map<Integer, Widget> widgetMap;

	@Inject
	public PirateHelper(OverlayManager overlayManager, RandomEventHelperConfig config, Client client)
	{
		super(overlayManager, config, client);
	}

	@Override
	public void onStartUp()
	{
		this.overlayManager.add(pirateOverlay);
		this.pirateChestSolver = new PirateChestSolver();
		this.initiallyLoaded = false;
		this.widgetMap = Maps.newHashMap();

		if (this.isLoggedIn())
		{
			this.clientThread.invokeLater(() -> {
				if (this.client.getWidget(this.CONFIRM_BUTTON_WIDGET_ID) != null)
				{
					WidgetLoaded chestConfirmButtonWidgetLoaded = new WidgetLoaded();
					chestConfirmButtonWidgetLoaded.setGroupId(InterfaceID.PIRATE_COMBILOCK);
					this.eventBus.post(chestConfirmButtonWidgetLoaded);
				}
			});
		}
	}

	@Override
	public void onShutdown()
	{
		this.overlayManager.remove(pirateOverlay);
		this.pirateChestSolver = null;
		this.initiallyLoaded = false;
		this.widgetMap = null;
	}

	@Override
	public boolean isEnabled()
	{
		return this.config.isCaptArnavChestEnabled();
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged varbitChanged)
	{
		// For some reason, when the player is within the maze random event, the varbits for the pirate chest event fire/are modified
		if (this.isInMazeLocalInstance())
		{
			return;
		}

		switch (varbitChanged.getVarbitId())
		{
			case VarbitID.PIRATE_COMBILOCK_LEFT:
			case VarbitID.PIRATE_COMBILOCK_CENTRE:
			case VarbitID.PIRATE_COMBILOCK_RIGHT:
				if (this.pirateChestSolver == null)
				{
					this.pirateChestSolver = new PirateChestSolver();
				}
				this.pirateChestSolver.updateActiveItem(varbitChanged.getVarbitId(), varbitChanged.getValue());
				if (!this.pirateChestSolver.isChestCorrectlySet())
				{
					log.debug("[#onVarbitChanged] Attempting to solve pirate chest lock");
					this.pirateChestSolver.solve();
				}
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded)
	{
		if (widgetLoaded.getGroupId() == InterfaceID.PIRATE_COMBILOCK)
		{
			log.info("Pirate chest random event detected");
			if (!this.initiallyLoaded)
			{
				this.initiallyLoaded = true;
				if (this.pirateChestSolver != null)
				{
					this.pirateChestSolver.reset();
				}
				else
				{
					this.pirateChestSolver = new PirateChestSolver();
				}
				VarbitChanged tempLeft = new VarbitChanged();
				tempLeft.setVarbitId(VarbitID.PIRATE_COMBILOCK_LEFT);
				tempLeft.setValue(this.client.getVarbitValue(VarbitID.PIRATE_COMBILOCK_LEFT));
				VarbitChanged tempCenter = new VarbitChanged();
				tempCenter.setVarbitId(VarbitID.PIRATE_COMBILOCK_CENTRE);
				tempCenter.setValue(this.client.getVarbitValue(VarbitID.PIRATE_COMBILOCK_CENTRE));
				VarbitChanged tempRight = new VarbitChanged();
				tempRight.setVarbitId(VarbitID.PIRATE_COMBILOCK_RIGHT);
				tempRight.setValue(this.client.getVarbitValue(VarbitID.PIRATE_COMBILOCK_RIGHT));
				this.onVarbitChanged(tempLeft);
				this.onVarbitChanged(tempCenter);
				this.onVarbitChanged(tempRight);
			}
			this.clientThread.invokeLater(() -> {
				if (this.client.getWidget(this.CONFIRM_BUTTON_WIDGET_ID) != null)
				{
					Widget leftLabelWidget = this.client.getWidget(ChestLockSlot.LEFT.getRequiredItemLabelWidgetID());
					Widget centerLabelWidget = this.client.getWidget(ChestLockSlot.CENTER.getRequiredItemLabelWidgetID());
					Widget rightLabelWidget = this.client.getWidget(ChestLockSlot.RIGHT.getRequiredItemLabelWidgetID());
					if (leftLabelWidget != null && centerLabelWidget != null && rightLabelWidget != null)
					{
						String leftItemString = Text.sanitizeMultilineText(leftLabelWidget.getText());
						String centerItemString = Text.sanitizeMultilineText(centerLabelWidget.getText());
						String rightItemString = Text.sanitizeMultilineText(rightLabelWidget.getText());
						ChestLockItem leftChestLockItem = ChestLockItem.fromLabel(leftItemString);
						ChestLockItem centerChestLockItem = ChestLockItem.fromLabel(centerItemString);
						ChestLockItem rightChestLockItem = ChestLockItem.fromLabel(rightItemString);
						log.info("Pirate Chest Lock combination: Left: {}, Center: {}, Right: {}", leftChestLockItem, centerChestLockItem, rightChestLockItem);
						if (leftChestLockItem != null && centerChestLockItem != null && rightChestLockItem != null)
						{
							this.pirateChestSolver.updateRequiredItem(ChestLockSlot.LEFT, leftChestLockItem);
							this.pirateChestSolver.updateRequiredItem(ChestLockSlot.CENTER, centerChestLockItem);
							this.pirateChestSolver.updateRequiredItem(ChestLockSlot.RIGHT, rightChestLockItem);
							if (!this.pirateChestSolver.isChestCorrectlySet())
							{
								log.debug("[#onWidgetLoaded] Attempting to solve pirate chest lock");
								this.pirateChestSolver.solve();
							}
						}
						else
						{
							log.warn("Failed to retrieve one or more chest lock items");
							log.debug("Left item string: {}, Center item string: {}, Right item string: {}", leftItemString, centerItemString, rightItemString);
						}
					}
					this.populateWidgetsMap();
					this.widgetMap.put(this.CONFIRM_BUTTON_WIDGET_ID, this.client.getWidget(this.CONFIRM_BUTTON_WIDGET_ID));
				}
			});
		}
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed widgetClosed)
	{
		if (widgetClosed.getGroupId() == InterfaceID.PIRATE_COMBILOCK)
		{
			log.info("Pirate chest random event closed");
			this.initiallyLoaded = false;
			if (this.pirateChestSolver != null)
			{
				this.pirateChestSolver.reset();
				this.pirateChestSolver = null;
			}
		}
	}

	private void populateWidgetsMap()
	{
		Widget leftAddWidget = this.client.getWidget(ChestLockSlot.LEFT.getAddWidgetID());
		Widget leftSubtractWidget = this.client.getWidget(ChestLockSlot.LEFT.getSubtractWidgetID());
		Widget centerAddWidget = this.client.getWidget(ChestLockSlot.CENTER.getAddWidgetID());
		Widget centerSubtractWidget = this.client.getWidget(ChestLockSlot.CENTER.getSubtractWidgetID());
		Widget rightAddWidget = this.client.getWidget(ChestLockSlot.RIGHT.getAddWidgetID());
		Widget rightSubtractWidget = this.client.getWidget(ChestLockSlot.RIGHT.getSubtractWidgetID());
		if (leftAddWidget != null)
		{
			this.widgetMap.put(ChestLockSlot.LEFT.getAddWidgetID(), leftAddWidget);
		}
		if (leftSubtractWidget != null)
		{
			this.widgetMap.put(ChestLockSlot.LEFT.getSubtractWidgetID(), leftSubtractWidget);
		}
		if (centerAddWidget != null)
		{
			this.widgetMap.put(ChestLockSlot.CENTER.getAddWidgetID(), centerAddWidget);
		}
		if (centerSubtractWidget != null)
		{
			this.widgetMap.put(ChestLockSlot.CENTER.getSubtractWidgetID(), centerSubtractWidget);
		}
		if (rightAddWidget != null)
		{
			this.widgetMap.put(ChestLockSlot.RIGHT.getAddWidgetID(), rightAddWidget);
		}
		if (rightSubtractWidget != null)
		{
			this.widgetMap.put(ChestLockSlot.RIGHT.getSubtractWidgetID(), rightSubtractWidget);
		}
	}

	private boolean isInMazeLocalInstance()
	{
		return RandomEventHelperPlugin.getRegionIDFromCurrentLocalPointInstanced(client) == 11591;
	}
}
