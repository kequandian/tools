import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * class MySqlXTab
 * params: connect-string:
 * - String connStr = "jdbc:mysql://localhost/codepilot?user=root&password=root";
 */
public class MySqlXTab {

    final Logger logger = Logger.getLogger(MySqlXTab.class.getName());

    private static void usage() {
        System.out.println("Usage:");
        System.out.println(" java -jar mysqlxtab.jar <conn-str> <src-tab> WHERE [options] TO [conn-str] <dest-tab>");
        System.out.println("\n");
        System.out.println("Example:");
        System.out.println(" java -jar mysqlxtab.jar \"jdbc:mysql://localhost/codepilot?user=root&password=root\" t_user WHERE \"name='admin'\" TO t_user_his ");
        System.out.println("");
    }

    public static void main(String[] args) {
        if (args.length < 5) {
            usage();
            return;
        }

        // print start time
        long currentTime = System.currentTimeMillis();

        String srcConn = args[0];
        String srcTable = args[1];
        String WHERE = args[2];
        if (!"WHERE".equals(WHERE)) {
            usage();
            return;
        }
        String srcOptions = args[3];

        String destConn = null;
        String destTable = null;
        if ("TO".equals(srcOptions)) {
            srcOptions = null;
            destConn = args[4];
            if (args.length == 6) {
                destTable = args[5];
            } else if (args.length == 5) {
                destTable = destConn;
                destConn = null;
            }
        } else {
            destConn = args[4];
            if (args.length == 7) {
                destTable = args[6];
            } else if (args.length == 6) {
                destTable = destConn;
                destConn = null;
            }
        }


        MySqlXTab xtab = new MySqlXTab(srcConn, destConn);
        if (srcConn == destConn && srcTable.equals(destTable)) {
            // all are equals, do nothing
        } else {
            xtab.copy_table(xtab.getConn(), srcTable, srcOptions, xtab.getDestConn(), destTable);
        }
        xtab.close();

        long millis = ((System.currentTimeMillis() - currentTime));
        System.out.println("Elapsed time: " + millis);
    }


    /**
     * class MySqlXTab
     */

    Connection _conn = null;
    Connection _destConn = null;

