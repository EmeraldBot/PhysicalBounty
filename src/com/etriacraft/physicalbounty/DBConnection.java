package com.etriacraft.physicalbounty;

import com.etriacraft.physicalbounty.sql.Database;
import com.etriacraft.physicalbounty.sql.SQLite;

public class DBConnection {

	public static Database sql;
	
	public static void init() {
		sql = new SQLite(PhysicalBounty.log, "[PhysicalBounty] Establishing SQLite Connection.", "bounties.db", PhysicalBounty.getInstance().getDataFolder().getAbsolutePath());
		((SQLite) sql).open();
		
		if (!sql.tableExists("bounties")) {
			PhysicalBounty.log.info("Creating bounties table.");
			String query = "CREATE TABLE `bounties` ("
					+ "`id` INTEGER PRIMARY KEY,"
					+ "`player` TEXT(32),"
					+ "`issuer` TEXT(32),"
					+ "`item` TEXT(32),"
					+ "`amount` INTEGER(32), "
					+ "`startdate` TEXT(255), "
					+ "`enddate` TEXT(255), "
					+ "`assassin` TEXT(32), "
					+ "`status` TEXT(32));";
			sql.modifyQuery(query);
		}
		if (!sql.tableExists("redeem")) {
			PhysicalBounty.log.info("Creating redeem table.");
			String query = "CREATE TABLE `redeem` ("
					+ "`id` INTEGER PRIMARY KEY,"
					+ "`player` TEXT(32),"
					+ "`redeemed` TEXT(32),"
					+ "`item` TEXT(32),"
					+ "`amount` INTEGER(32));";
			sql.modifyQuery(query);
		}
	}
}
