package randomeventhelper.randomevents.gravedigger;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.util.ImageUtil;
import randomeventhelper.RandomEventHelperConfig;

@Slf4j
@Singleton
public class GravediggerItemOverlay extends WidgetItemOverlay
{
	private final Client client;
	private final RandomEventHelperConfig config;
	private final GravediggerHelper gravediggerHelper;

	private final double SCALE_FACTOR = 0.8;

	@Inject
	public GravediggerItemOverlay(Client client, RandomEventHelperConfig config, GravediggerHelper gravediggerHelper)
	{
		this.client = client;
		this.config = config;
		this.gravediggerHelper = gravediggerHelper;
		showOnInventory();
	}

	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
	{
		if (gravediggerHelper.getCoffinsInInventory() != null && !gravediggerHelper.getCoffinsInInventory().isEmpty() && gravediggerHelper.getCoffinsInInventory().contains(itemId))
		{
			Coffin coffin = Coffin.getCoffinFromItemID(itemId);
			if (config.gravediggerHighlightMode().contains(GravediggerHighlightMode.HIGHLIGHT_COFFIN))
			{
				renderHighlightCoffinInInventory(graphics, widgetItem, coffin);
			}

			if (config.gravediggerHighlightMode().contains(GravediggerHighlightMode.COFFIN_ICON))
			{
				renderCoffinIconInInventory(graphics, widgetItem, coffin);
			}

			if (config.gravediggerHighlightMode().contains(GravediggerHighlightMode.TEXT_COFFIN))
			{
				renderCoffinTextInInventory(graphics, widgetItem, coffin);
			}
		}
	}

	private void renderCoffinTextInInventory(Graphics2D graphics, WidgetItem widgetItem, Coffin coffin)
	{
		Point textPoint = new Point(widgetItem.getCanvasBounds().x - 1, (widgetItem.getCanvasBounds().y - 1) + widgetItem.getCanvasBounds().height);
		String coffinText = this.config.gravediggerUseSkillIcons() ? coffin.getAssociatedSkillName() : coffin.getAssociatedItemName();
		OverlayUtil.renderTextLocation(graphics, textPoint, coffinText, Color.WHITE);
	}

	private void renderCoffinIconInInventory(Graphics2D graphics, WidgetItem widgetItem, Coffin coffin)
	{
		BufferedImage coffinImage = this.config.gravediggerUseSkillIcons() ? this.gravediggerHelper.getCoffinSkillImageMap().get(coffin) : this.gravediggerHelper.getCoffinItemImageMap().get(coffin);
		if (coffinImage == null)
		{
			return;
		}

		BufferedImage rescaledCoffinImage = ImageUtil.resizeImage(coffinImage, (int) (coffinImage.getWidth() * this.SCALE_FACTOR), (int) (coffinImage.getHeight() * this.SCALE_FACTOR));
		Point point = widgetItem.getCanvasLocation();
		// Move the point to be the bottom right of the item
		int dx = (int) (point.getX() + widgetItem.getCanvasBounds().getWidth() - rescaledCoffinImage.getWidth());
		int dy = (int) (point.getY() + 1);
		// Move the point to be at the bottom right, but making sure the x and y are capped within the widget bounds using Math.min
		// dx = Math.min(dx, (int) (widgetItem.getCanvasBounds().getX() + widgetItem.getCanvasBounds().getWidth() - rescaledCoffinImage.getWidth()));
		// dy = Math.min(dy, (int) (widgetItem.getCanvasBounds().getY() + widgetItem.getCanvasBounds().getHeight() - rescaledCoffinImage.getHeight()));
		point = new Point(dx, dy);
		OverlayUtil.renderImageLocation(graphics, point, rescaledCoffinImage);
	}

	private void renderHighlightCoffinInInventory(Graphics2D graphics, WidgetItem widgetItem, Coffin coffin)
	{
		Color requiredCoffinColor = coffin != null ? coffin.getColor() : Color.BLACK;
		Color requiredCoffinTransparentColor = coffin != null ? this.getTransparentColor(coffin.getColor(), 50) : Color.BLACK;
		OverlayUtil.renderPolygon(graphics, widgetItem.getCanvasBounds(), requiredCoffinColor, requiredCoffinTransparentColor, new BasicStroke(2));
	}

	private Color getTransparentColor(Color color, int alpha)
	{
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}
}
