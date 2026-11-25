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

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.GameObject;
import net.runelite.api.coords.Direction;
import net.runelite.api.gameval.ObjectID;
import randomeventhelper.randomevents.prisonpete.BalloonAnimal;

@Getter
@AllArgsConstructor
public enum EvilBobStatue
{
	NORTH(ObjectID.MACRO_EVIL_BOB_STATUE_3, Direction.NORTH),
	EAST(ObjectID.MACRO_EVIL_BOB_STATUE_2, Direction.EAST),
	SOUTH(ObjectID.MACRO_EVIL_BOB_STATUE_1, Direction.SOUTH),
	WEST(ObjectID.MACRO_EVIL_BOB_STATUE_4, Direction.WEST);

	private final int objectID;
	private final Direction direction;

	private static final Map<Integer, EvilBobStatue> OBJECT_ID_TO_STATUE_MAP;
	private static final Map<Direction, EvilBobStatue> DIRECTION_TO_STATUE_MAP;

	static
	{
		ImmutableMap.Builder<Integer, EvilBobStatue> objectIDToStatueMap = new ImmutableMap.Builder<>();
		ImmutableMap.Builder<Direction, EvilBobStatue> directionToStatueMap = new ImmutableMap.Builder<>();

		for (EvilBobStatue statue : values())
		{
			objectIDToStatueMap.put(statue.getObjectID(), statue);
			directionToStatueMap.put(statue.getDirection(), statue);
		}

		OBJECT_ID_TO_STATUE_MAP = objectIDToStatueMap.build();
		DIRECTION_TO_STATUE_MAP = directionToStatueMap.build();
	}

	public static EvilBobStatue fromObjectID(int objectID)
	{
		return OBJECT_ID_TO_STATUE_MAP.get(objectID);
	}

	public static EvilBobStatue fromDirection(Direction direction)
	{
		return DIRECTION_TO_STATUE_MAP.get(direction);
	}
}
