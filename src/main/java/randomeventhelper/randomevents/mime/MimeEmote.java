package randomeventhelper.randomevents.mime;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.gameval.AnimationID;
import net.runelite.api.gameval.InterfaceID;

@Getter
@AllArgsConstructor
public enum MimeEmote
{
	THINK(AnimationID.EMOTE_THINK, InterfaceID.MacroMimeEmotes.BUTTON_0),
	LAUGH(AnimationID.EMOTE_LAUGH, InterfaceID.MacroMimeEmotes.BUTTON_1),
	CLIMB_ROPE(AnimationID.EMOTE_CLIMBING_ROPE, InterfaceID.MacroMimeEmotes.BUTTON_2),
	GLASS_BOX(AnimationID.EMOTE_GLASS_BOX, InterfaceID.MacroMimeEmotes.BUTTON_3),
	CRY(AnimationID.EMOTE_CRY, InterfaceID.MacroMimeEmotes.BUTTON_4),
	DANCE(AnimationID.EMOTE_DANCE, InterfaceID.MacroMimeEmotes.BUTTON_5),
	LEAN(AnimationID.EMOTE_MIME_LEAN, InterfaceID.MacroMimeEmotes.BUTTON_6),
	GLASS_WALL(AnimationID.EMOTE_GLASS_WALL, InterfaceID.MacroMimeEmotes.BUTTON_7);

	private final int animationID;
	private final int buttonWidgetID;

	private static final Map<Integer, MimeEmote> ANIMATION_TO_MIME_EMOTE_MAP;

	static
	{
		ANIMATION_TO_MIME_EMOTE_MAP = Maps.uniqueIndex(ImmutableSet.copyOf(values()), MimeEmote::getAnimationID);
	}

	public static MimeEmote getMimeEmoteFromAnimationID(int animationID)
	{
		return ANIMATION_TO_MIME_EMOTE_MAP.get(animationID);
	}
}
