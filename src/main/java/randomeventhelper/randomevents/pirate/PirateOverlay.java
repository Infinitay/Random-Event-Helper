package randomeventhelper.randomevents.pirate;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import randomeventhelper.RandomEventHelperConfig;
import randomeventhelper.RandomEventHelperOverlay;

@Slf4j
@Singleton
public class PirateOverlay extends Overlay
{
	private final Client client;
	private final RandomEventHelperConfig config;
	private final PirateHelper plugin;

	@Inject
	public PirateOverlay(Client client, RandomEventHelperConfig config, PirateHelper plugin)
	{
		this.client = client;
		this.config = config;
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics2D)
	{
		if (plugin.getPirateChestSolver() == null || plugin.getWidgetMap() == null || plugin.getWidgetMap().isEmpty())
		{
			return null;
		}

		if (plugin.getPirateChestSolver().isSolved())
		{
			RandomEventHelperOverlay.renderOverlayWithFill(graphics2D, this.plugin.getWidgetMap().get(PirateHelper.CONFIRM_BUTTON_WIDGET_ID).getBounds(), this.config.borderColor(), this.config.fillColor());
		}
		else
		{
			Widget leftActionWidget = plugin.getWidgetMap().get(plugin.getPirateChestSolver().getLeftSlotUseWidget());
			Widget centerActionWidget = plugin.getWidgetMap().get(plugin.getPirateChestSolver().getCenterSlotUseWidget());
			Widget rightActionWidget = plugin.getWidgetMap().get(plugin.getPirateChestSolver().getRightSlotUseWidget());
			if (leftActionWidget != null)
			{
				RandomEventHelperOverlay.renderOverlayWithFill(graphics2D, leftActionWidget.getBounds(), this.config.borderColor(), this.config.fillColor());
			}
			if (centerActionWidget != null)
			{
				RandomEventHelperOverlay.renderOverlayWithFill(graphics2D, centerActionWidget.getBounds(), this.config.borderColor(), this.config.fillColor());
			}
			if (rightActionWidget != null)
			{
				RandomEventHelperOverlay.renderOverlayWithFill(graphics2D, rightActionWidget.getBounds(), this.config.borderColor(), this.config.fillColor());
			}
		}
		return null;
	}
}
