/////////////////////////////////
//       ©2012 BlackVoid       //
//     All rights reserved     //
/////////////////////////////////
//  This plugin is using the   //
//           license           //
//  Attribution-NonCommercial  //
//   -ShareAlike 3.0 Unported  //
//      (CC BY-NC-SA 3.0)      //
/////////////////////////////////

package com.dynamiceffect.whitelist;

import java.net.URL;
import java.net.URLConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.dynamiceffect.whitelist.Metrics.Graph;

/**
 * @author BlackVoid
 */

public class DynamicEffectWhitelist extends JavaPlugin {

	public static final Logger log = Logger.getLogger("Minecraft");

	static String maindir = "plugins/Dynamic Effect Whitelist/";
	public static YamlConfiguration Settings;
	static File SettingsFile = new File(maindir + "config.yml");
	static ArrayList<String> WhiteListedPlayers = new ArrayList<String>();
	int RefreshWhitelistTaskID = -1;
	static boolean WhitelistON = true;

	public void onDisable() {
		//Clears the whitelist array to clear up memory
		WhiteListedPlayers = new ArrayList<String>();
		//Removes the config to clear up memory
		Settings = null;
		
		this.getServer().getScheduler().cancelAllTasks();
		RefreshWhitelistTaskID = -1;
	}