    public MySqlXTab(String connStr, String destConnStr) {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            _conn = DriverManager.getConnection(connStr);

            if (destConnStr == null) {
                _destConn = _conn;
            } else {
                _destConn = DriverManager.getConnection(destConnStr);
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

    public Connection getConn() {
        return _conn;
    }

    private Connection getDestConn() {
        return _destConn;
    }

    public boolean copy_table(Connection src_conn, String src_table, String where_str, Connection dest_conn, String dest_table) {
        if (dest_conn == null) {
            dest_conn = src_conn;
        }

        String affectedSql = "SELECT * FROM " + src_table + (where_str == null ? "" : (" WHERE " + where_str));
        try {
            src_conn.setAutoCommit(false);
            dest_conn.setAutoCommit(false);

            ResultSet rs = execute_query(src_conn, affectedSql);
            ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();

            StringBuffer insertSqlBuilder = new StringBuffer();
            insertSqlBuilder.append("insert into " + dest_table + " values (");
            for (int i = 0; i < cols; i++) {
                if (i < cols - 1)
                    insertSqlBuilder.append("?, ");
                else
                    insertSqlBuilder.append("?)");
            }

            PreparedStatement q2 = dest_conn.prepareStatement(insertSqlBuilder.toString());

            while (rs.next()) {
                for (int i = 1; i <= cols; i++) {
                    int columnTpe = md.getColumnType(i);
                    switch (columnTpe) {
                        case Types.INTEGER:
                        case Types.TINYINT:
                        case Types.SMALLINT:
                        case Types.NUMERIC:
                        case Types.BIT:
                            q2.setInt(i, rs.getInt(i));
                            break;
                        case Types.BIGINT:
                            q2.setLong(i, rs.getLong(i));
                            break;
                        case Types.FLOAT:
                        case Types.REAL:
                            System.out.print(rs.getFloat(i));
                            break;
                        case Types.DOUBLE:
                        case Types.DECIMAL:
                            q2.setDouble(i, rs.getDouble(i));
                            break;
                        case Types.VARCHAR:
                        case Types.NVARCHAR:
                        case Types.LONGVARCHAR:
                        case Types.CHAR:
                        case Types.NCHAR:
                        case Types.DATE:
                        case Types.TIMESTAMP:
                            q2.setString(i, rs.getString(i));
                            break;
                        case Types.VARBINARY:
                        case Types.LONGVARBINARY:
                            q2.setBytes(i, rs.getBytes(i));
                            break;
                        case Types.NULL:
                            q2.setObject(i, null);
                            break;

                        default:
                            System.out.println("Unknown type: " + md.getColumnType(i));
                    }
                }
                q2.addBatch();

            }

            int[] rows = q2.executeBatch();
            if (rows == null) {
                logger.log(Level.WARNING, "Fail to insert row: " + rs.getInt(0));
            } else {
                String rowsString = rows.toString();
                logger.log(Level.INFO, String.format("Success insert %d rows: ", rows.length, rowsString));
            }
            dest_conn.commit();

        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage());
            try {
                dest_conn.rollback();
                return false;
            } catch (SQLException ex1) {
                logger.log(Level.SEVERE, ex1.getMessage());
                return false;
            }
        }
        return true;
    }

    public void print_table(Connection conn, String table_name, String where_str) {
        try {
            String sql = "select * from " + table_name + (isEmpty(where_str) ? "" : (" where " + where_str));
            logger.log(Level.INFO, sql);

            ResultSet rs = execute_query(conn, sql);
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
                        case Types.BIT:
                            System.out.print(rs.getInt(i));
                            break;
                        case Types.FLOAT:
                        case Types.REAL:
                            System.out.print(rs.getFloat(i));
                            break;
                        case Types.DOUBLE:
                        case Types.DECIMAL:
                            System.out.print(rs.getDouble(i));
                            break;
                        case Types.VARCHAR:
                        case Types.NVARCHAR:
                        case Types.CHAR:
                        case Types.NCHAR:
                        case Types.DATE:
                        case Types.TIMESTAMP: {
                            String val = rs.getString(i);
                            if (val == null) {
                                System.out.print("null");
                            } else {
                                val = val.replace("\r", "");
                                val = val.replace("\n", "");
                                System.out.print("'" + val + "'");
                            }
                        }
                        break;
                        case Types.VARBINARY: {
                            byte[] bytes = rs.getBytes(i);
                            if (bytes != null) {
                                String hex = "";
                                for (int c = 0; c < bytes.length; c++) {
                                    hex += String.format("%02X", bytes[c]);
                                }
                                System.out.print("'" + hex + "'");
                            } else {
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

                    if (i < cols) {
                        System.out.print("|");
                    }
                }
                // new line
                System.out.println("");
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, e.getMessage());
        } finally {
        }
    }

    public int empty_table(Connection conn, String table_name, String where_str) {
        String sql = "delete from " + table_name + (isEmpty(where_str) ? "" : (" where " + where_str));
        logger.log(Level.INFO, sql);
        return execute_update(conn, sql);
    }

    private ResultSet execute_query(Connection conn, String sql) {
        ResultSet rs = null;
        try {
            PreparedStatement q1 = conn.prepareStatement(sql);
            rs = q1.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, e.getMessage());
        }
        return rs;
    }

    private int execute_update(Connection conn, String sql) {
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

                    PreparedStatement q1 = conn.prepareStatement(sql);
                    affectedRows = q1.executeUpdate();
                }

                conn.commit();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, sql);
        }

        return affectedRows;
    }

    private void close() {
        if (_conn != null) {
            try {
                if (!_conn.isClosed()) {
                    _conn.close();
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage());
            }
        }

        if (_destConn != null) {
            try {
                if (!_destConn.isClosed()) {
                    _destConn.close();
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage());
            }
        }
    }

    private boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }
}
