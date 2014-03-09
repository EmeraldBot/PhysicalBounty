package com.etriacraft.physicalbounty;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Methods {

	PhysicalBounty plugin;

	public Methods(PhysicalBounty plugin) {
		this.plugin = plugin;
	}

	public static Set<String> playersWithBounties = new HashSet<String>();

	public static void loadPlayersWithBounties() {
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM bounties WHERE status = 'open'");
		try {
			while (rs2.next()) {
				playersWithBounties.add(rs2.getString("player"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static ResultSet getAllOpenBounties() {
		return DBConnection.sql.readQuery("SELECT * FROM bounties WHERE status = 'open'");
	}

	public static boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	public static ResultSet getBountiesOnPlayer(String player) {
		return DBConnection.sql.readQuery("SELECT * FROM bounties WHERE player = '" + player + "' AND status = 'open'");
	}
	public static void createBounty(String player, String issuer, String item, int amount) {
		DBConnection.sql.modifyQuery("INSERT INTO bounties (player, issuer, item, amount, startdate, enddate, assassin, status) VALUES ("
				+ "'" + player + "', "
				+ "'" + issuer + "', "
				+ "'" + item + "', "
				+ amount + ", "
				+ "'" + getCurrentDate() + "', "
				+ null + ", "
				+ null + ", "
				+ "'open')");
		if (!doesPlayerHaveBounty(player)) {
			playersWithBounties.add(player);
		}
	}



	public static void closeBounty(int id, String assassin, String target) {
		DBConnection.sql.modifyQuery("UPDATE bounties SET assassin = '" + assassin + "' WHERE id = " + id);
		DBConnection.sql.modifyQuery("UPDATE bounties SET enddate = '" + getCurrentDate() + "' WHERE id = " + id);
		DBConnection.sql.modifyQuery("UPDATE bounties SET status = 'closed' WHERE id = " + id);
	}
	public static boolean playerAlreadyHasBountyOnPlayer(String player, String target) {
		if (!playersWithBounties.contains(target)) {
			return false;
		}

		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM bounties WHERE issuer = '" + player + "' AND player = '" + target + "' AND status = 'open'");
		try {
			if (rs2.next()) return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static int getNumberOfOpenBountiesByPlayer(String player) {
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM bounties WHERE issuer = '" + player + "' AND status = 'open'");
		try {
			int i = 0;
			if (!rs2.next()) {
				return 0;
			}
			do {
				i++;
			} while (rs2.next());
			return i;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static void checkForExpiredBounties() {
		System.out.println("Checking for expired bounties.");
		if (PhysicalBounty.instance.getConfig().getInt("ExpirationTime") != 0) {
			ResultSet rs2 = getAllOpenBounties();
			try {
				do {
					Date startDate = stringToDate(rs2.getString("startdate"));
					Date expirationDate = getExpirationDate(startDate);
					Date currentDate = stringToDate(getCurrentDate());
					
					long timeUntilUnban = (expirationDate.getTime() - currentDate.getTime());
					if (timeUntilUnban <= 0) {
						withdrawBounty(rs2.getInt("id"));
						DBConnection.sql.modifyQuery("INSERT INTO redeem (player, redeemed, item, amount) VALUES ('" + rs2.getString("issuer") + "', 'false', '" + rs2.getString("item") + "', " + rs2.getInt("item" + ")"));
						for (Player player: Bukkit.getOnlinePlayers()) {
							if (rs2.getString("issuer").equalsIgnoreCase(player.getName())) {
								redeemForPlayer(player);
							}
						}
					}
				} while (rs2.next());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public static void redeemForPlayer(Player p) {
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM redeem WHERE player = '" + p.getName() + "' AND redeemed = 'false' LIMIT 1");
		try {
			if (rs2.next()) {
				String item = rs2.getString("item");
				int amount = rs2.getInt("amount");
				ItemStack is = new ItemStack(Material.getMaterial(item), amount);
				p.getInventory().addItem(is);
				p.sendMessage("§aOne of your bounties worth §7" + amount + " " + item + "§a has expired.");
				DBConnection.sql.modifyQuery("UPDATE redeem SET redeemed = 'true' WHERE id = " + rs2.getInt("id"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static Date getExpirationDate(Date d) {
		Calendar cal = Calendar.getInstance();
		int length = PhysicalBounty.instance.getConfig().getInt("ExpirationTime");
		cal.add(Calendar.SECOND, length);
		return cal.getTime();
	}

	public static Date stringToDate(String s) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		String date2 = dateFormat.format(date);
		try {
			return dateFormat.parse(date2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ResultSet getOpenBountiesByPlayer(String player) {
		return DBConnection.sql.readQuery("SELECT * FROM bounties WHERE issuer = '" + player + "' AND status = 'open'");
	}

	public static ResultSet getClosedBountiesByPlayer(String player) {
		return DBConnection.sql.readQuery("SELECT * FROM bounties WHERE issuer = '" + player + "' AND status = 'closed'");
	}

	public static void withdrawBounty(int id) {
		DBConnection.sql.modifyQuery("UPDATE bounties SET status = 'withdrawn' WHERE id = " + id);
	}
	public static ResultSet getWithdrawnBountiesByPlayer(String player) {
		return DBConnection.sql.readQuery("SELECT * FROM bounties WHERE issuer = '" + player + "' AND status = 'withdrawn'");
	}
	public static int getNumberOfBountiesOnPlayer(String player) {
		if (!playersWithBounties.contains(player)) return 0;
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM bounties WHERE player = '" + player + "' AND status = 'open'");
		try {
			int i = 0;
			while (rs2.next()) {
				i++;
			}
			return i;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static String getCurrentDate() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

		Date date = new Date();
		return dateFormat.format(date);
	}

	public static boolean doesPlayerHaveBounty(String player) {
		if (playersWithBounties.contains(player)) {
			return true;
		}
		return false;
	}

	public static ResultSet getBountyByID(int id) {
		return DBConnection.sql.readQuery("SELECT * FROM bounties WHERE id = " + id);
	}

}
