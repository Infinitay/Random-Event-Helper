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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Point;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import randomeventhelper.RandomEventHelperConfig;
import randomeventhelper.RandomEventHelperOverlay;

@Singleton
public class EvilBobOverlay extends Overlay
{
	private final Client client;
	private final RandomEventHelperConfig config;
	private final EvilBobHelper plugin;

	@Inject
	public EvilBobOverlay(Client client, RandomEventHelperConfig config, EvilBobHelper plugin)
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
		if (this.plugin.getEvilBobPortal() == null)
		{
			return null;
		}
		else if (this.plugin.isEventFinished())
		{
			RandomEventHelperOverlay.renderOverlayWithFill(graphics2D, this.plugin.getEvilBobPortal().getConvexHull(), this.config.borderColor(), this.config.fillColor());
			return null;
		}

		// Note, since you can log out with a fish in your inventory, ignore NPC renders if there is a fish in the inventory
		// Keep in mind that if this is the case, then the state all would have been reset

		// State: Just loaded into the event -> Talk to Servant to determine statue
		if (this.plugin.getServantNPC() != null && this.plugin.getCorrectEvilBobStatue() == null && !this.plugin.isFishInInventory())
		{
			String overheadTextOverlay = "Talk to determine the correct statue and fishing spots";
			int mimeHeight = this.plugin.getServantNPC().getLogicalHeight();
			int mimeTextOffset = this.plugin.getServantNPC().getAnimationHeightOffset();
			Point textPoint = this.plugin.getServantNPC().getCanvasTextLocation(graphics2D, overheadTextOverlay, mimeHeight + mimeTextOffset);
			OverlayUtil.renderTextLocation(graphics2D, textPoint, overheadTextOverlay, Color.WHITE);
			RandomEventHelperOverlay.renderOverlayWithFill(graphics2D, this.plugin.getServantNPC().getConvexHull(), this.config.borderColor(), this.config.fillColor());
		}

		// State: Correct statue has been determined -> Highlight the correct statue and fishing spots
		if (this.plugin.getCorrectEvilBobStatue() != null && !this.plugin.isCorrectFishInInventory())
		{
			RandomEventHelperOverlay.renderOverlayWithFill(graphics2D, this.plugin.getCorrectEvilBobStatue().getConvexHull(), this.config.borderColor(), this.config.fillColor());
			if (!this.plugin.getCorrectFishingSpotsSet().isEmpty())
			{
				for (GameObject correctFishingSpot : this.plugin.getCorrectFishingSpotsSet())
				{
					RandomEventHelperOverlay.renderOverlayWithFill(graphics2D, correctFishingSpot.getConvexHull(), this.config.borderColor(), this.config.fillColor());
				}
			}

			// Also, highlight the various fishing nets if the player doesn't already have one
			if (!this.plugin.isFishingNetInInventory())
			{
				for (Tile fishingNetTile : this.plugin.getFishingNetTileSet())
				{
					RandomEventHelperOverlay.renderOverlayWithFill(graphics2D, fishingNetTile.getItemLayer().getCanvasTilePoly(), this.config.borderColor(), this.config.fillColor());
				}
			}
		}

		// State: Player has the correct fish in their inventory
		if (this.plugin.isCorrectFishInInventory())
		{
			//  Cooked fish in inv, we need to uncook the fish -> Highlight the pots
			if (this.plugin.getFishState().equals(FishState.COOKED))
			{
				for (GameObject uncookingPot : this.plugin.getUncookingPotSet())
				{
					RandomEventHelperOverlay.renderOverlayWithFill(graphics2D, uncookingPot.getConvexHull(), this.config.borderColor(), this.config.fillColor());
				}
			}
			else
			{
				// Uncooked fish in inv, we need to turn in the uncooked fish -> Highlight Evil Bob
				if (this.plugin.getEvilBobNPC() != null)
				{
					String overheadTextOverlay = "Talk to turn in the uncooked fish";
					int mimeHeight = this.plugin.getEvilBobNPC().getLogicalHeight();
					int mimeTextOffset = this.plugin.getEvilBobNPC().getAnimationHeightOffset();
					Point textPoint = this.plugin.getEvilBobNPC().getCanvasTextLocation(graphics2D, overheadTextOverlay, mimeHeight + mimeTextOffset);
					OverlayUtil.renderTextLocation(graphics2D, textPoint, overheadTextOverlay, Color.WHITE);
					RandomEventHelperOverlay.renderOverlayWithFill(graphics2D, this.plugin.getEvilBobNPC().getConvexHull(), this.config.borderColor(), this.config.fillColor());
				}
			}
		}

		return null;
	}
}
