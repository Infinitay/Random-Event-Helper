package randomeventhelper.randomevents.sandwichlady;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import randomeventhelper.RandomEventHelperConfig;
import randomeventhelper.RandomEventHelperOverlay;

@Slf4j
@Singleton
public class SandwichLadyOverlay extends Overlay
{
	private final Client client;
	private final RandomEventHelperConfig config;
	private final SandwichLadyHelper plugin;

	@Inject
	public SandwichLadyOverlay(Client client, RandomEventHelperConfig config, SandwichLadyHelper plugin)
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
		if (plugin.getTrayFoodAnswerWidget() != null && !plugin.getTrayFoodAnswerWidget().isHidden())
		{
			RandomEventHelperOverlay.renderOverlayWithFill(graphics2D, this.plugin.getTrayFoodAnswerWidget().getBounds(), this.config.borderColor(), this.config.fillColor());
		}
		return null;
	}
}
