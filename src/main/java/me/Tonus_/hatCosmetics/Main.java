package me.Tonus_.hatCosmetics;

import java.util.stream.Collectors;
import co.aikar.commands.PaperCommandManager;
import me.Tonus_.hatCosmetics.command.MainCommand;
import me.Tonus_.hatCosmetics.config.ConfigRetriever;
import me.Tonus_.hatCosmetics.config.mapper.TypeMapperRegistry;
import me.Tonus_.hatCosmetics.cosmetic.Cosmetic;
import me.Tonus_.hatCosmetics.cosmetic.CosmeticEquipManager;
import me.Tonus_.hatCosmetics.cosmetic.CosmeticItemFactory;
import me.Tonus_.hatCosmetics.cosmetic.CosmeticTagManager;
import me.Tonus_.hatCosmetics.cosmetic.permission.CosmeticPermissionChecker;
import me.Tonus_.hatCosmetics.inventory.InventoryManager;
import me.Tonus_.hatCosmetics.storage.CosmeticStorageFactory;
import me.Tonus_.hatCosmetics.storage.YmlCosmeticStorage;
import me.Tonus_.hatCosmetics.message.translations.TranslationRetriever;
import me.Tonus_.hatCosmetics.networking.ModrinthUpstreamAPIClient;
import me.Tonus_.hatCosmetics.player.PlayerEventManager;
import me.Tonus_.hatCosmetics.updates.PluginVersionRetriever;
import me.Tonus_.hatCosmetics.reload.PluginReloader;
import me.Tonus_.hatCosmetics.updates.SemanticVersionChecker;
import me.Tonus_.hatCosmetics.message.MessageRetriever;
import me.Tonus_.hatCosmetics.utility.editor.NBTEditor;
import me.Tonus_.hatCosmetics.utility.jar.JarAccessor;
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
        var stringFormatter = new StringFormatter(getSLF4JLogger());
        var jarAccessor = new JarAccessor(this.getClass());
        var translationRetriever = new TranslationRetriever(this, jarAccessor);
        var messageRetriever = new MessageRetriever(this, configRetriever, translationRetriever, stringFormatter);
        var cosmeticTagManager = new CosmeticTagManager(nbtEditor);
        var customModelData = new CustomModelData(getLogger());
        var ymlStorage = new YmlCosmeticStorage(this, getSLF4JLogger(), jarAccessor);
        var storageFactory = new CosmeticStorageFactory(configRetriever, ymlStorage);
        var cosmeticStorage = storageFactory.createFromConfig();
        var permissionChecker = new CosmeticPermissionChecker();
        var cosmeticItemFactory = new CosmeticItemFactory(customModelData, cosmeticTagManager, messageRetriever, permissionChecker);
        var equipManager = new CosmeticEquipManager(cosmeticItemFactory, cosmeticTagManager, cosmeticStorage, messageRetriever, configRetriever, permissionChecker);
        var inventoryManager = new InventoryManager(nbtEditor, configRetriever, messageRetriever, cosmeticItemFactory, cosmeticTagManager, equipManager, cosmeticStorage, permissionChecker);

        Runnable versionCheck = () -> {
            var modrinthApiClient = new ModrinthUpstreamAPIClient(getSLF4JLogger(), "4h6EFh3D");
            var pluginVersionRetriever = new PluginVersionRetriever(this);
            new SemanticVersionChecker(getSLF4JLogger(), modrinthApiClient, pluginVersionRetriever, true)
                    .checkForUpdates();
        };

        var pluginReloader = new PluginReloader(
            inventoryManager,
            configRetriever,
            translationRetriever,
            cosmeticStorage,
            versionCheck,
            messageRetriever
        );

        // Register commands
        var commandListener = new MainCommand(inventoryManager, equipManager, pluginReloader, messageRetriever);

		var commandManager = new PaperCommandManager(this);
		commandManager.enableUnstableAPI("help");
		commandManager.registerCommand(commandListener);

		// Register command completions
        commandManager.getCommandCompletions().registerCompletion("hats", c -> {
            var player = c.getPlayer();
            var cosmetics = cosmeticStorage.loadAll();

            if (permissionChecker.hasWildcard(player)) {
                return cosmetics.stream().map(Cosmetic::name).collect(Collectors.toList());
            }

            return cosmetics.stream()
                .filter(cosmetic -> permissionChecker.canUseCosmetic(player, cosmetic))
                .map(Cosmetic::name)
                .collect(Collectors.toList());
        });

        // Register listener
        getServer().getPluginManager().registerEvents(
                new PlayerEventManager(inventoryManager, configRetriever, cosmeticTagManager, equipManager, cosmeticStorage),
                this
        );

		// Enable bStats
		metricService = new Metrics(this, 11075);

		// Check for updates
		versionCheck.run();
	}

	/**
	 * Plugin disable
	 */
	@Override
	public void onDisable() {
		metricService.shutdown();
		super.onDisable();
	}
}
