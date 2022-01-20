package com.tools;

import java.io.*;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.sql.*;
import java.util.Locale;

public class MySqlTest {
   //String connStr = "jdbc:mysql://localhost/codepilot?user=root&password=root";

    Connection conn = null;

    public MySqlTest(String connStr) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(connStr);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ResultSet executeQuery(String sql) {
        ResultSet rs=null;
        try {
            PreparedStatement q1 = conn.prepareStatement(sql);
            rs = q1.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }

    public int executeUpdate(String sql){
        int affectedRows = 0;

        try {
            conn.setAutoCommit(false);

            if (!sql.contains(";")) {
                PreparedStatement q1 = conn.prepareStatement(sql);
                affectedRows = q1.executeUpdate();
                conn.commit();

            } else {
                String[] sqlset = sql.split("\\;");

                for (int i = 0; i < sqlset.length; i++) {
                    sql = sqlset[i];

                    // check empty
                    if(sql==null || sql.length()==0) continue;

                    // trim space
                    sql = sql.trim(); if(sql.length() == 0) continue;
                    sql = sql.replace("\\`", "`");

                    PreparedStatement q1 = conn.prepareStatement(sql);
                    affectedRows = q1.executeUpdate();
                }

                conn.commit();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(sql);
        }

        return affectedRows;
    }

    private static String readFile(File fin){
        StringBuilder builder = new StringBuilder();

        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(fin), "UTF-8");
            BufferedReader br = new BufferedReader(isr);

            String line = null;
            String commentSymbol = "--";
            while ((line = br.readLine()) != null) {
                if(line.startsWith(commentSymbol)){
                    continue;
                }
                builder.append(line.trim());
                builder.append(" ");
            }

            br.close();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }

        return builder.toString();
    }

    public void close() {
        try {
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args==null || args.length < 1 || (args[0].startsWith("jdbc:mysql://")&&args.length<2)) {
            System.out.println("Usage:");
            System.out.println(" java -cp mysql-test.jar <conn-str> <sql> [param]");
            System.out.println(" java -cp mysql-test.jar <sql> [param]");
            System.out.println("\n");
            System.out.println("Example:");
            System.out.println(" java -cp mysql-test.jar \"jdbc:mysql://localhost/db?user=root&password=root&characterEncoding=utf8\" \"select now()\"");
            System.out.println("");
            return;
        }

        final String DRIVER_KEY_STRING=  "jdbc:mysql://";
        String arg0 = args[0];
        String conn_str = arg0.startsWith(DRIVER_KEY_STRING) ? arg0 : "";
        String sql = conn_str.length()==0 ? arg0 : args[1];
        if(sql==null){
            System.err.println("no sql to run !");
            return;
        }
        sql = sql.strip();
        if(sql.length()==0){
            System.err.println("no sql to run !");
            return;
        }
//        System.err.println("sql= " + sql);


        // if no connection string, get connection from mysql-test.rc
        if(conn_str.length()==0) {
            CodeSource codeSource = MySqlTest.class.getProtectionDomain().getCodeSource();
            File jarFile = null;
            try {
                jarFile = new File(codeSource.getLocation().toURI().getPath());
            } catch (URISyntaxException e) {
                e.printStackTrace();e.printStackTrace();
            }
            String jarDir = jarFile.getParentFile().getAbsolutePath();
            File file = new File(String.join(File.separator, jarDir, ".mysql-test"));
            if (!file.exists()) {
                System.out.println(file.getPath() + " not exits!");
                return;
            }
            conn_str = readFile(file).strip();
            if (conn_str.indexOf(DRIVER_KEY_STRING)>0) {
                conn_str = conn_str.substring(conn_str.indexOf(DRIVER_KEY_STRING));
            }
        }

        // get params replace in sql
        String[] params = null;
        if(args.length>2){
            params = new String[args.length-2];
            for(int i=2; i<args.length; i++){
                params[i-2] = args[i];
            }
        }

        File fin = new File(sql);
        if(fin.isFile()) {
            sql = readFile(fin);
        }
        sql = sql.strip();

        // replace params in sql
        if(params!=null){
            for (int i=1; i<=params.length; i++){
                String param = params[i-1];
                sql = sql.replace("$"+i, param);
            }
        }

        MySqlTest test = new MySqlTest(conn_str);
        sql = trim(sql, new char[]{'\"', '\'',' '}).strip();
        String lowerCaseSql = sql.toLowerCase();
//        System.err.println("lowerCaseSql= " + lowerCaseSql);
        if (lowerCaseSql.startsWith("select ") || lowerCaseSql.startsWith("show ")) {
            test.handleResult(sql);
        } else {
            test.executeUpdate(sql);
        }
    }

    private static String trim(String str, char[] trims){
        for(Character c : trims){
            if(str.startsWith(c.toString())){
                str = str.substring(1);

                if(str.endsWith(c.toString())){
                    str = str.substring(0,str.length()-1);
                }
            }
        }
        return str;
    }

    private void handleResult(String sql){
        ResultSet rs = executeQuery(sql);
        try {
            while (rs.next()) {
                ResultSetMetaData md = rs.getMetaData();
                int cols = md.getColumnCount();

                for (int i = 1; i <= cols; i++) {
                    switch (md.getColumnType(i)) {
                        case Types.BIT:
                        case Types.INTEGER:
                        case Types.TINYINT:
                        case Types.SMALLINT:
                        case Types.NUMERIC:
                            System.out.print(rs.getInt(i));
                            break;
                        case Types.BIGINT:
                            System.out.print(rs.getLong(i));
                            break;
                        case Types.DECIMAL:
                            System.out.print(rs.getBigDecimal(i));
                            break;
                        case Types.BOOLEAN:
                            System.out.print(rs.getBoolean(i));
                            break;
                        case Types.FLOAT:
                        case Types.REAL:
                            System.out.print(rs.getFloat(i));
                            break;
                        case Types.DOUBLE:
                            System.out.print(rs.getDouble(i));
                            break;
                        case Types.VARCHAR:
                        case Types.NVARCHAR:
                        case Types.CHAR:
                        case Types.NCHAR:
                        case Types.DATE:
                        case Types.TIMESTAMP: {
                            String val = rs.getString(i);
                            if(val==null){
                                System.out.print("null");
                            }else {
                                val = val.replace("\r", "");
                                val = val.replace("\n", "");

                                if(cols>1) {
                                    System.out.print("'" + val + "'");
                                }else{
                                    System.out.print(val);
                                }
                            }
                        }
                            break;
                        case Types.VARBINARY: {
                            byte[] bytes = rs.getBytes(i);
                            if(bytes!=null) {
                                String hex="";
                                for (int c = 0; c < bytes.length; c++) {
                                    hex += String.format("%02X", bytes[c]);
                                }
                                System.out.print("'" + hex + "'");
                            }else{
                                System.out.print("null");
                            }
                        }
                            break;
                        case Types.NULL:
                            System.out.print("null");
                            break;
                        case Types.LONGVARBINARY:
                            System.out.print("[IMAGE]");
                            break;
                        default:
                            System.out.print("Unknown type: " + md.getColumnType(i));
                    }

                    if(i<cols){
                        System.out.print("|");
                    }
                }

                // new line
                System.out.println("");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            close();
        }
    }
}
