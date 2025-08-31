package randomeventhelper.randomevents.sandwichlady;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

@Slf4j
@Singleton
public class SandwichLadyOverlay extends Overlay
{
	private final Client client;
	private final SandwichLadyHelper plugin;

	@Inject
	public SandwichLadyOverlay(Client client, SandwichLadyHelper plugin)
	{
		this.client = client;
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics2D)
	{
		if (plugin.getTrayFoodAnswerWidget() != null && !plugin.getTrayFoodAnswerWidget().isHidden())
		{
			OverlayUtil.renderPolygon(graphics2D, plugin.getTrayFoodAnswerWidget().getBounds(), Color.GREEN);
		}
		return null;
	}
}
