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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config {
	public static YamlConfiguration Config;

	public static void SetDefault(String Path, Object Value){
		Config.set(Path, Config.get(Path, Value));
	}
	
	public static YamlConfiguration loadMain(boolean Create){
		String maindir = "plugins/Dynamic Effect Whitelist/";
		File Settings = new File(maindir + "config.yml");
		try {
			Config = new YamlConfiguration();
			if(Create == false){
				Config.load(Settings);
			}
			SetDefault("General.WhitelistOn", true);
			SetDefault("General.ConnectionType", "file");
			SetDefault("General.UpdateInterval", 60);
			SetDefault("General.PrintConnectFails", false);
			SetDefault("General.DisconnectMessage", "Hi §1{player}§f, you are not on the whitelist!");
			SetDefault("General.DisallowMessage", "Disallowed §2{player}§f from joining");
			SetDefault("sql.type", "mysql");
			SetDefault("sql.host", "localhost");
			SetDefault("sql.port", 3306);
			SetDefault("sql.username", "username");
			SetDefault("sql.password", "password");
			SetDefault("sql.table", "table");
			SetDefault("sql.database", "minecraft");
			SetDefault("sql.query", "SELECT {name} FROM `{table}`;");
			SetDefault("sql.UserField", "name");
			SetDefault("Other.url", "http://mywebsite.com/whitelist.php");
			SetDefault("Other.file", "white-list.txt");
			SetDefault("Other.debug", false);
			Config.save(Settings);
			return Config;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}
}
