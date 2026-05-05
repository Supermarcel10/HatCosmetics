package me.Tonus_.hatCosmetics;

import co.aikar.commands.PaperCommandManager;
import me.Tonus_.hatCosmetics.command.MainCommand;
import me.Tonus_.hatCosmetics.config.ConfigRetriever;
import me.Tonus_.hatCosmetics.config.mapper.TypeMapperRegistry;
import me.Tonus_.hatCosmetics.cosmetic.CosmeticItemFactory;
import me.Tonus_.hatCosmetics.cosmetic.CosmeticTagManager;
import me.Tonus_.hatCosmetics.inventory.InventoryManager;
import me.Tonus_.hatCosmetics.message.color.ColorParser;
import me.Tonus_.hatCosmetics.message.translations.TranslationRetriever;
import me.Tonus_.hatCosmetics.networking.ModrinthUpstreamAPIClient;
import me.Tonus_.hatCosmetics.player.PlayerEventManager;
import me.Tonus_.hatCosmetics.updates.PluginVersionRetriever;
import me.Tonus_.hatCosmetics.updates.SemanticVersionChecker;
import me.Tonus_.hatCosmetics.message.MessageRetriever;
import me.Tonus_.hatCosmetics.utility.editor.NBTEditor;
import me.Tonus_.hatCosmetics.utility.string.StringFormatter;
import me.Tonus_.hatCosmetics.versionedAPICalls.CustomModelData;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;


public class Main extends JavaPlugin {
	private Metrics metricService;

	/**
	 * Plugin enable
	 */
	@Override
	public void onEnable() {
        var typeMapperRegistry = new TypeMapperRegistry();
        var configRetriever = new ConfigRetriever(this, typeMapperRegistry);
        var nbtEditor = new NBTEditor(this);
		var colorParser = new ColorParser();
		var stringFormatter = new StringFormatter(getSLF4JLogger());
		var translationRetriever = new TranslationRetriever(this);
        var messageRetriever = new MessageRetriever(this, configRetriever, colorParser, translationRetriever, stringFormatter);
        var cosmeticTagManager = new CosmeticTagManager(nbtEditor);
        var customModelData = new CustomModelData(getLogger());
        var cosmeticItemFactory = new CosmeticItemFactory(customModelData, cosmeticTagManager, messageRetriever);
        var inventoryManager = new InventoryManager(nbtEditor, configRetriever, messageRetriever, cosmeticItemFactory);

		// Register commands
        var commandListener = new MainCommand(inventoryManager);

		var commandManager = new PaperCommandManager(this);
		commandManager.enableUnstableAPI("help");
		commandManager.registerCommand(commandListener);

        // Register listener
        getServer().getPluginManager().registerEvents(
                new PlayerEventManager(inventoryManager, configRetriever, cosmeticTagManager),
                this
        );

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
