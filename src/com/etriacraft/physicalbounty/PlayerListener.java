package com.etriacraft.physicalbounty;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {

	public static PhysicalBounty plugin;

	public PlayerListener(PhysicalBounty instance) {
		plugin = instance;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		if (plugin.getConfig().getInt("NotifyOnJoinDistance") > 0) {
			int distance = plugin.getConfig().getInt("NotifyOnJoinDistance");
			Location lloc = e.getPlayer().getLocation();
			if (Methods.doesPlayerHaveBounty(e.getPlayer().getName())) {
				for (Player player: Bukkit.getOnlinePlayers()) {
					if (player.getLocation().distance(lloc) <= distance) {
						player.sendMessage("§aA player with one or more bounties on their head has signed on within §7" + distance + "§a blocks of you.");
					}
				}
			}
			
		}
		Methods.redeemForPlayer(e.getPlayer());
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		Entity en = e.getEntity();
		if (en instanceof Player) {
			Player p = (Player) en;
			Entity en2 = p.getKiller();
			if (en2 instanceof Player) {
				Player killer = (Player) en2;
				// Killed by a player
				if (Methods.doesPlayerHaveBounty(p.getName())) { // Player has bounties on head.
					if (plugin.getConfig().getStringList("ApplicableWorlds").contains(killer.getWorld().getName())) {
						ResultSet rs2 = Methods.getBountiesOnPlayer(p.getName());
						try {
							while (rs2.next()) {
								String issuer = rs2.getString("issuer");
								String item = rs2.getString("item");
								int amount = rs2.getInt("amount");
								int id = rs2.getInt("id");
								ItemStack is = new ItemStack(Material.getMaterial(item), amount);
								plugin.getServer().broadcastMessage("§7" + killer.getName() + " §ahas collected bounties on §7" + p.getName());
								killer.sendMessage("§aReceived bounty reward of §7" + amount + " " + item + " §afrom §7" + issuer + "§a.");
								killer.getInventory().addItem(is);
								Methods.closeBounty(id, killer.getName(), p.getName());
							}
						} catch (SQLException ex) {
							ex.printStackTrace();
						}
						Methods.playersWithBounties.remove(p.getName()); // Assuming all bounties are collected, we want to remove them from the list of players with bounties.
					}
				}
			}
		}
	}
}