/*
 * Copyright (c) 2026, Infinity <https://github.com/Infinitay>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package randomeventhelper.randomevents.evilbob;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import randomeventhelper.RandomEventHelperConfig;

@Singleton
public class EvilBobItemOverlay extends WidgetItemOverlay
{
	private final Client client;
	private final RandomEventHelperConfig config;
	private final EvilBobHelper evilBobHelper;

	private final Color CORRECT_KEY_COLOR = Color.GREEN;
	private final Color CORRECT_KEY_TRANSPARENT_COLOR = new Color(CORRECT_KEY_COLOR.getRed(), CORRECT_KEY_COLOR.getGreen(), CORRECT_KEY_COLOR.getBlue(), 50);
	private final Color INCORRECT_KEY_COLOR = Color.RED;
	private final Color INCORRECT_KEY_TRANSPARENT_COLOR = new Color(INCORRECT_KEY_COLOR.getRed(), INCORRECT_KEY_COLOR.getGreen(), INCORRECT_KEY_COLOR.getBlue(), 50);

	@Inject
	public EvilBobItemOverlay(Client client, RandomEventHelperConfig config, EvilBobHelper evilBobHelper)
	{
		this.client = client;
		this.config = config;
		this.evilBobHelper = evilBobHelper;
		showOnInventory();
	}

	@Override
	public void renderItemOverlay(Graphics2D graphics2D, int itemID, WidgetItem widgetItem)
	{
		if (this.evilBobHelper.getEvilBobPortal() != null && this.evilBobHelper.isFishInInventory() && !this.evilBobHelper.isEventFinished())
		{
			// Handle incorrect fish state
			if (itemID == ItemID.EVIL_BOB_COOKED_FISH_INCORRECT || itemID == ItemID.EVIL_BOB_UNCOOKED_FISH_INCORRECT)
			{
				Point textPoint = new Point(widgetItem.getCanvasBounds().x - 1, (widgetItem.getCanvasBounds().y - 1) + widgetItem.getCanvasBounds().height);
				String text = "Drop";
				OverlayUtil.renderTextLocation(graphics2D, textPoint, text, Color.WHITE);
				OverlayUtil.renderPolygon(graphics2D, widgetItem.getCanvasBounds(), INCORRECT_KEY_COLOR, INCORRECT_KEY_TRANSPARENT_COLOR, new BasicStroke(1));
			}
			// Handle correct fish state
			if (itemID == ItemID.EVIL_BOB_COOKED_FISH_CORRECT || itemID == ItemID.EVIL_BOB_UNCOOKED_FISH_CORRECT)
			{
				Point textPoint = new Point(widgetItem.getCanvasBounds().x - 1, (widgetItem.getCanvasBounds().y - 1) + widgetItem.getCanvasBounds().height);
				String text = (itemID == ItemID.EVIL_BOB_COOKED_FISH_CORRECT) ? "Uncook" : "Turn in";
				OverlayUtil.renderTextLocation(graphics2D, textPoint, text, Color.WHITE);
				OverlayUtil.renderPolygon(graphics2D, widgetItem.getCanvasBounds(), this.config.borderColor(), this.config.fillColor(), new BasicStroke(0));
			}
		}
	}
}
