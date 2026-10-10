package randomeventhelper.randomevents.prisonpete;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.gameval.NpcID;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import randomeventhelper.RandomEventHelperConfig;
import randomeventhelper.RandomEventHelperOverlay;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;

@Slf4j
@Singleton
public class PrisonPeteOverlay extends Overlay
{
	private final Client client;
	private final RandomEventHelperConfig config;
	private final PrisonPeteHelper plugin;

	@Inject
	public PrisonPeteOverlay(Client client, RandomEventHelperConfig config, PrisonPeteHelper plugin)
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
		if (!plugin.inPrisonRegion()) return null;
		if (plugin.getEnergyBarriersLeft() == 0) return null;

		if (plugin.getHighlightedAnimal() != null && !plugin.isInvHasKey())
		{
			client.getTopLevelWorldView().npcs().stream().filter(npc -> npc.getId() == plugin.getHighlightedAnimal().getNpcId())
				.map(npc -> npc.getConvexHull())
				.forEach(shape -> RandomEventHelperOverlay.renderOverlayWithFill(graphics2D, shape, this.config.borderColor(), this.config.fillColor()));
		}

		if (plugin.getLever() != null && !plugin.isInvHasKey() && plugin.getHighlightedAnimal() == null)
		{
			RandomEventHelperOverlay.renderOverlayWithFill(graphics2D, plugin.getLever().getConvexHull(), this.config.borderColor(), this.config.fillColor());
		}

		if (plugin.isInvHasKey())
		{
			client.getTopLevelWorldView().npcs().stream().filter(npc -> npc.getId() == NpcID.PRISONPETE_PETE)
				.map(npc -> npc.getConvexHull())
				.forEach(shape -> RandomEventHelperOverlay.renderOverlayWithFill(graphics2D, shape, this.config.borderColor(), this.config.fillColor()));
		}

		return null;
	}
}
