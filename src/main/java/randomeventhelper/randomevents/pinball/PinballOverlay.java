package randomeventhelper.randomevents.pinball;

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
public class PinballOverlay extends Overlay
{
	private final Client client;
	private final RandomEventHelperConfig config;
	private final PinballHelper plugin;

	@Inject
	public PinballOverlay(Client client, RandomEventHelperConfig config, PinballHelper plugin)
	{
		this.client = client;
		this.config = config;
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics2D)
	{
		if (plugin.getActivePinballPost() != null)
		{
			RandomEventHelperOverlay.renderOverlayWithFill(graphics2D, this.plugin.getActivePinballPost().getConvexHull(), this.config.borderColor(), this.config.fillColor());
		}
		return null;
	}
}
