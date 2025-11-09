package me.Tonus_.hatCosmetics;

import co.aikar.commands.PaperCommandManager;
import me.Tonus_.hatCosmetics.command.MainCommand;
import me.Tonus_.hatCosmetics.config.ConfigRetriever;
import me.Tonus_.hatCosmetics.message.color.ColorParser;
import me.Tonus_.hatCosmetics.message.generics.GenericsRetriever;
import me.Tonus_.hatCosmetics.message.translations.TranslationRetriever;
import me.Tonus_.hatCosmetics.networking.ModrinthUpstreamAPIClient;
import me.Tonus_.hatCosmetics.updates.PluginVersionRetriever;
import me.Tonus_.hatCosmetics.updates.SemanticVersionChecker;
import me.Tonus_.hatCosmetics.message.MessageRetriever;
import me.Tonus_.hatCosmetics.utility.string.StringFormatter;
import org.bstats.bukkit.Metrics;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;


public class Main extends JavaPlugin implements Listener {
	private Metrics metricService;

	/**
	 * Plugin enable
	 */
	@Override
	public void onEnable() {
        var configRetriever = new ConfigRetriever(this);
		var colorParser = new ColorParser();
		var genericsRetriever = new GenericsRetriever(getSLF4JLogger(), colorParser);
		var stringFormatter = new StringFormatter(getSLF4JLogger());
		var translationRetriever = new TranslationRetriever(this);
        var messageRetriever = new MessageRetriever(this, configRetriever, colorParser, genericsRetriever, translationRetriever, stringFormatter);

		// Register commands
		var commandManager = new PaperCommandManager(this);
		commandManager.enableUnstableAPI("help");
		commandManager.registerCommand(new MainCommand());

		// Enable bStats
		metricService = new Metrics(this, 11075);

		// Check for updates
		var modrinthApiClient = new ModrinthUpstreamAPIClient(getSLF4JLogger(), "4h6EFh3D");
		var pluginVersionRetriever = new PluginVersionRetriever(this);
		new SemanticVersionChecker(getSLF4JLogger(), modrinthApiClient, pluginVersionRetriever, true)
				.checkForUpdates();
	}

	/**
	 * Plugin disable
	 */
	@Override
	public void onDisable() {
		metricService.shutdown();
		super.onDisable();
	}


// TODO: Move template method somewhere else.
//	/**
//	 * Ensures that the template file exists
//	 */
//	private void ensureTemplateExists() {
//		// Check if the messages directory exists
//		File messageDir = new File(plugin.getDataFolder(), "messages");
//		if (!messageDir.exists()) {
//			plugin.saveResource("messages/", false);
//		}
//
//		// Check if the template file exists
//		File templateFile = new File(messageDir, "template.yml");
//		if (!templateFile.exists()) {
//			plugin.saveResource("messages/template.yml", false);
//		}
//	}
}
