import java.sql.*;
     
public class MssqlTest {    
    //String connStr = "jdbc:sqlserver://localhost:1433;DatabaseName=db;selectMethod=cursor";
    Connection c = null;    
    Statement conn;    
    ResultSet rs = null;

    public MssqlTest(String connStr, String user, String passwd) {
       try {
           Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();    
           c = DriverManager.getConnection(connStr,user,passwd);
           conn = c.createStatement();    
       } catch (Exception e) {    
           e.printStackTrace();    
       }
    }

    public boolean executeUpdate(String sql) {    
       try {    
           conn.executeUpdate(sql);    
           return true;    
       } catch (SQLException e) {    
           e.printStackTrace();    
           return false;    
       }    
    }    
     
    public ResultSet executeQuery(String sql) {    
       rs = null;    
       try {    
           rs = conn.executeQuery(sql);    
       } catch (SQLException e) {    
           e.printStackTrace();    
       }    
       return rs;
    }    
     
    public void close() {    
       try {    
           conn.close();    
           c.close();    
       } catch (Exception e) {    
           e.printStackTrace();    
       }    
    }
     
    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Usage:");
            System.out.println(" java -cp mssql-test.jar <conn-str> <user> <passw> <sql>");
            System.out.println("\n");
            System.out.println("Example:");
            System.out.println(" java -cp mssql-test.jar \"jdbc:sqlserver://114.215.142.242:1433;DatabaseName=ServerDTUInfo\" web Hngyqbbzxxzx090500~~~ \"select * from UsersInfo\"");
            System.out.println("");
            return;
        }

        String conn_str = args[0];
        String user = args[1];
        String pw = args[2];
        String sql = args[3];

       ResultSet rs;    
       MssqlTest conn = new MssqlTest(conn_str, user, pw);
       rs = conn.executeQuery(sql);
       try {
           while (rs.next()) {
               ResultSetMetaData md = rs.getMetaData();
               int cols = md.getColumnCount();

               for (int i = 1; i <= cols; i++) {
                   switch (md.getColumnType(i)) {
                       case Types.BIT:
                       case Types.INTEGER:
                       case Types.TINYINT:
                       case Types.BIGINT:
                       case Types.SMALLINT:
                       case Types.NUMERIC:
                       case Types.DECIMAL:
                           System.out.print(rs.getInt(i));
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
                       case Types.TIME:
                       case Types.LONGVARCHAR:
                       case Types.BINARY:
                       case Types.TIMESTAMP: {
                           String val = rs.getString(i);
                           if(val==null){
                               System.out.print("null");
                           }else {
                               val.replace("\r", "");
                               val.replace("\n", "");
                               System.out.print("'"+val+"'");
                           }
                       }
                           break;
                       case Types.NULL:
                           System.out.print("null");
                           break;
                       case Types.LONGVARBINARY:
                           System.out.print("[IMAGE]");
                           break;
                       case Types.VARBINARY:
                           System.out.print("[VARBINARY]");
                           break;
                       default:
                           System.out.print("[Unknown type: " + md.getColumnType(i));
                   }
                   if(i<cols){
                       System.out.print("|");
                   }
               }
               System.out.println("");
           }

       } catch (Exception e) {    
           e.printStackTrace();    
       }    
    }    
}    
     
