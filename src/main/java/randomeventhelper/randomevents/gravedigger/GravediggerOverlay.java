package randomeventhelper.randomevents.gravedigger;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.gameval.SpriteID;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import randomeventhelper.RandomEventHelperConfig;

@Slf4j
@Singleton
public class GravediggerOverlay extends Overlay
{
	private final Client client;
	private final RandomEventHelperConfig config;
	private final GravediggerHelper gravediggerHelper;
	private final SpriteManager spriteManager;
	private BufferedImage checkBufferedImage;
	private BufferedImage crossBufferedImage;

	@Inject
	public GravediggerOverlay(Client client, RandomEventHelperConfig config, GravediggerHelper gravediggerHelper, SpriteManager spriteManager)
	{
		this.client = client;
		this.config = config;
		this.spriteManager = spriteManager;
		this.gravediggerHelper = gravediggerHelper;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics2D)
	{
		if (this.gravediggerHelper.getGraveMap() != null && !this.gravediggerHelper.getGraveMap().isEmpty())
		{
			if (checkBufferedImage == null)
			{
				this.checkBufferedImage = this.spriteManager.getSprite(SpriteID.OptionsRadioButtons.CHECK_GREEN, 0);
			}
			if (crossBufferedImage == null)
			{
				this.crossBufferedImage = this.spriteManager.getSprite(SpriteID.OptionsRadioButtons.CROSS_RED, 0);
			}
			for (Map.Entry<GraveNumber, Grave> graveEntry : this.gravediggerHelper.getGraveMap().entrySet())
			{
				GraveNumber graveNumber = graveEntry.getKey();
				Grave grave = graveEntry.getValue();
				if (grave != null)
				{
					BufferedImage coffinImage = this.config.gravediggerUseSkillIcons() ? this.gravediggerHelper.getCoffinSkillImageMap().get(grave.getRequiredCoffin()) : this.gravediggerHelper.getCoffinItemImageMap().get(grave.getRequiredCoffin());
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

					// Renders the check or cross above the grave depending on if the correct coffin is placed
					GameObject graveObject = grave.getFilledGrave() != null ? grave.getFilledGrave() : grave.getEmptyGrave();
					if (graveObject == null)
					{
						continue;
					}

					Point centeredSpritePoint = Perspective.getCanvasImageLocation(client, graveObject.getLocalLocation(), checkBufferedImage, 0);
					if (centeredSpritePoint != null)
					{
						if (placedCoffin != requiredCoffin)
						{
							OverlayUtil.renderImageLocation(graphics2D, centeredSpritePoint, crossBufferedImage);
						}
						else
						{
							OverlayUtil.renderImageLocation(graphics2D, centeredSpritePoint, checkBufferedImage);
						}
					}

					if (coffinImage != null && config.gravediggerHighlightMode().contains(GravediggerHighlightMode.GRAVESTONE_ICON))
					{
						renderGravestoneIcon(graphics2D, grave, coffinImage, requiredCoffinColor);
					}

					if (coffinImage != null && config.gravediggerHighlightMode().contains(GravediggerHighlightMode.HIGHLIGHT_GRAVE))
					{
						// Highlight the gravestone/headstone
						renderHighlightGravestone(graphics2D, grave, coffinImage, requiredCoffinColor);

						// Highlight the grave itself based on whether it is filled or empty
						if (placedCoffin == Coffin.EMPTY)
						{
							renderHighlightEmptyGrave(graphics2D, grave, requiredCoffinColor, requiredCoffinTransparentColor, 2);
						}
						else
						{
							renderHighlightFilledGrave(graphics2D, grave, placedCoffinColor, placedCoffinTransparentColor, placedCoffin, requiredCoffin, 2);
						}
					}

					if (config.gravediggerHighlightMode().contains(GravediggerHighlightMode.TEXT_GRAVE))
					{
						renderTextGrave(graphics2D, grave, requiredCoffin);
					}
				}
			}
		}
		return null;
	}

	private void renderGravestoneIcon(Graphics2D graphics2D, Grave grave, BufferedImage coffinImage, Color requiredCoffinColor)
	{
		if (grave.getGraveStone() != null)
		{
			Point location = grave.getGraveStone().getCanvasLocation();
			LocalPoint localPoint = grave.getGraveStone().getLocalLocation();
			if (location != null)
			{
				OverlayUtil.renderImageLocation(client, graphics2D, localPoint, coffinImage, 50);
			}
		}
	}

	private void renderHighlightGravestone(Graphics2D graphics2D, Grave grave, BufferedImage coffinImage, Color requiredCoffinColor)
	{
		if (grave.getGraveStone() != null)
		{
			Shape graveStoneHull = grave.getGraveStone().getConvexHull();
			if (graveStoneHull != null)
			{
				OverlayUtil.renderPolygon(graphics2D, graveStoneHull, requiredCoffinColor);
			}
		}
	}

	private void renderHighlightEmptyGrave(Graphics2D graphics2D, Grave grave, Color requiredCoffinColor, Color requiredCoffinTransparentColor, int strokeWidth)
	{
		if (grave.getEmptyGrave() != null)
		{
			Shape emptyGraveHull = grave.getEmptyGrave().getConvexHull();
			if (emptyGraveHull != null)
			{
				OverlayUtil.renderPolygon(graphics2D, emptyGraveHull, requiredCoffinColor, requiredCoffinTransparentColor, new BasicStroke(strokeWidth));
			}
		}
	}

	private void renderHighlightFilledGrave(Graphics2D graphics2D, Grave grave, Color placedCoffinColor, Color placedCoffinTransparentColor, Coffin placedCoffin, Coffin requiredCoffin, int strokeWidth)
	{
		if (grave.getFilledGrave() != null)
		{
			Shape filledGraveHull = grave.getFilledGrave().getConvexHull();
			if (filledGraveHull != null)
			{
				OverlayUtil.renderPolygon(graphics2D, filledGraveHull, placedCoffinColor, placedCoffinTransparentColor, new BasicStroke(strokeWidth));
			}
		}
	}

	private void renderTextGrave(Graphics2D graphics2D, Grave grave, Coffin requiredCoffin)
	{
		GameObject graveObject = grave.getFilledGrave() != null ? grave.getFilledGrave() : grave.getEmptyGrave();
		if (graveObject != null)
		{
			String graveText = this.config.gravediggerUseSkillIcons() ? requiredCoffin.getAssociatedSkillName() : requiredCoffin.getAssociatedItemName();
			Point textPoint = graveObject.getCanvasTextLocation(graphics2D, graveText, 100);
			OverlayUtil.renderTextLocation(graphics2D, textPoint, graveText, Color.WHITE);
		}
	}

	private Color getTransparentColor(Color color, int alpha)
	{
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}
}
