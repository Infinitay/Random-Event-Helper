package randomeventhelper;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

public class RandomEventHelperOverlay extends Overlay
{

	private final Client client;
	private final SpriteManager spriteManager;
	private final RandomEventHelperPlugin plugin;
	private final ModelOutlineRenderer modelOutlineRenderer;

	@Inject
	public RandomEventHelperOverlay(Client client, RandomEventHelperPlugin plugin, ModelOutlineRenderer modelOutlineRenderer, SpriteManager spriteManager)
	{
		this.client = client;
		this.plugin = plugin;
		this.modelOutlineRenderer = modelOutlineRenderer;
		this.spriteManager = spriteManager;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics2D)
	{
		return null;
	}
}
