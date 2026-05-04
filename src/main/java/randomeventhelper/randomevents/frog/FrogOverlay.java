package randomeventhelper.randomevents.frog;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import randomeventhelper.RandomEventHelperConfig;
import randomeventhelper.RandomEventHelperOverlay;

public class FrogOverlay extends Overlay
{
	private final Client client;
	private final RandomEventHelperConfig config;
	private final FrogHelper plugin;

	@Inject
	public FrogOverlay(Client client, RandomEventHelperConfig config, FrogHelper plugin)
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
		if (this.plugin.isEventActiveForPlayer() && this.plugin.getCrownedFrogNPC() != null)
		{
			RandomEventHelperOverlay.renderOverlayWithFill(graphics2D, this.plugin.getCrownedFrogNPC().getConvexHull(), this.config.borderColor(), this.config.fillColor());
		}
		return null;
	}
}
