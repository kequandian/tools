/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ericsson.codepilot;

import java.sql.*;
import org.sqlite.SQLiteConfig;
/**
 *
 * @author lucaswang
 */
public class TestSQLite {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        // TODO code application logic here
        try
        {
            Class.forName("org.sqlite.JDBC").newInstance();
            String con_str  = "jdbc:sqlite:C:\\Users\\ezeiwng\\Documents\\My Projects\\db\\test.db";
            SQLiteConfig config = new SQLiteConfig();            
            config.setReadOnly(true);
            //String con_str con_str = "jdbc:sqlite:/codepilot/db/test.db";
            Connection conn = DriverManager.getConnection(con_str,  config.toProperties()); 
           

            if ( conn == null)
            {
                System.out.println("Failed to open SQLite DB: " +  con_str);
                return;
            }
            if (!conn.isReadOnly())
            {
                System.out.println("Not readonly!!!");
                //return;            	
            }
            
            {
                System.out.println("Good, readonly DB!");
                
                Statement s = conn.createStatement();
                s.execute("pragma read_uncommitted = 1;");
                s.close();
                ResultSet rs1;
                PreparedStatement q1 = conn.prepareStatement(" select * from dru_map");
                rs1 = q1.executeQuery();
                System.in.read();
                 rs1.close();
               return;            	
            }
        }
        catch (Exception ex) { System.err.println(ex);}
    
    
    }
    
}
