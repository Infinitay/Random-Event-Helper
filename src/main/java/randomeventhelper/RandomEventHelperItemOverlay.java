package randomeventhelper;

import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

public class RandomEventHelperItemOverlay extends WidgetItemOverlay
{
	private final Client client;
	private final RandomEventHelperPlugin plugin;
	private final ModelOutlineRenderer modelOutlineRenderer;

	@Inject
	public RandomEventHelperItemOverlay(Client client, RandomEventHelperPlugin plugin, ModelOutlineRenderer modelOutlineRenderer)
	{
		this.client = client;
		this.plugin = plugin;
		this.modelOutlineRenderer = modelOutlineRenderer;
		showOnInventory();
	}

	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
	{
	}
}
