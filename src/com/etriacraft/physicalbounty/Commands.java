package com.etriacraft.physicalbounty;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Commands {

	PhysicalBounty plugin;

	public Commands(PhysicalBounty instance) {
		this.plugin = instance;
		init();
	}

	private void init() {
		PluginCommand bounty = plugin.getCommand("bounty");
		CommandExecutor exe;

		exe = new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
				if (args.length == 0) {
					s.sendMessage("-----§cBounty Commands§f-----");
					s.sendMessage("§3/bounty new [Player] [Amount]§f - Create a new bounty.");
					s.sendMessage("§3/bounty withdraw [ID]§f - Withdraw a Bounty By ID");
					s.sendMessage("§3/bounty view <id|open|closed|withdrawn>§f - View bounty info or list your open bounties.");
					s.sendMessage("§3/bounty list <Player>§f - List all open bounties or bounties on specific player");
					return true;
				}
				
				if (args[0].equalsIgnoreCase("withdraw")) {
					if (!s.hasPermission("physicalbounty.withdraw")) {
						s.sendMessage("§cYou don't have permission to do that.");
						return true;
					}
					if (args.length != 2) {
						s.sendMessage("§3Proper Usage: §6/bounty withdraw [ID]");
						s.sendMessage("§aWithdraw one of your bounties.");
						return true;
					}
					
					if (!Methods.isInteger(args[1])) {
						s.sendMessage("§cID must be an integer.");
						return true;
					}
					
					ResultSet rs2 = Methods.getBountyByID(Integer.parseInt(args[1]));
					try {
						if (!rs2.next()) {
							s.sendMessage("§cA bounty with that ID does not exist.");
							return true;
						} else {
							String issuer = rs2.getString("issuer");
							if (!issuer.equalsIgnoreCase(s.getName())) {
								s.sendMessage("§cYou cannot withdraw a bounty you did not create.");
								return true;
							}
							Methods.withdrawBounty(Integer.parseInt(args[1]));
							s.sendMessage("§cYou have withdrawn §7Bounty #" + args[1]);
							int amount = rs2.getInt("amount");
							String item = rs2.getString("item");
							ItemStack is = new ItemStack(Material.getMaterial(item), amount);
							((Player) s).getInventory().addItem(is);
							return true;
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				
				if (args[0].equalsIgnoreCase("view")) {
					if (!s.hasPermission("physicalbounty.view")) {
						s.sendMessage("§cYou don't have permission to do that.");
						return true;
					}
					if (args.length != 2) {
						s.sendMessage("§3Proper Usage: §7/bounty view <id|open|closed|withdrawn>");
						s.sendMessage("§aView specific information about a bounty, or view your own open, closed, or withdrawn bounties.");
						return true;
					}
					
					if (!Methods.isInteger(args[1]) && !args[1].equalsIgnoreCase("open") && !args[1].equalsIgnoreCase("closed") && !args[1].equalsIgnoreCase("withdrawn")) {
						s.sendMessage("§3Proper Usage: §7/bounty view <id|open|closed|withdrawn>");
						s.sendMessage("§aView specific information about a bounty, or view your own open, closed, or withdrawn bounties.");
						return true;
					}
					
					if (Methods.isInteger(args[1])) {
						// They are looking up an ID.
						ResultSet rs2 = Methods.getBountyByID(Integer.parseInt(args[1]));
						try {
							if (!rs2.next()) {
								s.sendMessage("§cNo bounty exists by that ID.");
								return true;
							} else {
								s.sendMessage("-----§cBounty #" + args[1] + "§f-----");
								s.sendMessage("§3ID: §a" + args[1]);
								s.sendMessage("§3Player: §a" + rs2.getString("player"));
								s.sendMessage("§3Issued By: §a" + rs2.getString("issuer"));
								s.sendMessage("§3Worth: §a" + rs2.getInt("amount") + " " + rs2.getString("item"));
								s.sendMessage("§3Started On: §a" + rs2.getString("startdate"));
								if (rs2.getString("assassin") != null) {
									s.sendMessage("§3Assassin: §a" + rs2.getString("assassin"));
								}
								if (rs2.getString("enddate") != null) {
									s.sendMessage("§3Completed On: §a" + rs2.getString("enddate"));
								}
							}
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
					
					if (args[1].equalsIgnoreCase("open")) {
						s.sendMessage("-----§cYour Open Bounties§f-----");
						s.sendMessage("§3ID§f - §3Player§f - §3Reward");
						ResultSet rs2 = Methods.getOpenBountiesByPlayer(s.getName());
						try {
							if (!rs2.next()) {
								s.sendMessage("§cYou don't have any open bounties.");
								return true;
							} else {
								do {
									s.sendMessage("§3" + rs2.getInt("id") + "§f - §3" + rs2.getString("player") + "§f - §3" + rs2.getInt("amount") + " " + rs2.getString("item"));
								} while (rs2.next());
							}
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
					
					if (args[1].equalsIgnoreCase("closed")) {
						s.sendMessage("-----§cYour Closed Bounties§f-----");
						s.sendMessage("§3ID§f - §3Player§f - §3Reward");
						ResultSet rs2 = Methods.getClosedBountiesByPlayer(s.getName());
						try {
							if (!rs2.next()) {
								s.sendMessage("§cYou don't have any closed bounties.");
								return true;
							} else {
								do {
									s.sendMessage("§3" + rs2.getInt("id") + "§f - §3" + rs2.getString("player") + "§f - §3" + rs2.getInt("amount") + " " + rs2.getString("item"));
								} while (rs2.next());
							}
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
					
					if (args[1].equalsIgnoreCase("withdrawn")) {
						s.sendMessage("-----§cYour Withdrawn Bounties§f-----");
						s.sendMessage("§3ID§f - §3Player§f - §3Reward");
						ResultSet rs2 = Methods.getWithdrawnBountiesByPlayer(s.getName());
						try {
							if (!rs2.next()) {
								s.sendMessage("§cYou don't have any withdrawn bounties.");
								return true;
							} else {
								do {
									s.sendMessage("§3" + rs2.getInt("id") + "§f - §3" + rs2.getString("player") + "§f - §3" + rs2.getInt("amount") + " " + rs2.getString("item"));
								} while (rs2.next());
							}
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
				}
				if (args[0].equalsIgnoreCase("list")) {
					if (!s.hasPermission("physicalbounty.list")) {
						s.sendMessage("§cYou don't have permission to do that.");
						return true;
					}
					if (args.length > 2) {
						s.sendMessage("§3Proper Usage: §6/bounty list [Player]");
						s.sendMessage("§aView all open bounties or bounties on specific player.");
						return true;
					}

					if (args.length == 1) {
						s.sendMessage("-----§cOpen Bounties§f-----");
						ResultSet rs2 = Methods.getAllOpenBounties();
						try {
							if (!rs2.next()) {
								s.sendMessage("§cThere are no open bounties.");
								return true;
							} else {
								s.sendMessage("§3ID§f - §3Player§f - §3Reward§f");

								do {
									s.sendMessage("§3" + rs2.getInt("id") + "§f - §3" + rs2.getString("player") + "§f - §3" + rs2.getInt("amount") + " " + rs2.getString("item"));
								} while (rs2.next());
							}
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}

					if (args.length == 2) {
						String target = args[1];
						ResultSet rs2 = Methods.getBountiesOnPlayer(target);
						s.sendMessage("-----§cOpen Bounties On " + target + "§f-----");
						try {
							if (!rs2.next()) {
								s.sendMessage("§cThere are no open bounties on " + target);
								return true;
							} else {
								s.sendMessage("§3ID§f - §3Issued By§f - §3Reward§f");
								do {
									s.sendMessage("§3" + rs2.getInt("id") + "§f - §3" + rs2.getString("issuer") + "§f - §3" + rs2.getInt("amount") + " " + rs2.getString("item"));
								} while (rs2.next());
							}
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
				}

				if (args[0].equalsIgnoreCase("new")) {
					if (!s.hasPermission("physicalbounty.create")) {
						s.sendMessage("§cYou don't have permission to do that.");
						return true;
					}
					if (args.length != 3) {
						s.sendMessage("§3Proper Usage: §6/bounty new [Player] [Amount]");
						s.sendMessage("§aOpens a new bounty on [Player] using the item in your hand. [Amount] is the value of the bounty.");
						return true;
					}

					String t = args[1];
					Player target = Bukkit.getPlayer(t);
					if (target == null || !target.isOnline()) {
						s.sendMessage("§cBounties can only be issued on online players.");
						return true;
					}
					int amount = Integer.parseInt(args[2]);

					if (target.getName().equalsIgnoreCase(s.getName())) {
						s.sendMessage("§cYou can't open a bounty on yourself.");
						return true;
					}

					if (amount < 0 || amount > plugin.getConfig().getInt("MaxItems")) {
						s.sendMessage("§cAmount must be between 0 and " + plugin.getConfig().getInt("MaxItems"));
						return true;
					}

					if (!(s instanceof Player)) {
						s.sendMessage("§cThis command can only be issued by a player.");
						return true;
					}

					Player player = (Player) s;

					String itemInHand = player.getInventory().getItemInHand().getType().toString();
					List<String> applicableItems = plugin.getConfig().getStringList("ApplicableItems");
					if (itemInHand == null || !applicableItems.contains(itemInHand)) {
						s.sendMessage("§cYou cannot use the item in your hand to create a bounty.");
						s.sendMessage("§3Applicable Items: §a" + applicableItems.toString());
						return true;
					}

					if (player.getInventory().getItemInHand().getAmount() < amount) {
						s.sendMessage("§cYou do not have enough items in your hand.");
						int required = amount - player.getInventory().getItemInHand().getAmount();
						s.sendMessage("§cYou need §7" + required + "§c more §7" + itemInHand);
						return true;
					}

					if (plugin.getConfig().getInt("MaxBountiesOnPlayer") != 0) {
						if (Methods.getNumberOfBountiesOnPlayer(target.getName()) >= plugin.getConfig().getInt("MaxBountiesOnPlayer")) {
							s.sendMessage("§cThat player already has §7" + plugin.getConfig().getInt("MaxBountiesOnPlayer") + " §cbounties on them.");
							return true;
						}
					}

					if (plugin.getConfig().getInt("MaxBountiesPerPlayer") != 0) {
						if (Methods.getNumberOfOpenBountiesByPlayer(player.getName()) >= plugin.getConfig().getInt("MaxBountiesPerPlayer")) {
							s.sendMessage("§cYou already have §7" + plugin.getConfig().getInt("MaxBountiesPerPlayer"));
							return true;
						}
					}

					if (Methods.playerAlreadyHasBountyOnPlayer(player.getName(), target.getName())) {
						s.sendMessage("§cYou already have a bounty out on §7" + target.getName() + "§c.");
						return true;
					}

					player.getInventory().removeItem(new ItemStack(Material.getMaterial(itemInHand), amount));
					Methods.createBounty(target.getName(), player.getName(), itemInHand, amount);
					Bukkit.getServer().broadcastMessage("§aA bounty has been placed on §7" + target.getName() + "§a by §7" + player.getName() + "§a for §7" + amount + " " + itemInHand + "§a.");
				}
				return true;
			}
		}; bounty.setExecutor(exe);
	}

}
