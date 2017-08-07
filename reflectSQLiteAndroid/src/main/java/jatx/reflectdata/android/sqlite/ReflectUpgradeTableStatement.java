package jatx.reflectdata.android.sqlite;

import jatx.reflectdata.android.annotations.DoNotCreateNorInsert;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jatx on 04.08.17.
 */
public class ReflectUpgradeTableStatement {
    /*
    private Map<String,Object> defaultValueMap = new HashMap<>();

    private String _tableName;

    private Connection connection;
    private Class clazz;
    private int oldVersion;
    private int newVersion;

    public ReflectUpgradeTableStatement(Connection connection, Table table, int oldVersion, int newVersion) {
        this.connection = connection;
        this.clazz = table.getTableClass();
        _tableName = table.getTableName();
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
    }

    private List<String> generateAlterTableQueryList() {
        List<String> queryList = new ArrayList<>();

        List<Column> newColumns = new ArrayList<>();

        Field[] fields = clazz.getFields();

        for (Field field: fields) {
            int modifiers = field.getModifiers();
            if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers)
                    && !field.isAnnotationPresent(DoNotCreate.class)
                    && !field.isAnnotationPresent(DoNotCreateNorInsert.class)) {
                Column column = Column.fromField(field);
                if (column!=null && column.fromVersion > oldVersion && column.fromVersion <= newVersion) {
                    newColumns.add(column);
                }
            }
        }

        for (Column column: newColumns) {
            String addQuery = Const.STRING_ALTER_TABLE + Const.STRING_BEFORE_NAME + _tableName + Const.STRING_AFTER_NAME +
                    Const.STRING_ADD_COLUMN + column.toString() + Const.STRING_QUERY_ENDING;
            queryList.add(addQuery);
            if (column.hasDefaultValue) {
                defaultValueMap.put(addQuery, column.getDefaultValue());
            }
        }

        return queryList;
    }

    public List<PreparedStatement> toPreparedStatementList() throws ReflectMySQLException {
        try {
            List<PreparedStatement> result = new ArrayList<>();
            for (String query : generateAlterTableQueryList()) {
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                int index = 0;
                if (defaultValueMap.containsKey(query)) {
                    preparedStatement.setObject(1, defaultValueMap.get(query));
                }
                result.add(preparedStatement);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ReflectMySQLException();
        }
    }

    public List<String> toSQLStringList() throws ReflectMySQLException {
        List<String> result = new ArrayList<>();
        for (PreparedStatement preparedStatement: toPreparedStatementList()) {
            result.add(preparedStatement.toString());
        }
        return result;
    }
    */
}
