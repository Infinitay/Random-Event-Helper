package randomeventsolver.randomevents.drilldemon;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DrillExercise
{
	JOG(1, "Get yourself over there and jog on that mat, private!"),
	SIT_UP(2, "Get on that mat and give me sit ups, private!"),
	PUSH_UP(3, "Drop and give me push ups on that mat, private!"),
	STAR_JUMP(4, "I want to see you on that mat doing star jumps, private!");

	private final int varbitValue;
	private final String drillSergeantText;

	public static final Map<Integer, DrillExercise> VARBIT_TO_EXERCISE_MAP = Maps.uniqueIndex(ImmutableList.copyOf(values()), DrillExercise::getVarbitValue);

	public static DrillExercise getExerciseFromText(String text)
	{
		for (DrillExercise exercise : values())
		{
			if (text.toLowerCase().endsWith(exercise.getDrillSergeantText().toLowerCase()))
			{
				return exercise;
			}
		}
		return null;
	}
}
