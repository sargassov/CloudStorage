import java.sql.*;

public class DBStorage {

    private static Connection connection;
    private static String SQL_DRIVER = "org.sqlite.JDBC";
    private static String DB_DST = "jdbc:sqlite:Cloud_storage_Database.db";
    private static String SQL_LOGIN_PASSWORD_REQUEST =
            "SELECT id FROM main where login = '%s' and password = '%s'";
    private static Statement statement;
    private static ResultSet resultSet;

    static void connect() throws SQLException {
        try {

            Class.forName(SQL_DRIVER);
            connection = DriverManager.getConnection(DB_DST);
            statement = connection.createStatement();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static String getIdByLoginAndPass(String login, String pass) throws SQLException {

        String sql = String.format(SQL_LOGIN_PASSWORD_REQUEST, login, pass);
        resultSet = statement.executeQuery(sql);

        if (resultSet.next()) {
            return resultSet.getString(1);
        }
        return null;
    }
}
