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
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.logging.Level;

public class SQLConnection {

    public static Connection getSQLConnection() {
    	
        try {
            return DriverManager.getConnection("jdbc:" + DynamicEffectWhitelist.Settings.getString("sql.type")+"://" + DynamicEffectWhitelist.Settings.getString("sql.host") + ":" + DynamicEffectWhitelist.Settings.getInt("sql.port") +"/" + DynamicEffectWhitelist.Settings.getString("sql.database") + "?autoReconnect=true&user=" + DynamicEffectWhitelist.Settings.getString("sql.username") + "&password=" + DynamicEffectWhitelist.Settings.getString("sql.password"));
        } catch (SQLException ex) {
        	DynamicEffectWhitelist.log.log(Level.SEVERE, "Unable to retreive connection", ex);
        }
        return null;
    }
	
}
