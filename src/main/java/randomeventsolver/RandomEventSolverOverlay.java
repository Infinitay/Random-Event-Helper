package randomeventsolver;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Objects;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GroundObject;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;
import randomeventsolver.data.Coffin;
import randomeventsolver.data.Grave;

public class RandomEventSolverOverlay extends Overlay
{

	private final Client client;
	private final SpriteManager spriteManager;
	private final RandomEventSolverPlugin plugin;
	private final ModelOutlineRenderer modelOutlineRenderer;

	private BufferedImage checkBufferedImage;
	private BufferedImage crossBufferedImage;

	@Inject
	public RandomEventSolverOverlay(Client client, RandomEventSolverPlugin plugin, ModelOutlineRenderer modelOutlineRenderer, SpriteManager spriteManager)
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
		if (plugin.getActivePinballPost() != null)
		{
			OverlayUtil.renderPolygon(graphics2D, plugin.getActivePinballPost().getConvexHull(), Color.GREEN);
		}

		if (plugin.getExerciseMatsAnswerList() != null && !plugin.getExerciseMatsAnswerList().isEmpty())
		{
			// Get the non-null ground objects, map it to their convex hulls, and then combine them into a single shape
			Area combinedMatHull = new Area();
			plugin.getExerciseMatsAnswerList().stream().filter(Objects::nonNull).map(GroundObject::getConvexHull).forEach(hull -> combinedMatHull.add(new Area(hull)));
			for (GroundObject exerciseGroundObject : plugin.getExerciseMatsAnswerList())
			{
				if (exerciseGroundObject != null)
				{
					OverlayUtil.renderPolygon(graphics2D, combinedMatHull, Color.GREEN);
				}
			}
		}

		if (plugin.getGraveMap() != null && !plugin.getGraveMap().isEmpty())
		{
			if (checkBufferedImage == null)
			{
				this.checkBufferedImage = this.spriteManager.getSprite(SpriteID.OptionsRadioButtons.CHECK_GREEN, 0);
			}
			if (crossBufferedImage == null)
			{
				this.crossBufferedImage = this.spriteManager.getSprite(SpriteID.OptionsRadioButtons.CROSS_RED, 0);
			}
			for (Map.Entry<Grave.GraveNumber, Grave> graveEntry : plugin.getGraveMap().entrySet())
			{
				Grave.GraveNumber graveNumber = graveEntry.getKey();
				Grave grave = graveEntry.getValue();
				if (grave != null)
				{
					BufferedImage coffinImage = plugin.getCoffinItemImageMap().get(grave.getRequiredCoffin());
					Coffin requiredCoffin = grave.getRequiredCoffin();
					Coffin placedCoffin = grave.getPlacedCoffin();
					if (requiredCoffin == null || placedCoffin == null)
					{
						continue;
					}
					Color requiredCoffinColor = requiredCoffin.getColor();
					Color requiredCoffinTransparentColor = this.getTransparentColor(requiredCoffin.getColor(), 50);
					Color placedCoffinColor = placedCoffin.getColor();
					Color placedCoffinTransparentColor = this.getTransparentColor(placedCoffin.getColor(), 50);
					if (coffinImage == null)
					{
						continue;
					}
					if (grave.getGraveStone() != null)
					{
						Point location = grave.getGraveStone().getCanvasLocation();
						LocalPoint localPoint = grave.getGraveStone().getLocalLocation();
						if (location != null)
						{
							OverlayUtil.renderImageLocation(client, graphics2D, localPoint, coffinImage, 50);
						}
						// Also outline the gravestone
						Shape graveStoneHull = grave.getGraveStone().getConvexHull();
						if (graveStoneHull != null)
						{
							OverlayUtil.renderPolygon(graphics2D, graveStoneHull, requiredCoffinColor);
						}
					}
					// If the grave is empty, then highlight it according to
					if (placedCoffin == Coffin.EMPTY)
					{
						Shape emptyGraveHull = grave.getEmptyGrave().getConvexHull();
						if (emptyGraveHull != null)
						{
							OverlayUtil.renderPolygon(graphics2D, emptyGraveHull, requiredCoffinColor, requiredCoffinTransparentColor, new BasicStroke(2));
						}
					}
					else
					{
						Shape filledGraveHull = grave.getFilledGrave().getConvexHull();
						if (filledGraveHull != null)
						{
							OverlayUtil.renderPolygon(graphics2D, filledGraveHull, placedCoffinColor, placedCoffinTransparentColor, new BasicStroke(2));
							Point centeredSpritePoint = Perspective.getCanvasImageLocation(client, grave.getFilledGrave().getLocalLocation(), checkBufferedImage, 0);
							if (placedCoffin != requiredCoffin)
							{
								if (centeredSpritePoint != null)
								{
									OverlayUtil.renderImageLocation(graphics2D, centeredSpritePoint, crossBufferedImage);
								}
							}
							else
							{
								if (centeredSpritePoint != null)
								{
									OverlayUtil.renderImageLocation(graphics2D, centeredSpritePoint, checkBufferedImage);
								}
							}
						}
					}
				}
			}
		}

		return null;
	}

	private Color getTransparentColor(Color color, int alpha)
	{
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}
}
