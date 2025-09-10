package com.flowey258.enderchestVault;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EnderChestCommand implements CommandExecutor {

    private final EnderChestVaultPlugin plugin;
    private final VaultManager vaultManager;
    private final PlayerVaultsIntegration playerVaultsIntegration;

    public EnderChestCommand(EnderChestVaultPlugin plugin, VaultManager vaultManager, PlayerVaultsIntegration playerVaultsIntegration) {
        this.plugin = plugin;
        this.vaultManager = vaultManager;
        this.playerVaultsIntegration = playerVaultsIntegration;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfig().getString("messages.player-only",
                    "§cThis command can only be used by players!"));
            return true;
        }

        Player player = (Player) sender;

        // Check permission
        if (!player.hasPermission("enderchest.use")) {
            player.sendMessage(plugin.getConfig().getString("messages.no-permission",
                    "§cYou don't have permission to use this command!"));
            return true;
        }

        // Check vault permission
        if (!player.hasPermission("enderchest.vault")) {
            player.sendMessage(plugin.getConfig().getString("messages.no-vault-permission",
                    "§cYou don't have permission to use personal vaults!"));
            return true;
        }

        // Open the personal vault using appropriate method
        openPersonalVault(player);
        return true;
    }

    /**
     * Opens personal vault using either PlayerVaults integration or built-in system
     */
    private void openPersonalVault(Player player) {
        if (plugin.isUsingPlayerVaults()) {
            // Use PlayerVaults integration
            int vaultNumber = plugin.getConfig().getInt("plugin.playervaults-number", 1);
            if (playerVaultsIntegration.openPlayerVault(player, vaultNumber)) {
                String message = plugin.getConfig().getString("messages.vault-opened",
                        "§6Opening your personal vault...");
                player.sendMessage(message);
            } else {
                player.sendMessage("§cFailed to open personal vault!");
            }
        } else {
            // Use built-in vault system
            if (vaultManager != null) {
                vaultManager.openVault(player);
            } else {
                player.sendMessage("§cVault system not available!");
            }
        }
    }
}