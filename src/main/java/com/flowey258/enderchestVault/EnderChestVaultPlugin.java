package com.flowey258.enderchestVault;

import org.bukkit.plugin.java.JavaPlugin;

public class EnderChestVaultPlugin extends JavaPlugin {

    private VaultManager vaultManager;
    private PlayerVaultsIntegration playerVaultsIntegration;
    private boolean usePlayerVaults;

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();

        // Load configuration
        usePlayerVaults = getConfig().getBoolean("plugin.use-playervaults", true);

        // Initialize PlayerVaults integration if enabled and available
        playerVaultsIntegration = new PlayerVaultsIntegration(this);

        if (usePlayerVaults && playerVaultsIntegration.isPlayerVaultsEnabled()) {
            getLogger().info("PlayerVaults detected! Using PlayerVaults integration.");
            vaultManager = null; // Don't need our own vault manager
        } else {
            getLogger().info("Using built-in vault system.");
            vaultManager = new VaultManager(this);
        }

        // Register event listeners
        getServer().getPluginManager().registerEvents(
                new EnderChestListener(this, vaultManager, playerVaultsIntegration), this);

        // Register command executor
        EnderChestCommand commandExecutor = new EnderChestCommand(this, vaultManager, playerVaultsIntegration);
        getCommand("enderchest").setExecutor(commandExecutor);

        getLogger().info("EnderChestVault has been enabled!");
        getLogger().info("Ender Chests will now open personal vaults instead of the default GUI.");
    }

    @Override
    public void onDisable() {
        // Save all player data before disabling (only if using built-in system)
        if (vaultManager != null) {
            vaultManager.saveAllPlayerData();
        }

        getLogger().info("EnderChestVault has been disabled!");
    }

    public VaultManager getVaultManager() {
        return vaultManager;
    }

    public PlayerVaultsIntegration getPlayerVaultsIntegration() {
        return playerVaultsIntegration;
    }

    public boolean isUsingPlayerVaults() {
        return usePlayerVaults && playerVaultsIntegration != null && playerVaultsIntegration.isPlayerVaultsEnabled();
    }
}