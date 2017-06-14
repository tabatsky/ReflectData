package jatx.reflectdata.mysql;

import java.sql.*;
import java.util.List;

/**
 * Created by jatx on 14.06.17.
 */
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

    public void connect() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        connection = DriverManager
                .getConnection("jdbc:mysql://localhost/" + db +
                        "?user=" + user + "&password=" + password);
        statement = connection.createStatement();
    }

    public void disconnect() throws SQLException {
        statement.close();
        connection.close();
    }

    public void createTable(Class clazz) throws MySQLQueryGenerator.MoreThanOnePrimaryKeyException, SQLException {
        String query = MySQLQueryGenerator.generateCreateTableQuery(clazz);
        statement.execute(query);
    }

    public void alterTable(Class oldClass, Class newClass) throws MySQLQueryGenerator.MoreThanOnePrimaryKeyException, MySQLQueryGenerator.PrimaryKeyMismatchException, SQLException {
        List<String> queryList = MySQLQueryGenerator.generateAlterTableQueries(oldClass, newClass);
        for (String query: queryList) {
            statement.execute(query);
        }
    }

    public void insert(Object obj) throws IllegalAccessException, MySQLQueryGenerator.NotNullColumnHasNullValueException, MySQLQueryGenerator.BothInsertIgnoreAndReplaceSet, SQLException {
        String query = MySQLQueryGenerator.generateInsertQuery(obj);
        statement.executeUpdate(query);
    }

    public void update(Object obj) throws IllegalAccessException, MySQLQueryGenerator.NoPrimaryKeyException, MySQLQueryGenerator.NotNullColumnHasNullValueException, MySQLQueryGenerator.BothInsertIgnoreAndReplaceSet, MySQLQueryGenerator.PrimaryKeyNullValueException, MySQLQueryGenerator.MoreThanOnePrimaryKeyException, SQLException {
        String query = MySQLQueryGenerator.generateUpdateQuery(obj);
        statement.executeUpdate(query);
    }

    public List select(Class clazz) throws SQLException, InstantiationException, IllegalAccessException {
        String query = "SELECT * FROM `" + MySQLQueryGenerator.getTableName(clazz) + "`";
        ResultSet resultSet = statement.executeQuery(query);
        List result = ResultSetExtractor.extractResultSet(resultSet, clazz);
        resultSet.close();
        return result;
    }
}
