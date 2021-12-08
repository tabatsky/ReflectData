package jatx.reflectdata.mysql;

import jatx.reflectdata.annotations.DoNotCreateNorInsert;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class ReflectCreateTableStatement {
    private List<Object> defaultValueList = new ArrayList();

    private String _tableName;
    private Connection connection;
    private Class clazz;
    private int dbVersion;
    private boolean ifNotExists;

    public ReflectCreateTableStatement(Connection connection, Class clazz) {
        this.connection = connection;
        this.clazz = clazz;
        _tableName = Table.getTableName(clazz);
        this.dbVersion = 0;
        ifNotExists = true;
    }

    public ReflectCreateTableStatement setTableName(String tableName) {
        _tableName = tableName;
        return this;
    }

    public ReflectCreateTableStatement setDBVersion(int dbVersion) {
        this.dbVersion = dbVersion;
        return this;
    }

    public ReflectCreateTableStatement setIfNotExists(boolean ifNotExists) {
        this.ifNotExists = ifNotExists;
        return this;
    }

    private String generateCreateTableQuery() throws ReflectSQLExceptions.MoreThanOnePrimaryKeyException {
        String tableName = (_tableName == null) || (_tableName.isEmpty()) ? Table.getTableName(clazz) : _tableName;

        List<Column> columnList = new ArrayList();
        Field[] fields = clazz.getFields();

        for (Field field : fields) {
            int modifiers = field.getModifiers();
            if ((Modifier.isPublic(modifiers)) && (!Modifier.isStatic(modifiers)) && (!field.isAnnotationPresent(DoNotCreateNorInsert.class))) {
                Column column = Column.fromField(field);
                if ((column != null) && (dbVersion >= column.fromVersion)) {
                    columnList.add(column);
                    if (column.hasDefaultValue) {
                        defaultValueList.add(column.getDefaultValue());
                    }
                }
            }
        }

        String header = Const.STRING_CREATE_TABLE + (ifNotExists ? Const.STRING_IF_NOT_EXISTS : "") +
                Const.STRING_BEFORE_NAME + tableName + Const.STRING_AFTER_NAME + " (\n";

        int primaryKeyCount = 0;
        String primaryKeyName = "";

        List<String> columnStringList = new ArrayList();

        for (Column column : columnList) {
            if ((column.isPrimaryKey) || (column.isAutoIncrement)) {
                primaryKeyCount++;
                primaryKeyName = column.name;
            }
            columnStringList.add(column.toString());
        }

        if (primaryKeyCount > 1) throw new ReflectSQLExceptions.MoreThanOnePrimaryKeyException();
        String footer = "";
        if (primaryKeyCount == 1) {
            footer = Const.STRING_PRIMARY_KEY + Const.STRING_BEFORE_NAME + primaryKeyName + Const.STRING_AFTER_NAME + "))";
        } else {
            footer = ")";
        }

        return header + StringUtils.join(columnStringList, ",\n") + footer + Const.STRING_QUERY_ENDING;
    }

    public PreparedStatement toPreparedStatement() throws ReflectMySQLException {
        try {
            PreparedStatement preparedStatement = null;
            preparedStatement = connection.prepareStatement(generateCreateTableQuery());
            int index = 0;
            for (Object defValue : defaultValueList) {
                preparedStatement.setObject(++index, defValue);
            }
            return preparedStatement;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ReflectMySQLException();
        }
    }

    public String toSQLString() throws ReflectMySQLException {
        return toPreparedStatement().toString();
    }
}
