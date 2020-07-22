/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ericsson.codepilot;

import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sqlite.SQLiteConfig;

/**
 * @author lucaswang
 */
public class DBConvert {
    private String dbName;
    private String driver;

    public DBConvert() {
    }

    public DBConvert(String dbName) {
        this.dbName = dbName;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public void setDriver(String driver){
        this.driver = driver;
    }
    public String getDriver(){
        return this.driver;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage:");
            System.out.println(" java -jar DBConvert.jar <mysql connection string> <sqlite connection string>");
            System.out.println("\n");
            System.out.println("Example:");
            System.out.println(" java -jar DBConvert.jar \"jdbc:mysql://localhost/codepilot?user=root&password=root\" \"jdbc:sqlite:/codepilot/codepilot.db\"");
            System.out.println("");
            return;
        }

        String con_str = args[0];

        // TODO code application logic here
        try {
            DBConvert convert = new DBConvert();
            convert.setDbName(convert.get_dbName_from(con_str));
            if (convert.getDbName() == null) {
                System.out.println("No db name checked.");
                return;
            }

            //String con_str = "jdbc:mysql://localhost/codepilot?user=cpserver&password=cptest";
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Connection conn_mysql = DriverManager.getConnection(con_str);

            Class.forName("org.sqlite.JDBC").newInstance();
            SQLiteConfig config = new SQLiteConfig();
            //config.setEncoding(SQLiteConfig.Encoding.UTF8);            

            //con_str = "jdbc:sqlite:D:\\My Projects\\db\\test.db";
            con_str = args[1];
            Connection conn_sqlite = DriverManager.getConnection(con_str, config.toProperties());

            if (conn_mysql == null) {
                System.out.println("Failed to connect to MySQL: " + args[0]);
                return;
            }
            if (conn_sqlite == null) {
                System.out.println("Failed to open SQLite DB: " + args[1]);
                return;
            }
            conn_sqlite.setAutoCommit(false);
            // Move data.
            //String tables[] = {"product_module", "module", "section", "productstream",
            //        "patch_info", "patch_pcl", "patch_product",
            //        "patch_module", "dru_map"};
            String[] tables = convert.get_tables(conn_mysql);
            if (tables == null) {
                System.out.println("No table got.");
                return;
            }

            System.out.println("Describe tables...");
            System.out.println("==================");
            for (int t = 0; t < tables.length; t++) {
                String desc = convert.get_table_desc(conn_mysql, tables[t]);
                System.out.println(desc);
                System.out.println("--");
            }

            for (int i = 0; i < tables.length; i++) {
                System.out.println("Copying table [" + tables[i] + "]... ");
                boolean rc = convert.copy_table(conn_mysql, conn_sqlite, tables[i]);
                if (rc) {
                    System.out.println("Done with table copying.");
                    // Reopen the connection to prevent from out of memory.
                    conn_sqlite.close();
                    conn_sqlite = DriverManager.getConnection(con_str, config.toProperties());
                    if (conn_sqlite != null)
                        conn_sqlite.setAutoCommit(false);
                    else {
                        System.out.println("Failed to re-establish connection to: " + con_str);
                        return;
                    }
                } else {
                    System.out.println("Failed to copy table!");
                    return;
                }
            }

        } catch (Exception ex) {
            System.err.println(ex);
        }
    }

    public boolean create_sqlite_table(Connection conn_dst, String table_desc) {
        try {
            PreparedStatement q2 = conn_dst.prepareStatement(table_desc);
            int rows = q2.executeUpdate();

            conn_dst.commit();

        } catch (Exception ex) {
            Logger.getLogger(DBConvert.class.getName()).log(Level.SEVERE, ex.getMessage());
            try {
                conn_dst.rollback();
                return false;
            } catch (SQLException ex1) {
                Logger.getLogger(DBConvert.class.getName()).log(Level.SEVERE, ex1.getMessage());
                return false;
            }
        }

        return true;
    }


    public boolean copy_table(Connection conn_src, Connection conn_dst, String table) {
        try {
            ResultSet rs1;
            PreparedStatement q1 = conn_src.prepareStatement(" select * from " + table);
            rs1 = q1.executeQuery();

            while (rs1.next()) {
                ResultSetMetaData md = rs1.getMetaData();

                int cols = md.getColumnCount();

                StringBuffer sb = new StringBuffer();
                sb.append("insert into " + table + " values (");
                for (int i = 0; i < cols; i++) {
                    if (i < cols - 1)
                        sb.append("?, ");
                    else
                        sb.append("?)");
                }

                PreparedStatement q2 = conn_dst.prepareStatement(sb.toString());
                for (int i = 1; i <= cols; i++) {
                    switch (md.getColumnType(i)) {
                        case Types.INTEGER:
                        case Types.TINYINT:
                        case Types.BIGINT:
                        case Types.SMALLINT:
                        case Types.NUMERIC:
                            q2.setInt(i, rs1.getInt(i));
                            break;
                        case Types.VARCHAR:
                        case Types.NVARCHAR:
                        case Types.CHAR:
                        case Types.NCHAR:
                        case Types.DATE:
                        case Types.TIMESTAMP:
                            q2.setString(i, rs1.getString(i));
                            break;
                        //q2.setDate(i, rs1.getDate(i));
                        default:
                            System.out.println("Unknown type: " + md.getColumnType(i));
                    }
                }
                int rows = q2.executeUpdate();

            }
            conn_dst.commit();
        } catch (Exception ex) {
            Logger.getLogger(DBConvert.class.getName()).log(Level.SEVERE, ex.getMessage());
            try {
                conn_dst.rollback();
                return false;
            } catch (SQLException ex1) {
                Logger.getLogger(DBConvert.class.getName()).log(Level.SEVERE, ex1.getMessage());
                return false;
            }
        }
        return true;
    }

    public String[] get_tables(Connection conn_src) {
        ArrayList<String> tables = null;

        ResultSet rs1 = null;
        PreparedStatement q1 = null;
        try {
            String sql = String.format("select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_SCHEMA='%s' and TABLE_NAME <> 'schema_version'", dbName);
            q1 = conn_src.prepareStatement(sql);
            rs1 = q1.executeQuery();

            if (rs1 != null) {
                tables = new ArrayList<String>();

                while (rs1.next()) {
                    tables.add(rs1.getString("TABLE_NAME"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (tables == null) {
            return null;
        }

        String[] arrays = new String[tables.size()];
        return tables.toArray(arrays);
    }


    /**
     * get table description of creation
     * @param conn_src
     * @param table
     * @return
     */
    public String get_table_desc(Connection conn_src, String table) {
        String tableDesc = null;

        ResultSet rs1 = null;
        PreparedStatement q1 = null;
        try {
            String sql = String.format("SHOW CREATE TABLE %s", table);
            q1 = conn_src.prepareStatement(sql);
            rs1 = q1.executeQuery();

            if (rs1 != null) {
                while (rs1.next()) {
                    tableDesc = rs1.getString("Create Table");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tableDesc;
    }


    /**
     * get database name from connection string.
     * @param conn
     * @return
     */
    private String get_dbName_from(String conn) {
        if (conn.contains("mysql")) {
            String db = null;
            String[] ss = conn.split("\\?");
            if (ss == null) {
                return null;
            }

            if (ss.length >= 1) {
                ss = ss[0].split("/|//");
                if (ss == null) {
                    return null;
                }

                db = ss[ss.length - 1];
            }

            return db;
        }

        return null;
    }
}
