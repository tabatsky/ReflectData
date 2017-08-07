package jatx.reflectdata.android.sqlite;

import jatx.reflectdata.android.annotations.TableName;

/**
 * Created by jatx on 26.07.17.
 */
public class Table {
    private Class clazz;
    private String tableName;

    public Table(Class clazz) {
        this(clazz, null);
    }

    public Table(Class clazz, String tableName) {
        this.clazz = clazz;
        this.tableName = (tableName == null || tableName.isEmpty()) ? getTableName(clazz) : tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public Class getTableClass() {
        return clazz;
    }

    public static String getTableName(Class clazz) {
        String tableName = "";
        if (clazz.isAnnotationPresent(TableName.class)) {
            TableName tableNameAnnotation = (TableName)clazz.getAnnotation(TableName.class);
            tableName = tableNameAnnotation.value();
        }

        if (tableName.isEmpty()) tableName = clazz.getSimpleName() + "_table";

        return tableName;
    }
}
