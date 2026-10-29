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
package randomeventhelper.pluginmodulesystem;

import javax.inject.Inject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
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
	private ClientThread clientThread;
	@Inject
	protected GameEventManager gameEventManager;
	protected final OverlayManager overlayManager;
	protected final RandomEventHelperConfig config;
	protected final Client client;

	// Cold-start logins
	protected boolean isInInitialLoginState;
	// Post hopping or relogging
	protected boolean isInReloggingState;

	@Getter
	private boolean isBlockingEvents;

	public abstract void onStartUp();

	public abstract void onShutdown();

	public void onPlayerValidated()
	{
		// Keep it non-abstract so modules can use it as-need-be by overriding it
	}

	public void onPlayerInvalidated()
	{
		// Keep it non-abstract so modules can use it as-need-be by overriding it
		// For state-resetting purposes, you could rely on #onPlayerValidated instead
		// Don't worry about overlays because the game freezes anyway and with that freeze, the overlay is also frozen too
		// So even if you reset the state within this method, the overlay is still frozen and drawn
	}

	public abstract boolean isEnabled();

	public void startUp()
	{
		this.eventBus.register(this);
		this.onStartUp();
		if (!this.isLoggedIn())
		{
			// Module started while the player is not logged in
			// Sanity check for if the player is actually in the game but perhaps lost connection or started in the middle of a hop
			if (this.client.getGameState().equals(GameState.HOPPING) || this.client.getGameState().equals(GameState.CONNECTION_LOST))
			{
				this.isInInitialLoginState = false;
				this.isInReloggingState = true;
			}
			else
			{
				this.isInInitialLoginState = true;
				this.isInReloggingState = false;
			}

			// To help ignore processing x-spawned events twice on cold starts. Note that we're simulating the game events for the module after the player is logged in and initialized anyway.
			this.isBlockingEvents = true;
		}
		else if (this.isLoggedIn() && this.isLocalPlayerValid())
		{
			// Module started while the player is already logged in and initialized
			this.isInInitialLoginState = false;
			this.isInReloggingState = false;
			invokeAfterLoggedInAndValidated(() -> {
				// Run #onPlayerValidated first in case the state is being reset and we're tracking entities
				this.onPlayerValidated();
				log.debug("Player is already logged in and validated, attempting to simulating game events for the {} module", this.getClass().getSimpleName());
				// Make sure to unblock isBlockingEvents flag prior to re-posting the events in case there are blocking checks in the event handlers
				this.isBlockingEvents = false;
				this.simulateGameEventsForModule();
			});
		}
		log.debug("Started the {} module", this.getClass().getSimpleName());
	}

	public void shutdown()
	{
		this.eventBus.unregister(this);
		this.onShutdown();
		this.isInInitialLoginState = false;
		this.isInReloggingState = false;
		this.isBlockingEvents = false;
		log.debug("Shutdown the {} module", this.getClass().getSimpleName());
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState().equals(GameState.HOPPING) || event.getGameState().equals(GameState.LOGIN_SCREEN))
		{
			// At this game state, the player is technically invalidated or will be once they log back in (or hop to another world)
			this.onPlayerInvalidated();
			this.isInReloggingState = true;
			// Block the game events between HOPPING/LOGIN_SCREEN and the LOGGED_IN state to make it easier to process events
			// This is useful for dealing with x-spawned events given the context of hopping/logging in
			this.isBlockingEvents = true;
		}
		// Only simulate game events for an initial login or post-hop "login"
		if ((this.isInInitialLoginState || this.isInReloggingState) && event.getGameState().equals(GameState.LOGGED_IN))
		{
			invokeAfterLoggedInAndValidated(() -> {
				// Run #onPlayerValidated first in case the state is being reset and we're tracking entities
				this.onPlayerValidated();
				String loginType = this.isInInitialLoginState ? "Initial login" : "Re-log/Hop";
				log.debug("{} detected, attempting to simulate game events for the {} module", loginType, this.getClass().getSimpleName());
				// Make sure to unblock isBlockingEvents flag prior to re-posting the events in case there are blocking checks in the event handlers
				this.isBlockingEvents = false;
				this.simulateGameEventsForModule();
				// Reset the initial login and relogging flag so we don't simulate game events again until the player logs out and back in again
				this.isInInitialLoginState = false;
				this.isInReloggingState = false;
			});
		}
	}

	public void invokeAfterLoggedInAndValidated(Runnable runnableTask)
	{
		this.clientThread.invokeLater(() -> {
			if (this.isLoggedIn() && this.isLocalPlayerValid())
			{
				runnableTask.run();
				return true;
			}
			else
			{
				return false;
			}
		});
	}

	private void simulateGameEventsForModule()
	{
		log.debug("Simulating game events for the {} module", this.getClass().getSimpleName());
		// Remember to pass in the instance (this) and not the class (#getClass)
		// Re-posts ItemContainerChanged, NpcSpawned, PlayerSpawned, WallObjectSpawned, DecorativeObjectSpawned, GroundObjectSpawned, GameObjectSpawned, ItemSpawned, WorldEntitySpawned
		this.gameEventManager.simulateGameEvents(this);
	}

	public boolean isLoggedIn()
	{
		return this.client.getGameState().equals(GameState.LOGGED_IN);
	}

	public boolean isLocalPlayerValid()
	{
		return this.client.getLocalPlayer() != null;
	}
}
