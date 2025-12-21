package randomeventhelper.pluginmodulesystem;

import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.GameEventManager;
import randomeventhelper.RandomEventHelperConfig;

@Slf4j
// Thanks to Llemon for this - Since we are now relying on constructor injection, we will need a constructor followed by injecting it
// To keep it a little cleaner, you can still use lombok RAC and also pass in @Inject into it so that the constructor will be injected properly
// Also, keep in mind that we don't need to define final variables within the module classes themselves since they will be passed in via the constructor injection super call
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public abstract class PluginModule
{
	// It's fine to field inject these since we only need access to them here and not in the module classes themselves
	@Inject
	protected EventBus eventBus;
	@Inject
	protected GameEventManager gameEventManager;
	protected final OverlayManager overlayManager;
	protected final RandomEventHelperConfig config;
	protected final Client client;

	public abstract void onStartUp();

	public abstract void onShutdown();

	public abstract boolean isEnabled();

	public void startUp()
	{
		this.eventBus.register(this);
		this.onStartUp();
		if (client.getGameState().getState() >= GameState.LOGGED_IN.getState())
		{
			// Remember to pass in the instance (this) and not the class (#getClass)
			// Re-posts NpcSpawned, PlayerSpawned, WallObjectSpawned, DecorativeObjectSpawned, GroundObjectSpawned, GameObjectSpawned, ItemSpawned, WorldEntitySpawned
			this.gameEventManager.simulateGameEvents(this);
		}
		log.debug("Started the {} module", this.getClass().getSimpleName());
	}

	public void shutdown()
	{
		this.eventBus.unregister(this);
		this.onShutdown();
		log.debug("Shutdown the {} module", this.getClass().getSimpleName());
	}
}
