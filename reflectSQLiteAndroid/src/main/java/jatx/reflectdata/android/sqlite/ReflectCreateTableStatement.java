package jatx.reflectdata.android.sqlite;


import jatx.reflectdata.android.annotations.DoNotCreateNorInsert;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jatx on 26.07.17.
 */
public class ReflectCreateTableStatement {
    private String _tableName;

    private Class clazz;
    private int dbVersion;

    public ReflectCreateTableStatement(Table table, int dbVersion) {
        this.clazz = table.getTableClass();
        _tableName = table.getTableName();
        this.dbVersion = dbVersion;
    }

    public ReflectCreateTableStatement(Class clazz, int dbVersion) {
        this(new Table(clazz), dbVersion);
    }

    public ReflectCreateTableStatement(Table table) {
        this(table, 1);
    }

    @Deprecated
    public ReflectCreateTableStatement(Class clazz, String tableName) {
        this(new Table(clazz, tableName));
    }

    public ReflectCreateTableStatement(Class clazz) {
        this(new Table(clazz));
    }

    private String generateCreateTableQuery() throws ReflectSQLExceptions.MoreThanOnePrimaryKeyException, ReflectSQLExceptions.UnsupportedValueException {
        final String tableName = (_tableName==null || _tableName.isEmpty()) ? Table.getTableName(clazz) : _tableName;

        List<Column> columnList = new ArrayList<>();
        Field[] fields = clazz.getFields();

        for (Field field: fields) {
            int modifiers = field.getModifiers();
            if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers)
                    && !field.isAnnotationPresent(DoNotCreateNorInsert.class)) {
                Column column = Column.fromField(field);
                if (column!=null && dbVersion >= column.fromVersion) {
                    columnList.add(column);
                }
            }
        }

        //boolean ifNotExists = clazz.isAnnotationPresent(IfNotExists.class);
        //boolean ifNotExists = true;
        boolean ifNotExists = false;

        String header = Const.STRING_CREATE_TABLE + (ifNotExists?Const.STRING_IF_NOT_EXISTS:"")
                + Const.STRING_BEFORE_NAME + tableName + Const.STRING_AFTER_NAME + " (\n";

        int primaryKeyCount = 0;

        List<String> columnStringList = new ArrayList<>();

        for (Column column: columnList) {
            if (column.isPrimaryKey) {
                primaryKeyCount++;
            }
            columnStringList.add(column.asString());
        }

        if (primaryKeyCount>1) throw new ReflectSQLExceptions.MoreThanOnePrimaryKeyException();
        String footer = ")";

        return header + StringUtils.join(columnStringList, ",\n" /*", "*/) + footer + Const.STRING_QUERY_ENDING;
    }

    public String toSQL() throws ReflectSQLiteException {
        try {
            String query = generateCreateTableQuery();
            return query;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ReflectSQLiteException();
        }
    }

}
