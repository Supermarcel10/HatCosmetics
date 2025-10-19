package me.Tonus_.hatCosmetics;

import lombok.Getter;
import me.Tonus_.hatCosmetics.command.CommandHandler;
import me.Tonus_.hatCosmetics.handler.*;
import me.Tonus_.hatCosmetics.inventory.InventoryEventHandler;
import me.Tonus_.hatCosmetics.inventory.InventoryHandler;
import me.Tonus_.hatCosmetics.utility.Configs;
import me.Tonus_.hatCosmetics.utility.VersionChecker;
import me.Tonus_.hatCosmetics.utility.Messages;
import me.Tonus_.hatCosmetics.networking.ModrinthUpstreamAPIClient;
import me.Tonus_.hatCosmetics.updates.PluginVersionRetriever;
import me.Tonus_.hatCosmetics.updates.SemanticVersionChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

import java.util.*;


public class HatCosmetics extends JavaPlugin implements Listener {
	@Getter private static JavaPlugin instance;
	@Getter private static Logger log; // TODO: Look to change into logHandler due to clash of getLogger()

	@Getter private static CommandHandler commandHandler;

	/**
	 * Plugin enable
	 */
	@Override
	public void onEnable() {
		instance = this;
		log = getSLF4JLogger();

		Configs.init();
		Messages.init();
		commandHandler = new CommandHandler();
		InventoryHandler.init();
		ResourcePackHandler.init();

		Objects.requireNonNull(getCommand("hatcosmetics")).setExecutor(commandHandler);
		Objects.requireNonNull(getCommand("hatcosmetics")).setTabCompleter(commandHandler);
		getServer().getPluginManager().registerEvents(new InventoryEventHandler(), this);
		getServer().getPluginManager().registerEvents(new PlayerJoinHandler(), this);

		// Enable bStats
		new Metrics(this, 11075);

		// Check for updates
		var modrinthApiClient = new ModrinthUpstreamAPIClient(this.getSLF4JLogger(), "4h6EFh3D");
		var pluginVersionRetriever = new PluginVersionRetriever(this);
		new SemanticVersionChecker(this.getSLF4JLogger(), modrinthApiClient, pluginVersionRetriever, true)
				.checkForUpdates();
	}

	/**
	 * Plugin disable
	 */
	@Override
	public void onDisable() {
		super.onDisable();
	}
}
