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
		//Gets the value of the path and if it doesnt exist it sets it to the deafult value.
		Config.set(Path, Config.get(Path, Value));
	}
	
	public static void ChangePath(String OldPath, String NewPath){
		//Gets the old paths value and sets that value for the new path.
		Config.set(NewPath, Config.get(OldPath, Config.get(NewPath, null)));
		//Removes the old path
		Config.set(OldPath, null);
	}
	
	public static YamlConfiguration loadMain(boolean Create){
		String maindir = "plugins/Dynamic Effect Whitelist/";
		File Settings = new File(maindir + "config.yml");
		try {
			Config = new YamlConfiguration();
			if(Create == false){
				Config.load(Settings);
			}
			
			//Changes the old paths to the new paths.
			ChangePath("sql.query","sql.queries.Select");
			
			//Sets all default values, if yml path doesnt exist it sets it to default else it gets the value.
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
			SetDefault("sql.UserField", "name");
			SetDefault("sql.queries.Select", "SELECT `{name}` FROM `{table}`;");
			SetDefault("sql.queries.Insert", "INSERT INTO `{table}` (`{name}`) VALUES(?);");
			SetDefault("sql.queries.Delete", "DELETE FROM `{table}` WHERE `{name}` = ?");
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
		} catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}
}
