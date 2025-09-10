package com.flowey258.enderchestVault;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class VaultManager {

    private final JavaPlugin plugin;
    private final Map<UUID, Inventory> playerVaults;
    private final File vaultDataFolder;

    public VaultManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.playerVaults = new HashMap<>();
        this.vaultDataFolder = new File(plugin.getDataFolder(), "vaults");

        // Create vaults folder if it doesn't exist
        if (!vaultDataFolder.exists()) {
            vaultDataFolder.mkdirs();
        }
    }

    /**
     * Opens the personal vault for a player (equivalent to /pv 1)
     */
    public void openVault(Player player) {
        UUID playerUUID = player.getUniqueId();

        // Check if vault is already loaded
        Inventory vault = playerVaults.get(playerUUID);
        if (vault == null) {
            vault = loadPlayerVault(player);
            playerVaults.put(playerUUID, vault);
        }

        player.openInventory(vault);
        player.sendMessage("§6Opening your personal vault...");
    }

    /**
     * Creates or loads a player's vault inventory
     */
    private Inventory loadPlayerVault(Player player) {
        UUID playerUUID = player.getUniqueId();
        String title = "§8Personal Vault - " + player.getName();
        Inventory vault = Bukkit.createInventory(null, 54, title); // Double chest size

        File playerFile = new File(vaultDataFolder, playerUUID.toString() + ".yml");

        if (playerFile.exists()) {
            try {
                FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);

                // Load items from config
                for (int i = 0; i < vault.getSize(); i++) {
                    if (config.contains("items." + i)) {
                        ItemStack item = config.getItemStack("items." + i);
                        if (item != null && item.getType() != Material.AIR) {
                            vault.setItem(i, item);
                        }
                    }
                }

                plugin.getLogger().info("Loaded vault data for player: " + player.getName());
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to load vault for player: " + player.getName(), e);
            }
        }

        return vault;
    }

    /**
     * Saves a player's vault data to file
     */
    public void savePlayerVault(Player player) {
        UUID playerUUID = player.getUniqueId();
        Inventory vault = playerVaults.get(playerUUID);

        if (vault == null) {
            return; // No vault to save
        }

        File playerFile = new File(vaultDataFolder, playerUUID.toString() + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);

        try {
            // Clear existing items
            config.set("items", null);

            // Save current items
            for (int i = 0; i < vault.getSize(); i++) {
                ItemStack item = vault.getItem(i);
                if (item != null && item.getType() != Material.AIR) {
                    config.set("items." + i, item);
                }
            }

            config.set("player-name", player.getName());
            config.set("last-saved", System.currentTimeMillis());

            config.save(playerFile);
            plugin.getLogger().info("Saved vault data for player: " + player.getName());
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save vault for player: " + player.getName(), e);
        }
    }

    /**
     * Saves all loaded player vaults
     */
    public void saveAllPlayerData() {
        for (UUID playerUUID : playerVaults.keySet()) {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null) {
                savePlayerVault(player);
            }
        }
        plugin.getLogger().info("Saved all vault data.");
    }

    /**
     * Removes a player's vault from memory (call when player leaves)
     */
    public void unloadPlayerVault(Player player) {
        UUID playerUUID = player.getUniqueId();
        savePlayerVault(player);
        playerVaults.remove(playerUUID);
    }

    /**
     * Checks if a player has permission to use vaults
     */
    public boolean hasVaultPermission(Player player) {
        return player.hasPermission("enderchest.vault");
    }

    /**
     * Gets a player's vault inventory (for updating purposes)
     */
    public Inventory getPlayerVault(UUID playerUUID) {
        return playerVaults.get(playerUUID);
    }
}