	public void onEnable() {
		new File(maindir).mkdir();
		//Checks if file exists
		if (!SettingsFile.exists()) {
			//Creates the config file if it doesn't exist
			try {
				SettingsFile.createNewFile();
				Settings = Config.loadMain(true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			//If it exists load the config
			Settings = Config.loadMain(false);
		}
		// Sets the whitelists mode
		WhitelistON = DynamicEffectWhitelist.Settings.getBoolean("General.WhitelistOn");
		
		PluginManager pm = getServer().getPluginManager();
		
		//Registers the Listener class
		pm.registerEvents(new DynamicEffectPlayerListener(), this);
		
		//Prints if its on or off.
		log.log(Level.INFO, "[DEWhitelist] Whitelist is " + (WhitelistON == true ? "on" : "off"));
		//If debug mode is on this message will print to notify the user
		DebugPrint("Debug mode is on! Turn it off in the config if you do not want to see debug messages.");
		RefreshWhitelist(true);
		//If the refresh timer doesnt exist it creates one.
		if(RefreshWhitelistTaskID < 0){
			RefreshWhitelistTaskID = getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
				public void run() {
					//Refreshes the whitelist
					RefreshWhitelist(false);
				}
			}, 0, Settings.getInt("General.UpdateInterval") * 20);
		}
		
		//Loads Metrics
		try {
		    Metrics metrics = new Metrics(this);

		    Graph graph = metrics.createGraph("Data source usage");
		    graph.addPlotter(new Metrics.Plotter("MySQL") {

		            @Override
		            public int getValue() {
		                    return DynamicEffectWhitelist.Settings.getString("General.ConnectionType").equals("sql")? 1 : 0;
		            }

		    });
		    graph.addPlotter(new Metrics.Plotter("File") {

		            @Override
		            public int getValue() {
		                    return DynamicEffectWhitelist.Settings.getString("General.ConnectionType").equals("file")? 1 : 0;
		            }

		    });
		    graph.addPlotter(new Metrics.Plotter("URL") {

		            @Override
		            public int getValue() {
		                    return DynamicEffectWhitelist.Settings.getString("General.ConnectionType").equals("url")? 1 : 0;
		            }

		    });
		    
		    //Starts Metrics
		    metrics.start();
		} catch (IOException e) {
			log.log(Level.WARNING, "Error in Metrics: " + e.getMessage());
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
		String commandLabel, String[] args) {
		String commandName = command.getName().toLowerCase();
		String[] trimmedArgs = args;

		if (commandName.equals("whitelist")) {
			//Sends all /whitelsit chat messages to CommandHandler
			return CommandHandler(sender, trimmedArgs);
		}
		return false;
	}
	
	public static void SendMessage(CommandSender Player, String MSG){
		Player.sendMessage("§7[DEW] §f" + MSG);
	}

	public boolean CommandHandler(CommandSender sender, String[] trimmedArgs) {
		if (trimmedArgs.length > 0) {
			//Creates a new array without the first value i.e trimmedString[0]
			String[] args = RearangeString(1, trimmedArgs);
			String CommandName = trimmedArgs[0];
			if (CommandName.equals("add")) {
				return AddPlayerToWhitelist(sender, args);
			}
			if (CommandName.equals("remove")) {
				return RemovePlayerFromWhitelist(sender, args);
			}
			if (CommandName.equals("reload")) {
				return ReloadPlugin(sender, args);
			}
			if (CommandName.equals("refresh")) {
				return RefreshWhitelist(sender, args);
			}
			if (CommandName.equals("import")) {
				return ImportWhitelist(sender, args);
			}
			if (CommandName.equals("on")) {
				return WhitelistOn(sender, args);
			}
			if (CommandName.equals("off")) {
				return WhitelistOff(sender, args);
			}
		}
		//If no function has handles the command then print the helper.
		PrintHelper(sender);
		return true;
	}
	
	private boolean WhitelistOn(CommandSender sender, String[] args){
		boolean auth = false;
		Player player = null;
		//Checks for permission
		if (sender instanceof Player) {
			player = (Player) sender;
			if (player.hasPermission("dewhitelist.mode"))
				auth = true;
		} else {
			auth = true;
		}
		if (auth) {
			//Turns the whitelist on, doesnt change the config value though.
			WhitelistON = true;
			log.log(Level.INFO, "[DEWhitelist] "
					+ (player == null ? "console" : player.getName())
					+ " turned on the whitelist!");
			sender.sendMessage("[DEWhitelist] Whitelist is now on!");
			return true;
		}
		sender.sendMessage("§6You have no access to this command!");
		return true;
	}
	
	private boolean WhitelistOff(CommandSender sender, String[] args){
		boolean auth = false;
		Player player = null;
		//Checks for permission
		if (sender instanceof Player) {
			player = (Player) sender;
			if (player.hasPermission("dewhitelist.mode"))
				auth = true;
		} else {
			auth = true;
		}
		if (auth) {
			//Turns the whitelist off, doesnt change the config value though.
			WhitelistON = false;
			log.log(Level.INFO, "[DEWhitelist] "
					+ (player == null ? "console" : player.getName())
					+ " turned on the whitelist!");
			sender.sendMessage("[DEWhitelist] Whitelist is now off!");
			return true;
		}
		sender.sendMessage("§6You have no access to this command!");
		return true;
	}
	
	private boolean RefreshWhitelist(CommandSender sender, String[] args){
		boolean auth = false;
		Player player = null;
		//Checks for permission
		if (sender instanceof Player) {
			player = (Player) sender;
			if (player.hasPermission("dewhitelist.refresh"))
				auth = true;
		} else {
			auth = true;
		}
		if (auth) {
			log.log(Level.INFO, "[DEWhitelist] "
					+ (player == null ? "console" : player.getName())
					+ " refreshed the whitelist!");
			sender.sendMessage("[DEWhitelist] Successfully refreshed the whitelist!");
			//Runs refresh whitelist function
			(new UpdateWhitelist(false)).start();
			return true;
		}
		sender.sendMessage("§6You have no access to this command!");
		return true;
	}
	
	private void PrintHelper(CommandSender sender){
		sender.sendMessage("Commands:");
		sender.sendMessage("    add [player] - Adds a player to the whitelist");
		sender.sendMessage("    remove [player] - Removes a player from the whitelist");
		sender.sendMessage("    reload - Reloads the plugin");
		sender.sendMessage("    import [source] [destination] - imports whitelist from source to destination");
		sender.sendMessage("    refresh - Refreshes the whitelist");
	}
	
	private boolean ReloadPlugin(CommandSender sender, String[] args){
		boolean auth = false;
		Player player = null;
		//Checks for permission
		if (sender instanceof Player) {
			player = (Player) sender;
			if (player.hasPermission("dewhitelist.reload"))
				auth = true;
		} else {
			auth = true;
		}
		if (auth) {
			//Disables the plugin
			onDisable();
			//Enables the plugin
			onEnable();
			log.log(Level.INFO, "[DEWhitelist] "
					+ (player == null ? "console" : player.getName())
					+ " reloaded the plugin!");
			sender.sendMessage("[DEWhitelist] Successfully reloaded the plugin!");
			return true;
		}
		sender.sendMessage("§6You have no access to this command!");
		return true;
	}
	
	private ArrayList<String> GetWhitelist(String Type){
		//Makes an empty temporary array
		ArrayList<String> tmpArray = new ArrayList<String>();
		//Checks which connection type you are using
		if(Type.equals("file")){
			FileInputStream in;
			try {
				in = new FileInputStream(DynamicEffectWhitelist.Settings.getString("Other.file"));
			
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String strLine;
				//Adds each line(player) to the temporary array
				while ((strLine = br.readLine()) != null) {
					tmpArray.add(strLine.toLowerCase());
				}
				//If debug mode is on it will print connection type and people in the whitelist
				DebugPrint("Whitelist (type:" + Type +" count: " + tmpArray.toArray().length + ")");
				in.close();
				//Returns all players in the whitelist
				return tmpArray;
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}else if(Type.equals("sql")){
				Connection conn = SQLConnection.getSQLConnection();
				if (conn == null) {
					log.log(Level.SEVERE,
							"[DEWhitelist] Could not establish SQL connection.");
					return null;
				} else {
	
					PreparedStatement ps = null;
					ResultSet rs = null;
					try {
						//Loads the query and replaces all variables with their values.
						String Query = DynamicEffectWhitelist.Settings.getString("sql.queries.Select").replace("{table}",
								DynamicEffectWhitelist.Settings.getString("sql.table")).replace("{name}",
								DynamicEffectWhitelist.Settings.getString("sql.UserField")).replace("{time}",
								"" + GetTime());
						ps = conn.prepareStatement(Query);
						rs = ps.executeQuery();
						//Adds each player retrived to the array
						while (rs.next()) {
							tmpArray.add(rs.getString(
									DynamicEffectWhitelist.Settings.getString("sql.UserField")).toLowerCase());
						}
						//If debug mode is on it will print connection type and people in the whitelist
						DebugPrint("Whitelist (type:" + Type +" count: " + tmpArray.toArray().length + ")");
						
						//Returns all players in the whitelist
						return tmpArray;
					} catch (SQLException ex) {
						log.log(Level.SEVERE,
								"[DEWhitelist] Couldn't execute SQL statement: ",
								ex);
					} finally {
						try {
							if (ps != null)
								ps.close();
							if (conn != null)
								conn.close();
						} catch (SQLException ex) {
							log.log(Level.SEVERE,
									"[DEWhitelist] Failed to close db connection: ",
									ex);
						}
					}
	
					try {
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
		}else if(Type.equals("url")){
				try {
					URL url = new URL(DynamicEffectWhitelist.Settings.getString("Other.url"));
					URLConnection con = url.openConnection();
	
					InputStream istream = con.getInputStream();
					String Content = "";
					int ch;
					byte[] bytes = new byte[1];
					while ((ch = istream.read()) != -1) {
						bytes[0] = (byte) ch;
						Content = Content + new String(bytes);
					}
					//Makes a new array entry for each | character
					String[] tmpWL = Content.split("\\|");
					if (tmpWL.length > 0) {
						//Adds each player to the array retrived from the URL
						for (int i = 0; i < tmpWL.length; i++) {
							tmpArray.add(tmpWL[i].toLowerCase());
						}
						//If debug mode is on it will print connection type and people in the whitelist
						DebugPrint("Whitelist (type:" + Type +" count: " + tmpArray.toArray().length + ")");
						istream.close();
						
						//Returns all players in the whitelist
						return tmpArray;
					}
					istream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}else{
			DynamicEffectWhitelist.log.log(Level.SEVERE,"[DEWhitelist] Connection Type: \"" + DynamicEffectWhitelist.Settings.getString("General.ConnectionType") + "\" does not exist!");
		}
		return null;
	}
	
	private boolean ImportWhitelist(CommandSender sender, String[] args){
		boolean auth = false;
		Player player = null;
		//Checks for permission
		if (sender instanceof Player) {
			player = (Player) sender;
			if (player.hasPermission("dewhitelist.import"))
				auth = true;
		} else {
			auth = true;
		}
		if (auth) {
			//Checks if number of arguments is 3 or 2
			if(args.length == 2 || args.length == 3){
				String source = args[0];
				String target = args[1];
				//Checks if source and destination is the same
				if(target.equals(source)){
					sender.sendMessage("[DEWhitelist] Source and target can't be the same!");
					return true;
				}
				ArrayList<String> TmpList = new ArrayList<String>();
				//Checks if source is one of the valid sources
				if(source.equals("file") || source.equals("sql") || source.equals("url") || source.equals("world")){
					if(source.equals("world")){
						if(args.length == 2){
							sender.sendMessage("[DEWhitelist] World name missing!");
							sender.sendMessage("/whitelist import world " + target + " [world name]");
							return true;							
						}
						File levelFile = new File(args[2]+"/level.dat");
						if(levelFile.exists() && levelFile.isFile()){
							File PlayerFolder = new File(args[2]+"/players");
							File[] Players = PlayerFolder.listFiles();
							if(Players == null){
								sender.sendMessage("[DEWhitelist] No players exists in that world!");
								return true;
							}
							for (File Player : Players) {
								if(Player.isFile()){
									TmpList.add(Player.getName().split("\\.")[0]);										
								}
							}
						}else{
							sender.sendMessage("[DEWhitelist] The world \"" + args[2] + "\" does not exist!");
							return true;							
						}
					}else{
						TmpList = GetWhitelist(source);
						if(TmpList.equals(null)){
							sender.sendMessage("[DEWhitelist] An error occured. Check console for errors!");
							return true;
						}
					}
				}else {
					sender.sendMessage("[DEWhitelist] The source \"" + source + "\" does not exist!");
					return true;
				}
				
				if(!target.equals("file") && !target.equals("sql") && !target.equals("url")){
					sender.sendMessage("[DEWhitelist] The target \"" + source + "\" does not exist!");
					return true;
				}
				
				ArrayList<String> TargetList = GetWhitelist(target);
				if(TargetList.equals(null)){
					sender.sendMessage("[DEWhitelist] An error occured when retriving the target whitelist!");
					return true;
				}
				int OldSize = TargetList.size();
				int Difference = 0;
				if(target.equals("file")){
					for(int i = 0; i<TmpList.size(); i++){
						if(!TargetList.contains(TmpList.get(i))){
							TargetList.add(TmpList.get(i));
						}
					}
					try{
						BufferedWriter fW = new BufferedWriter(new FileWriter(DynamicEffectWhitelist.Settings.getString("Other.file")));
						for(int i = 0; i< TargetList.size(); i = i + 1){
							fW.write(TargetList.get(i));
							fW.newLine();
						}
					    fW.close();
					}catch (Exception e){
						e.printStackTrace();
					}
					Difference = TargetList.size() - OldSize;
					sender.sendMessage("§6Successfully imported whitelist! " + Difference + " new " + (Difference==1?"entry":"entries") + "!");
					return true;
				}else if(target.equals("sql")){
					Connection conn = null;
					PreparedStatement ps = null;
					conn = SQLConnection.getSQLConnection();
					if (conn == null) {
						log.log(Level.SEVERE,
								"[DEWhitelist] Could not establish SQL connection.");
						sender.sendMessage("[DEWhitelist] An unknown when connecting to the sql server!");
						return true;
					}
					for(int i = 0; i<TmpList.size(); i++){
						if(!TargetList.contains(TmpList.get(i))){
							TargetList.add(TmpList.get(i));
							try {
								ps = conn.prepareStatement("INSERT INTO "
										+ Settings.getString("sql.table") + " (name) VALUES(?)");
								ps.setString(1, TmpList.get(i));
								ps.executeUpdate();
							} catch (SQLException ex) {
								sender.sendMessage("[DEWhitelist] An error occured check console for errors!");
								log.log(Level.SEVERE,
										"[DEWhitelist] Couldn't execute SQL statement: ",
										ex);
								return true;
							}
						}
					}
					try {
						ps.close();
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
						sender.sendMessage("[DEWhitelist] An unknown error occured!");
					}
					Difference = TargetList.size() - OldSize;
					if(DynamicEffectWhitelist.Settings.getString("General.ConnectionType") == target){
						DynamicEffectWhitelist.WhiteListedPlayers = GetWhitelist(target);
					}
					sender.sendMessage("§6Successfully imported whitelist! " + Difference + " new entries.");
					return true;
				}else if(target.equals("url")){
					sender.sendMessage("[DEWhitelist] You can't import a whitelist to a URL!");
					return true;
				}else{
					sender.sendMessage("[DEWhitelist] The datatype: " + target + " doesn't exist!");
					return true;
				}
			}else{
				sender.sendMessage("/whitelist import [source] [target]");
				return true;
			}
		}
		sender.sendMessage("§6You have no access to this command!");
		return true;
	}

	private boolean AddPlayerToWhitelist(CommandSender sender, String[] args) {
		boolean auth = false;
		Player player = null;
		//Checks for permission
		if (sender instanceof Player) {
			player = (Player) sender;
			if (player.hasPermission("dewhitelist.add"))
				auth = true;
		} else {
			auth = true;
		}
		if (auth) {
			if (args.length > 0) {
				String p = args[0];

				if (DynamicEffectWhitelist.WhiteListedPlayers.contains(p
						.toLowerCase())) {
					sender.sendMessage("§cPlayer §e" + p
							+ " §cis already whitelisted!");
					return true;
				}

				DynamicEffectWhitelist.WhiteListedPlayers.add(p.toLowerCase());
				String ConType = DynamicEffectWhitelist.Settings.getString("General.ConnectionType");
				if(ConType.equals("file")){
						try{
							BufferedWriter fW = new BufferedWriter(new FileWriter(DynamicEffectWhitelist.Settings.getString("Other.file")));
							for(int i = 0; i< DynamicEffectWhitelist.WhiteListedPlayers.size(); i = i + 1){
								fW.write(DynamicEffectWhitelist.WhiteListedPlayers.get(i));
								fW.newLine();
							}
						    fW.close();
						}catch (Exception e){
							e.printStackTrace();
						}
						sender.sendMessage("§6Successfully whitelisted " + p + "!");
						return true;
				}else if(ConType.equals("sql")){
						Connection conn = null;
						PreparedStatement ps = null;
						try {
							conn = SQLConnection.getSQLConnection();
							ps = conn.prepareStatement(DynamicEffectWhitelist.Settings.getString("sql.queries.Insert").replace("{table}",
														DynamicEffectWhitelist.Settings.getString("sql.table")).replace("{name}",
														DynamicEffectWhitelist.Settings.getString("sql.UserField")).replace("{time}",
														"" + GetTime()));
							ps.setString(1, p);
							ps.executeUpdate();
						} catch (SQLException ex) {
							log.log(Level.SEVERE,
									"[DEWhitelist] Couldn't execute SQL statement: ",
									ex);
						} finally {
							try {
								if (ps != null)
									ps.close();
								if (conn != null)
									conn.close();
							} catch (SQLException ex) {
								log.log(Level.SEVERE,
										"[DEWhitelist] Failed to close db connection: ",
										ex);
							}
						}

						log.log(Level.INFO, "[DEWhitelist] "
								+ (player == null ? "console" : player.getName())
								+ " whitelisted player " + p + ".");
						sender.sendMessage("§6Successfully whitelisted " + p + "!");
						return true;
				}else if(ConType.equals("url")){
						sender.sendMessage("§6You cant add people to the whitelist with this connection type!");
						return true;
				}else{
					DynamicEffectWhitelist.log.log(Level.SEVERE,"[DEWhitelist] Connection Type: \"" + DynamicEffectWhitelist.Settings.getString("General.ConnectionType") + "\" does not exist!");
				}
			}
		}
		sender.sendMessage("§6You have no access to this command!");
		return true;
	}

	private boolean RemovePlayerFromWhitelist(CommandSender sender, String[] args) {
		boolean auth = false;
		Player player = null;
		//Checks for permission
		if (sender instanceof Player) {
			player = (Player) sender;
			if (player.hasPermission("dewhitelist.remove"))
				auth = true;
		} else {
			auth = true;
		}
		if (auth) {
			if (args.length > 0) {
				String p = args[0];
				Player victim = this.getServer().getPlayer(p);

				if (!DynamicEffectWhitelist.WhiteListedPlayers.contains(p
						.toLowerCase())) {
					sender.sendMessage("§cPlayer §e" + p
							+ " §cis not whitelisted!");
					return true;
				}

				if (victim != null) {
					p = victim.getName();
				}

				DynamicEffectWhitelist.WhiteListedPlayers.remove(p
						.toLowerCase());
				String ConType = DynamicEffectWhitelist.Settings.getString("General.ConnectionType");
				if(ConType.equals("file")){
					try{
						BufferedWriter fW = new BufferedWriter(new FileWriter(DynamicEffectWhitelist.Settings.getString("Other.file")));
						for(int i = 0; i< DynamicEffectWhitelist.WhiteListedPlayers.size(); i = i + 1){
							fW.write(DynamicEffectWhitelist.WhiteListedPlayers.get(i));
							fW.newLine();
						}
					    fW.close();
					}catch (Exception e){
						e.printStackTrace();
					}
				}else if(ConType.equals("sql")){
					Connection conn = null;
					PreparedStatement ps = null;
					try {
						conn = SQLConnection.getSQLConnection();
						ps = conn.prepareStatement(DynamicEffectWhitelist.Settings.getString("sql.queries.Delete").replace("{table}",
								DynamicEffectWhitelist.Settings.getString("sql.table")).replace("{name}",
								DynamicEffectWhitelist.Settings.getString("sql.UserField")).replace("{time}",
								"" + GetTime()));
						ps.setString(1, p);
						ps.executeUpdate();
					} catch (SQLException ex) {
						log.log(Level.SEVERE,
								"[DEWhitelist] Couldn't execute SQL statement: ",
								ex);
					} finally {
						try {
							if (ps != null)
								ps.close();
							if (conn != null)
								conn.close();
						} catch (SQLException ex) {
							log.log(Level.SEVERE,
									"[DEWhitelist] Failed to close SQL connection: ",
									ex);
						}
					}
				}else if(ConType.equals("url")){
					sender.sendMessage("§6You cant add people to the whitelist with this connection type!");
					return true;
				}else{
					DynamicEffectWhitelist.log.log(Level.SEVERE,"[DEWhitelist] Connection Type: \"" + DynamicEffectWhitelist.Settings.getString("General.ConnectionType") + "\" does not exist!");
					return true;
				}
				sender.sendMessage("§6Successfully removed " + p
						+ "from the whitelist!");
				return true;
			}
		}
		sender.sendMessage("§6You have no access to this command!");
		return true;
	}

	public static void DebugPrint(String MSG) {
		if (Settings.getBoolean("Other.debug")) {
			log.log(Level.INFO, "[DEWhitelist-debug] " + MSG);
		}
	}

	public static String[] RearangeString(int startIndex, String[] string) {
		String TMPString = "";
		for (int i = startIndex; i < string.length; i++) {
			String Add = " ";
			if (i == startIndex) {
				Add = "";
			}
			TMPString += Add + string[i];
		}
		return TMPString.split("\\ ");
	}

	public void RefreshWhitelist(Boolean First) {
		new UpdateWhitelist(First).run();
	}

	public long GetTime() {
		return System.currentTimeMillis() / 1000L;
	}

	class UpdateWhitelist extends Thread {

		private Boolean First;

		public UpdateWhitelist(Boolean First) {
			this.First = First;
		}

		public void run() {
			String ConType = DynamicEffectWhitelist.Settings.getString("General.ConnectionType");
			if(ConType.equals("file") || ConType.equals("sql") || ConType.equals("url")){
				ArrayList<String> TmpArray = new ArrayList<String>();
				TmpArray = GetWhitelist(ConType);
				if(!TmpArray.equals(null)){
					if(First){
						log.log(Level.INFO, "[DEWhitelist] Whitelist retrived successfully!");
					}
					DynamicEffectWhitelist.WhiteListedPlayers = TmpArray;
				}
				TmpArray = null;
				return;
			}
			log.log(Level.SEVERE, "[DEWhitelist] The source \"" + ConType + "\" does not exist!");
		}
	}
}
