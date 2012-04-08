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

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.entity.Player;

/**
 * @author BlackVoid
 */
public class DynamicEffectPlayerListener implements Listener {
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLogin(PlayerLoginEvent event){
    	if( DynamicEffectWhitelist.WhitelistON == true){
	    	Player player = event.getPlayer();
	    	//Checks if player is on the whitelist
	    	if(!DynamicEffectWhitelist.WhiteListedPlayers.contains(player.getName().toLowerCase())){
	    		//Kick message
	    		String DisMSG = DynamicEffectWhitelist.Settings.getString("General.DisconnectMessage")
	    				.replace("{player}", player.getName());
	    		event.setKickMessage(DisMSG);
	    		event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
	    		
	    		DynamicEffectWhitelist.log.log(Level.INFO,"[DEWhitelist] " + player.getName() + " is not whitelisted! Deny!");
	    		//Check if print connection failures is enabled
	    		if(DynamicEffectWhitelist.Settings.getBoolean("General.PrintConnectFails") == true){
					Bukkit.getServer().broadcast("§7[DEW] §f" + DynamicEffectWhitelist.Settings.getString("General.DisallowMessage").replace("{player}", player.getName()), "dewhitelist.displayfails");
	    		}
	    	}
    	}
    }
}