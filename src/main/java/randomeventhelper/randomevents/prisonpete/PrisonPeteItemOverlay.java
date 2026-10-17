package randomeventhelper.randomevents.prisonpete;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import randomeventhelper.RandomEventHelperConfig;

@Slf4j
@Singleton
public class PrisonPeteItemOverlay extends WidgetItemOverlay
{
	private final Client client;
	private final RandomEventHelperConfig config;
	private final PrisonPeteHelper prisonPeteHelper;

	private final Color CORRECT_KEY_COLOR = Color.GREEN;
	private final Color CORRECT_KEY_TRANSPARENT_COLOR = new Color(CORRECT_KEY_COLOR.getRed(), CORRECT_KEY_COLOR.getGreen(), CORRECT_KEY_COLOR.getBlue(), 50);
	private final Color INCORRECT_KEY_COLOR = Color.RED;
	private final Color INCORRECT_KEY_TRANSPARENT_COLOR = new Color(INCORRECT_KEY_COLOR.getRed(), INCORRECT_KEY_COLOR.getGreen(), INCORRECT_KEY_COLOR.getBlue(), 50);

	@Inject
	public PrisonPeteItemOverlay(Client client, RandomEventHelperConfig config, PrisonPeteHelper prisonPeteHelper)
	{
		this.client = client;
		this.config = config;
		this.prisonPeteHelper = prisonPeteHelper;
		showOnInventory();
	}

	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
	{
		if (this.prisonPeteHelper.isKeyInInventory() && itemId == ItemID.PRISONPETE_KEY)
		{
			Point textPoint = new Point(widgetItem.getCanvasBounds().x - 1, (widgetItem.getCanvasBounds().y - 1) + widgetItem.getCanvasBounds().height);
			String text = this.prisonPeteHelper.isLastPoppedBalloonAnimalCorrect() ? "Turn in" : "Drop";
			OverlayUtil.renderTextLocation(graphics, textPoint, text, Color.WHITE);
			if (this.prisonPeteHelper.isLastPoppedBalloonAnimalCorrect())
			{
				OverlayUtil.renderPolygon(graphics, widgetItem.getCanvasBounds(), this.config.borderColor(), this.config.fillColor(), new BasicStroke(2));
			}
			else
			{
				OverlayUtil.renderPolygon(graphics, widgetItem.getCanvasBounds(), INCORRECT_KEY_COLOR, INCORRECT_KEY_TRANSPARENT_COLOR, new BasicStroke(2));
			}
		}
	}
}
