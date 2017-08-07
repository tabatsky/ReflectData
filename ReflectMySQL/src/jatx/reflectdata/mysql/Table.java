package jatx.reflectdata.mysql;

import jatx.reflectdata.annotations.DefaultMaxLength;
import jatx.reflectdata.annotations.TableName;

public class Table {
    private Class clazz;
    private String tableName;

    public Table(Class clazz)
    {
        this(clazz, null);
    }

    public Table(Class clazz, String tableName) {
        this.clazz = clazz;
        this.tableName = ((tableName == null) || (tableName.isEmpty()) ? getTableName(clazz) : tableName);
    }

    public String getTableName() {
        return tableName;
    }

    public Class getTableClass() {
        return clazz;
    }

    public int getDefaultMaxLength() {
        if (clazz.isAnnotationPresent(DefaultMaxLength.class)) {
            DefaultMaxLength defaultMaxLength = (DefaultMaxLength)clazz.getAnnotation(DefaultMaxLength.class);
            return defaultMaxLength.value();
        }
        return 256;
    }

    public static int getDefaultMaxLength(Class clazz) {
        if (clazz.isAnnotationPresent(DefaultMaxLength.class)) {
            DefaultMaxLength defaultMaxLength = (DefaultMaxLength)clazz.getAnnotation(DefaultMaxLength.class);
            return defaultMaxLength.value();
        }
        return 256;
    }

    public static String getTableName(Class clazz) {
        String tableName = "";
        if (clazz.isAnnotationPresent(TableName.class)) {
            TableName tableNameAnnotation = (TableName)clazz.getAnnotation(TableName.class);
            tableName = tableNameAnnotation.value();
        }

        if (tableName.isEmpty()) {
            tableName = clazz.getSimpleName() + "_table";
        }
        return tableName;
    }
}
