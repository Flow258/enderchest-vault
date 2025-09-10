package com.flowey258.enderchestVault;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerVaultsIntegration {

    private final JavaPlugin plugin;
    private boolean playerVaultsEnabled = false;

    public PlayerVaultsIntegration(JavaPlugin plugin) {
        this.plugin = plugin;
        this.playerVaultsEnabled = Bukkit.getPluginManager().isPluginEnabled("PlayerVaults");

        if (playerVaultsEnabled) {
            plugin.getLogger().info("PlayerVaults integration enabled!");
        } else {
            plugin.getLogger().info("PlayerVaults not found, using built-in vault system.");
        }
    }

    /**
     * Opens a specific vault using PlayerVaults command
     */
    public boolean openPlayerVault(Player player, int vaultNumber) {
        if (!playerVaultsEnabled) {
            plugin.getLogger().warning("Attempted to use PlayerVaults integration, but PlayerVaults is not available!");
            return false;
        }

        if (vaultNumber < 1 || vaultNumber > 99) {
            plugin.getLogger().warning("Invalid vault number: " + vaultNumber + ". Must be between 1-99.");
            return false;
        }

        try {
            // Check if player has permission for the specific vault
            String permission = "playervaults.amount." + vaultNumber;
            if (!player.hasPermission(permission) && !player.hasPermission("playervaults.*")) {
                player.sendMessage("§cYou don't have permission to access vault #" + vaultNumber + "!");
                return false;
            }

            // Dispatch the PlayerVaults command as the player
            String command = "pv " + vaultNumber;
            return player.performCommand(command);

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to open PlayerVault #" + vaultNumber + " for " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Opens vault #1 (default)
     */
    public boolean openPlayerVault(Player player) {
        return openPlayerVault(player, 1);
    }

    /**
     * Checks if PlayerVaults plugin is enabled and available
     */
    public boolean isPlayerVaultsEnabled() {
        return playerVaultsEnabled;
    }

    /**
     * Refreshes the PlayerVaults availability status
     */
    public void refreshAvailability() {
        boolean wasEnabled = playerVaultsEnabled;
        playerVaultsEnabled = Bukkit.getPluginManager().isPluginEnabled("PlayerVaults");

        if (wasEnabled != playerVaultsEnabled) {
            if (playerVaultsEnabled) {
                plugin.getLogger().info("PlayerVaults has been enabled! Switching to PlayerVaults integration.");
            } else {
                plugin.getLogger().info("PlayerVaults has been disabled! Switching to built-in vault system.");
            }
        }
    }
}