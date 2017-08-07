package jatx.reflectdata.mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class MySQLClient {
    private String user;
    private String password;
    private String db;
    private Connection connection;
    private Statement statement;

    public MySQLClient(String user, String password, String db) {
        this.user = user;
        this.password = password;
        this.db = db;
    }

    public Connection connect() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        connection = java.sql.DriverManager.getConnection("jdbc:mysql://localhost/" + db + "?user=" + user + "&password=" + password);

        statement = connection.createStatement();
        return connection;
    }

    public void disconnect() throws SQLException {
        statement.close();
        connection.close();
    }

    public void createTable(Class clazz) throws ReflectMySQLException, SQLException {
        new ReflectCreateTableStatement(connection, clazz).toPreparedStatement().execute();
    }

    public void insert(Object obj) throws ReflectMySQLException, SQLException {
        new ReflectInsertStatement(connection, obj).toPreparedStatement().executeUpdate();
    }

    public List select(Class clazz) throws SQLException, ReflectMySQLException {
        String query = "SELECT * FROM `" + Table.getTableName(clazz) + "`";
        ResultSet resultSet = statement.executeQuery(query);
        List result = ResultSetExtractor.extractResultSet(resultSet, clazz);
        resultSet.close();
        return result;
    }
}
