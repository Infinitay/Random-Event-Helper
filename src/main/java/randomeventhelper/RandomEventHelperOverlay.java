package randomeventhelper;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import net.runelite.client.ui.overlay.OverlayUtil;

public class RandomEventHelperOverlay
{
	public static void renderOverlayWithFill(Graphics2D graphics, Shape poly, Color borderColor, Color fillColor)
	{
		if (poly == null)
		{
			return;
		}
		OverlayUtil.renderPolygon(graphics, poly, borderColor, fillColor, new BasicStroke(2));
	}
}
