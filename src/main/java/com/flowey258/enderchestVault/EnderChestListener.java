package com.flowey258.enderchestVault;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EnderChestListener implements Listener {

    private final EnderChestVaultPlugin plugin;
    private final VaultManager vaultManager;
    private final PlayerVaultsIntegration playerVaultsIntegration;

    public EnderChestListener(EnderChestVaultPlugin plugin, VaultManager vaultManager, PlayerVaultsIntegration playerVaultsIntegration) {
        this.plugin = plugin;
        this.vaultManager = vaultManager;
        this.playerVaultsIntegration = playerVaultsIntegration;
    }

    /**
     * Handles right-clicking on Ender Chests
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnderChestInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || clickedBlock.getType() != Material.ENDER_CHEST) {
            return;
        }

        Player player = event.getPlayer();

        // Check permission
        if (!player.hasPermission("enderchest.vault")) {
            player.sendMessage(plugin.getConfig().getString("messages.no-vault-permission",
                    "§cYou don't have permission to use personal vaults!"));
            event.setCancelled(true);
            return;
        }

        // Cancel the default ender chest opening
        event.setCancelled(true);

        // Open personal vault using appropriate method
        openPersonalVault(player);
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

    /**
     * Saves vault data when player closes the inventory (built-in system only)
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player) || vaultManager == null) {
            return;
        }

        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();

        // Check if this is a personal vault (built-in system)
        if (title.startsWith("§8Personal Vault - ")) {
            vaultManager.savePlayerVault(player);
        }
    }

    /**
     * Saves and unloads vault when player leaves (built-in system only)
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (vaultManager != null) {
            Player player = event.getPlayer();
            vaultManager.unloadPlayerVault(player);
        }
    }
}