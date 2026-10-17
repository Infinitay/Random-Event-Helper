package randomeventhelper.randomevents.prisonpete;

import java.awt.geom.Area;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import randomeventhelper.RandomEventHelperConfig;
import randomeventhelper.RandomEventHelperOverlay;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;

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
		if (this.plugin.getPrisonPeteNPC() == null)
		{
			return null;
		}

		// Player doesn't have a key and the correct balloon animal has been identified -> Highlight the correct balloon animals
		if (plugin.getCorrectBalloonAnimal() != null && !plugin.isKeyInInventory())
		{
			for (NPC correctBalloonAnimalNPC : this.plugin.getCorrectBalloonAnimalNPCSet())
			{
				if (correctBalloonAnimalNPC != null)
				{
					RandomEventHelperOverlay.renderOverlayWithFill(graphics2D, correctBalloonAnimalNPC.getConvexHull(), this.config.borderColor(), this.config.fillColor());
				}
			}
		}

		// Player doesn't have a key and the correct balloon animal has not been identified -> Highlight the lever
		if (plugin.getLever() != null && !plugin.isKeyInInventory() && plugin.getCorrectBalloonAnimal() == null)
		{
			RandomEventHelperOverlay.renderOverlayWithFill(graphics2D, plugin.getLever().getConvexHull(), this.config.borderColor(), this.config.fillColor());
		}

		// Player has a key in their inventory -> Highlight Prison Pete
		if (plugin.isKeyInInventory() && this.plugin.getPrisonPeteNPC() != null)
		{
			RandomEventHelperOverlay.renderOverlayWithFill(graphics2D, this.plugin.getPrisonPeteNPC().getConvexHull(), this.config.borderColor(), this.config.fillColor());
		}

		// Player has dispelled all the barriers and they can exit the event area
		if (plugin.isBarriersDispelled() && this.plugin.getEnergyBarriers() != null)
		{
			Area energyBarrierArea = new Area();
			for (GameObject energyBarrier : plugin.getEnergyBarriers())
			{
				if (energyBarrier != null && energyBarrier.getCanvasTilePoly() != null)
				{
					energyBarrierArea.add(new Area(energyBarrier.getCanvasTilePoly()));
				}
			}

			if (!energyBarrierArea.isEmpty())
			{
				RandomEventHelperOverlay.renderOverlayWithFill(graphics2D, energyBarrierArea, config.borderColor(), config.fillColor());
			}
		}

		return null;
	}
}
