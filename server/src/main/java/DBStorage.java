import java.sql.*;
import java.util.List;

public class DBStorage{

    public static Connection connection;
    public static Statement statement;
    public static ResultSet resultSet;
    private static final String CREATE_USER_QWE = "INSERT INTO 'users' ('nickname', 'login', 'password') VALUES('qwe', 'qwe', 'qwe')";
    private static final String CREATE_USER_ASD = "INSERT INTO 'users' ('nickname', 'login', 'password') VALUES('asd', 'asd', 'asd')";
    private static final String CREATE_USER_ZXC = "INSERT INTO 'users' ('nickname', 'login', 'password') VALUES('zxc', 'zxc', 'zxc')";
    private static final String CREATE_TABLE_EXECUTE = "CREATE TABLE if not exists 'users'" +
            "('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'nickname' text, 'login' text, 'password' text);";
    private List<UserData> users;

    private class UserData {
        String login;
        String password;
        String nickname;

        public UserData(String login, String password, String nickname) {
            this.login = login;
            this.password = password;
            this.nickname = nickname;
        }
    }

    public DBStorage() throws SQLException, ClassNotFoundException {
        setConnection();
        createdb();
        writedb();
    }


    public static String getNicknameByLoginAndPassword(String login, String password) throws SQLException {
        resultSet = statement.executeQuery("SELECT * FROM users");
        String dbNickname;
        while(resultSet.next()){
            if (resultSet.getString("login").equals(login) &&
                    resultSet.getString("password").equals(password)) {
                return dbNickname = resultSet.getString("nickname");
            }
        }

        return null;
    }

    public boolean registration(String login, String password, String nickname) throws SQLException {
        resultSet = statement.executeQuery("SELECT * FROM users");
        while(resultSet.next()){
            if (resultSet.getString("login").equals(login) ||
                    resultSet.getString("nickname").equals(nickname)) { return false;
            }
            statement.execute("INSERT INTO 'users' ('nickname', 'login', 'password') VALUES ('" + nickname + "', '"
                    + login + "', '" + password + "')");
        }

        return true;
    }

    public static void setConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:Cloud_storage_Database.db:authdb");
    }

    public static void createdb() throws SQLException {
        statement = connection.createStatement();
        statement.execute(CREATE_TABLE_EXECUTE);
    }

    public static void writedb() throws SQLException {
        statement.execute(CREATE_USER_QWE);
        statement.execute(CREATE_USER_ASD);
        statement.execute(CREATE_USER_ZXC);
    }

    public static void closedb() throws SQLException {
        resultSet.close();
        statement.close();
        connection.close();
    }
}
