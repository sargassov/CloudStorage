import lombok.SneakyThrows;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DBStorage{

    private static Connection connection;
    private static Statement statement;
    private static ResultSet resultSet;


    public DBStorage() throws SQLException, ClassNotFoundException {
        setConnection();
        createdb();
        writedb();
    }

    @SneakyThrows
    public static String userIdVerify(String login, String password) throws SQLException {
        resultSet = selectAllFromUsers();

        while(resultSet.next()){
            if (resultSet.getString("login").equals(login) &&
                    resultSet.getString("password").equals(password)) {
                return resultSet.getString("login");
            }
        }

        return null;
    }

    public boolean registration(String login, String password) throws SQLException {
        resultSet = selectAllFromUsers();
        while(resultSet.next()){
            if (resultSet.getString("login").equals(login)) { return false; }
        }
        statement.execute("INSERT INTO 'users' ('login', 'password') VALUES ('" + login + "', '" + password + "')");
        return true;
    }

    private static void setConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:Cloud_storage_Database.db:authdb");
    }

    private static void createdb() throws SQLException {
        String createTableExecute = "CREATE TABLE if not exists 'users'('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'login' text, 'password' text);";
        statement = connection.createStatement();
        statement.execute(createTableExecute);
    }

    private static void writedb() {
        List<String> createUserCommands = new ArrayList<>(Arrays.asList(
                "INSERT INTO 'users' ('login', 'password') VALUES('qwe', 'qwe')",
                "INSERT INTO 'users' ('login', 'password') VALUES('asd', 'asd')",
                "INSERT INTO 'users' ('login', 'password') VALUES('zxc', 'zxc')"
        ));

        createUserCommands.forEach(s -> {
            try {
                statement.execute(s);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
    }

    @SneakyThrows
    private static ResultSet selectAllFromUsers(){
        return statement.executeQuery("SELECT * FROM users");
    }

    public static void closedb() throws SQLException {
        resultSet.close();
        statement.close();
        connection.close();
    }
}
