package randomeventhelper.randomevents.gravedigger;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.WidgetItemOverlay;

@Slf4j
@Singleton
public class GravediggerItemOverlay extends WidgetItemOverlay
{
	private final Client client;
	private final GravediggerHelper plugin;

	@Inject
	public GravediggerItemOverlay(Client client, GravediggerHelper plugin)
	{
		this.client = client;
		this.plugin = plugin;
		showOnInventory();
	}

	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
	{
		if (plugin.getCoffinsInInventory() != null && !plugin.getCoffinsInInventory().isEmpty() && plugin.getCoffinsInInventory().contains(itemId))
		{
			Coffin coffin = Coffin.getCoffinFromItemID(itemId);
			Color requiredCoffinColor = coffin != null ? coffin.getColor() : Color.BLACK;
			Color requiredCoffinTransparentColor = coffin != null ? this.getTransparentColor(coffin.getColor(), 50) : Color.BLACK;
			OverlayUtil.renderPolygon(graphics, widgetItem.getCanvasBounds(), requiredCoffinColor, requiredCoffinTransparentColor, new BasicStroke(2));
		}
	}

	private Color getTransparentColor(Color color, int alpha)
	{
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}
}
