package randomeventhelper.randomevents.drilldemon;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GroundObject;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;
import randomeventhelper.RandomEventHelperPlugin;

@Slf4j
@Singleton
public class DrillDemonHelper
{
	@Inject
	private EventBus eventBus;

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private DrillDemonOverlay drillDemonOverlay;

	@Getter
	private List<GroundObject> exerciseMatsAnswerList;

	// <Post Number, Mat>
	private Multimap<Integer, GroundObject> exerciseMatsMultimap;

	// <Exercise Varbit, Mat>
	private Multimap<Integer, GroundObject> exerciseVarbitMatMultimap;

	public void startUp()
	{
		this.eventBus.register(this);
		this.overlayManager.add(drillDemonOverlay);
		this.exerciseMatsAnswerList = Lists.newArrayListWithExpectedSize(4);
		this.exerciseMatsMultimap = HashMultimap.create(4, 2);
		this.exerciseVarbitMatMultimap = HashMultimap.create(4, 2);
	}

	public void shutDown()
	{
		this.eventBus.unregister(this);
		this.overlayManager.remove(drillDemonOverlay);
		this.exerciseMatsAnswerList = null;
		this.exerciseMatsMultimap = null;
		this.exerciseVarbitMatMultimap = null;
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		if (npcDespawned.getNpc().getId() == NpcID.MACRO_DRILLDEMON)
		{
			log.debug("Drill Demon NPC despawned, resetting exercise mats and mappings.");
			this.exerciseMatsAnswerList.clear();
			this.exerciseMatsMultimap.clear();
			this.exerciseVarbitMatMultimap.clear();
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged varbitChanged)
	{
		switch (varbitChanged.getVarbitId())
		{
			case VarbitID.MACRO_DRILLDEMON_POST_1:
				this.updateExerciseMappings(varbitChanged.getValue(), 1);
				break;
			case VarbitID.MACRO_DRILLDEMON_POST_2:
				this.updateExerciseMappings(varbitChanged.getValue(), 2);
				break;
			case VarbitID.MACRO_DRILLDEMON_POST_3:
				this.updateExerciseMappings(varbitChanged.getValue(), 3);
				break;
			case VarbitID.MACRO_DRILLDEMON_POST_4:
				this.updateExerciseMappings(varbitChanged.getValue(), 4);
				break;
			default:
				break;
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		String sanitizedChatMessage = Text.sanitizeMultilineText(chatMessage.getMessage());
		if (chatMessage.getType() == ChatMessageType.DIALOG)
		{
			if (this.isInDrillDemonLocalInstance())
			{
				this.exerciseMatsAnswerList.clear();
				DrillExercise exercise = DrillExercise.getExerciseFromText(sanitizedChatMessage);
				if (exercise != null)
				{
					log.debug("Drill Demon requested exercise: {}", exercise.name());
					this.exerciseMatsAnswerList = Lists.newArrayList(this.exerciseVarbitMatMultimap.get(exercise.getVarbitValue()));
					log.debug("Drill Demon exercise mats list set to: {}", this.exerciseMatsAnswerList);
				}
				else
				{
					log.warn("Drill Demon requested unknown exercise: {}", sanitizedChatMessage);
					this.exerciseMatsAnswerList.clear();
				}
			}
		}
	}

	@Subscribe
	public void onGroundObjectSpawned(GroundObjectSpawned groundObjectSpawned)
	{
		if (this.isInDrillDemonLocalInstance())
		{
			switch (groundObjectSpawned.getGroundObject().getId())
			{
				case ObjectID.BARRACK_MAT_1:
					exerciseMatsMultimap.put(1, groundObjectSpawned.getGroundObject());
					log.debug("Added exercise mat with ID {} to post 1", groundObjectSpawned.getGroundObject().getId());
					break;
				case ObjectID.BARRACK_MAT_2:
					exerciseMatsMultimap.put(2, groundObjectSpawned.getGroundObject());
					log.debug("Added exercise mat with ID {} to post 2", groundObjectSpawned.getGroundObject().getId());
					break;
				case ObjectID.BARRACK_MAT_3:
					exerciseMatsMultimap.put(3, groundObjectSpawned.getGroundObject());
					log.debug("Added exercise mat with ID {} to post 3", groundObjectSpawned.getGroundObject().getId());
					break;
				case ObjectID.BARRACK_MAT_4:
					exerciseMatsMultimap.put(4, groundObjectSpawned.getGroundObject());
					log.debug("Added exercise mat with ID {} to post 4", groundObjectSpawned.getGroundObject().getId());
					break;
				default:
					break;
			}
		}
	}

	private void updateExerciseMappings(int exerciseVarbitValue, int postNumber)
	{
		DrillExercise exercise = DrillExercise.VARBIT_TO_EXERCISE_MAP.get(exerciseVarbitValue);
		if (exercise != null)
		{
			log.debug("Drill Demon exercise of Post_{} changed to: {} ({})", postNumber, exercise.getVarbitValue(), exercise.name());
			this.exerciseVarbitMatMultimap.replaceValues(exerciseVarbitValue, this.exerciseMatsMultimap.get(postNumber));
		}
		else
		{
			log.warn("Drill Demon exercise varbit changed to unknown value: {}", exerciseVarbitValue);
			this.exerciseVarbitMatMultimap.replaceValues(exerciseVarbitValue, ImmutableSet.of());
		}
	}

	private boolean isInDrillDemonLocalInstance()
	{
		return RandomEventHelperPlugin.getRegionIDFromCurrentLocalPointInstanced(client) == 12619;
	}
}
