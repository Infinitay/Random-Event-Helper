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

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

@Getter
public enum EvilBobFishingSpot
{
	NORTH(new WorldPoint(2526, 4791, 0), new WorldPoint(2527, 4791, 0), new WorldPoint(2529, 4791, 0), new WorldPoint(2530, 4791, 0)),
	EAST(new WorldPoint(2543, 4779, 0), new WorldPoint(2543, 4778, 0), new WorldPoint(2543, 4777, 0), new WorldPoint(2543, 4776, 0)),
	SOUTH(new WorldPoint(2524, 4764, 0), new WorldPoint(2525, 4764, 0), new WorldPoint(2528, 4764, 0), new WorldPoint(2529, 4764, 0)),
	WEST(new WorldPoint(2510, 4778, 0), new WorldPoint(2510, 4777, 0), new WorldPoint(2510, 4775, 0), new WorldPoint(2510, 4774, 0));

	private final Set<WorldPoint> fishingSpotWorldPointSet;

	EvilBobFishingSpot(WorldPoint... fishingSpots)
	{
		this.fishingSpotWorldPointSet = ImmutableSet.copyOf(fishingSpots);
	}

	// It'd be more efficient to use this when we already know the statue
	public boolean containsFishingSpot(WorldPoint worldPoint)
	{
		return this.fishingSpotWorldPointSet.contains(worldPoint);
	}

	public static EvilBobFishingSpot getFishingSpotFromStatue(EvilBobStatue statue)
	{
		switch (statue)
		{
			case NORTH:
				return NORTH;
			case EAST:
				return EAST;
			case SOUTH:
				return SOUTH;
			case WEST:
				return WEST;
			default:
				return null;
		}
	}

	public static Set<WorldPoint> getFishingSpotWorldPointSetFromStatue(EvilBobStatue statue)
	{
		switch (statue)
		{
			case NORTH:
				return NORTH.getFishingSpotWorldPointSet();
			case EAST:
				return EAST.getFishingSpotWorldPointSet();
			case SOUTH:
				return SOUTH.getFishingSpotWorldPointSet();
			case WEST:
				return WEST.getFishingSpotWorldPointSet();
			default:
				return null;
		}
	}

	public static boolean isFishingSpotByWorldPoint(WorldPoint worldPoint)
	{
		for (EvilBobFishingSpot fishingSpot : values())
		{
			if (fishingSpot.getFishingSpotWorldPointSet().contains(worldPoint))
			{
				return true;
			}
		}
		return false;
	}
}
