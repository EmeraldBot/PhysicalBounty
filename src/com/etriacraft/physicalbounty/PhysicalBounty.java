package com.etriacraft.physicalbounty;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

public class PhysicalBounty extends JavaPlugin {
	
	protected static Logger log;
	
	public static PhysicalBounty instance;
	
	@Override
	public void onEnable() {
		
		instance = this;
		PhysicalBounty.log = this.getLogger();
		
		configCheck();
		new Commands(this);
		new Methods(this);
		getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
		DBConnection.init();
		Methods.loadPlayersWithBounties();
		
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				Methods.checkForExpiredBounties();
			}
		}, 0, 6000);
	}
	
	@Override
	public void onDisable() {
		DBConnection.sql.close();
	}
	
	public void configCheck() {
		getConfig().addDefault("MaxBountiesPerPlayer", 0);
		getConfig().addDefault("MaxBountiesOnPlayer", 5);
		List<String> applicableItems = new ArrayList<String>();
		applicableItems.add("DIAMOND");
		applicableItems.add("EMERALD");
		applicableItems.add("GOLD_INGOT");
		applicableItems.add("IRON_INGOT");
		getConfig().addDefault("ApplicableItems", applicableItems);
		getConfig().addDefault("MaxItems", 64);
		getConfig().addDefault("NotifyOnJoinDistance", 150);
		List<String> applicableWorlds = new ArrayList<String>();
		applicableWorlds.add("world");
		applicableWorlds.add("world_nether");
		getConfig().addDefault("ExpirationTime", 259200);
		
		getConfig().options().copyDefaults(true);
		saveConfig();
	}
	
	public static PhysicalBounty getInstance() {
		return instance;
	}

	
}