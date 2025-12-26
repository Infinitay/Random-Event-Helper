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
import net.runelite.api.NPC;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;
import randomeventhelper.RandomEventHelperConfig;
import randomeventhelper.RandomEventHelperPlugin;
import randomeventhelper.pluginmodulesystem.PluginModule;

@Slf4j
@Singleton
public class DrillDemonHelper extends PluginModule
{
	@Inject
	private ClientThread clientThread;

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

	@Getter
	private NPC drillDemonNPC;

	@Getter
	private DrillExercise requestedExercise;

	@Inject
	public DrillDemonHelper(OverlayManager overlayManager, RandomEventHelperConfig config, Client client)
	{
		super(overlayManager, config, client);
	}

	@Override
	public void onStartUp()
	{
		this.overlayManager.add(drillDemonOverlay);
		this.exerciseMatsAnswerList = Lists.newArrayListWithExpectedSize(4);
		this.exerciseMatsMultimap = HashMultimap.create(4, 2);
		this.exerciseVarbitMatMultimap = HashMultimap.create(4, 2);
		this.drillDemonNPC = null;
		this.requestedExercise = null;

		if (this.isLoggedIn()) {
			this.clientThread.invoke(() ->
			{
				log.debug("Initializing varbits for Drill Demon exercise mappings in case plugin was enabled mid-event.");
				for (int postVarbitID = VarbitID.MACRO_DRILLDEMON_POST_1; postVarbitID <= VarbitID.MACRO_DRILLDEMON_POST_4; postVarbitID++)
				{
					int postVarbitValue = client.getVarbitValue(postVarbitID);
					VarbitChanged varbitChangedEvent = new VarbitChanged();
					varbitChangedEvent.setVarbitId(postVarbitID);
					varbitChangedEvent.setValue(postVarbitValue);
					this.onVarbitChanged(varbitChangedEvent);
				}
			});
		}
	}

	@Override
	public void onShutdown()
	{
		this.overlayManager.remove(drillDemonOverlay);
		this.exerciseMatsAnswerList = null;
		this.exerciseMatsMultimap = null;
		this.exerciseVarbitMatMultimap = null;
		this.drillDemonNPC = null;
		this.requestedExercise = null;
	}

	@Override
	public boolean isEnabled()
	{
		return this.config.isDrillDemonEnabled();
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		// We want to prio storing the Drill Demon NPC if it's interacting with the player because that one is the correct one in case of edge cases
		boolean isNPCInteractingWithPlayer = npcSpawned.getNpc().getInteracting() != null && npcSpawned.getNpc().getInteracting().equals(client.getLocalPlayer());
		if (npcSpawned.getNpc().getId() == NpcID.MACRO_DRILLDEMON && this.isInDrillDemonLocalInstance() && (this.drillDemonNPC == null || isNPCInteractingWithPlayer))
		{
			this.drillDemonNPC = npcSpawned.getNpc();
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		if (npcDespawned.getNpc().getId() == NpcID.MACRO_DRILLDEMON && !this.isInDrillDemonLocalInstance())
		{
			log.debug("Drill Demon NPC despawned, resetting exercise mats and mappings.");
			this.exerciseMatsAnswerList.clear();
			this.exerciseMatsMultimap.clear();
			this.exerciseVarbitMatMultimap.clear();
			this.drillDemonNPC = null;
			this.requestedExercise = null;
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged varbitChanged)
	{
		// For some reason, when the player is within the maze random event, the varbits for the drill demon event fire/are modified
		if (this.isInMazeLocalInstance())
		{
			return;
		}

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
				this.requestedExercise = null;
				DrillExercise exercise = DrillExercise.getExerciseFromText(sanitizedChatMessage);
				if (exercise != null)
				{
					log.debug("Drill Demon requested exercise: {}", exercise.name());
					this.requestedExercise = exercise;
					this.exerciseMatsAnswerList = Lists.newArrayList(this.exerciseVarbitMatMultimap.get(exercise.getVarbitValue()));
					log.debug("Drill Demon exercise mats list set to: {}", this.exerciseMatsAnswerList);
				}
				else
				{
					if (sanitizedChatMessage.endsWith("Private! Follow my orders and you may, just may, leave here in a fit state for my corps!"))
					{
						return;
					}
					log.warn("Drill Demon requested unknown exercise: {}", sanitizedChatMessage);
					this.exerciseMatsAnswerList.clear();
					this.requestedExercise = null;
				}
			}
		}
	}

	@Subscribe
	public void onGroundObjectSpawned(GroundObjectSpawned groundObjectSpawned)
	{
		if (this.isInDrillDemonLocalInstance())
		{
			int addedPost = -1;
			switch (groundObjectSpawned.getGroundObject().getId())
			{
				case ObjectID.BARRACK_MAT_1:
					exerciseMatsMultimap.put(1, groundObjectSpawned.getGroundObject());
					log.debug("Added exercise mat with ID {} to post 1", groundObjectSpawned.getGroundObject().getId());
					addedPost = 1;
					break;
				case ObjectID.BARRACK_MAT_2:
					exerciseMatsMultimap.put(2, groundObjectSpawned.getGroundObject());
					log.debug("Added exercise mat with ID {} to post 2", groundObjectSpawned.getGroundObject().getId());
					addedPost = 2;
					break;
				case ObjectID.BARRACK_MAT_3:
					exerciseMatsMultimap.put(3, groundObjectSpawned.getGroundObject());
					log.debug("Added exercise mat with ID {} to post 3", groundObjectSpawned.getGroundObject().getId());
					addedPost = 3;
					break;
				case ObjectID.BARRACK_MAT_4:
					exerciseMatsMultimap.put(4, groundObjectSpawned.getGroundObject());
					log.debug("Added exercise mat with ID {} to post 4", groundObjectSpawned.getGroundObject().getId());
					addedPost = 4;
					break;
				default:
					break;
			}

			if (addedPost != -1)
			{
				// Update the mappings in case the mats spawned after the varbits were set
				int postVarbit = client.getVarbitValue(VarbitID.MACRO_DRILLDEMON_POST_1 + (addedPost - 1));
				this.updateExerciseMappings(postVarbit, addedPost);
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

	private boolean isInMazeLocalInstance()
	{
		return RandomEventHelperPlugin.getRegionIDFromCurrentLocalPointInstanced(client) == 11591;
	}
}
