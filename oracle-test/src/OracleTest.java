import java.sql.*;
     
public class OracleTest {    
    String connStr = "jdbc:oracle:thin:@192.168.1.247:1521/topprod";
    String theUser = "ssww";    
    String thePw = "ssww";

    Connection c = null;    
    Statement conn;    
    ResultSet rs = null;

    public OracleTest(String connStr, String user, String pw) {
       try {    
           Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();    
           c = DriverManager.getConnection(connStr, user, pw);
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
            System.out.println(" java -cp oracle-test.jar <conn-str> <user> <password> <sql>");
            System.out.println("\n");
            System.out.println("Example:");
            System.out.println(" java -cp oracle-test.jar \"jdbc:oracle:thin:@192.168.1.247:1521/topprod\" ssww ssww \"select * from t_user\"");
            System.out.println("");
            return;
        }

        String conn_str = args[0];
        String user = args[1];
        String pw = args[2];
        String sql = args[3];

       ResultSet rs;    
       OracleTest conn = new OracleTest(conn_str, user, pw);
       rs = conn.executeQuery(sql);
       try {
           while (rs.next()) {
               ResultSetMetaData md = rs.getMetaData();
               int cols = md.getColumnCount();

               for (int i = 1; i <= cols; i++) {
                   switch (md.getColumnType(i)) {
                       case Types.INTEGER:
                       case Types.TINYINT:
                       case Types.BIGINT:
                       case Types.SMALLINT:
                       case Types.NUMERIC:
                           System.out.print(rs.getInt(i));
                           break;
                       case Types.VARCHAR:
                       case Types.NVARCHAR:
                       case Types.CHAR:
                       case Types.NCHAR:
                       case Types.DATE:
                       case Types.TIMESTAMP:
                           System.out.print(rs.getString(i));
                           break;
                       default:
                           System.out.println("Unknown type: " + md.getColumnType(i));
                   }
                   System.out.print("|");
               }

               System.out.println("");
           }    
       } catch (Exception e) {    
           e.printStackTrace();    
       }    
    }    
}    
     
