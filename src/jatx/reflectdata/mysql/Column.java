package jatx.reflectdata.mysql;

import jatx.reflectdata.annotations.*;

import java.lang.reflect.Field;

/**
 * Created by jatx on 14.06.17.
 */
public class Column {
    public enum ColumnType {
        BOOL,
        TINYINT,
        SMALLINT,
        INT,
        BIGINT,
        FLOAT,
        DOUBLE,
        VARCHAR,
        CHAR,
        OTHER;

        @Override
        public String toString() {
            switch (this) {
                case BOOL:
                    return "BOOL";
                case TINYINT:
                    return "TINYINT";
                case SMALLINT:
                    return "SMALLINT";
                case INT:
                    return "INT";
                case BIGINT:
                    return "BIGINT";
                case FLOAT:
                    return "FLOAT";
                case DOUBLE:
                    return "DOUBLE";
                case CHAR:
                case VARCHAR:
                    return "VARCHAR";
            }

            return "OTHER";
        }
    }

    public String name;
    public ColumnType columnType;
    public boolean isAutoIncrement;
    public boolean isPrimaryKey;
    public boolean isNotNull;
    public boolean hasDefaultValue;
    public String defaultValue;
    public int maxLength = 256;

    public static Column fromField(Field field) {
        Class fieldType = field.getType();
        ColumnType columnType;
        if (fieldType == boolean.class || fieldType == Boolean.class) {
            columnType = ColumnType.BOOL;
        } else if (fieldType == byte.class || fieldType == Byte.class) {
            columnType = ColumnType.TINYINT;
        } else if (fieldType == short.class || fieldType == Short.class) {
            columnType = ColumnType.SMALLINT;
        } else if (fieldType == int.class || fieldType == Integer.class) {
            columnType = ColumnType.INT;
        } else if (fieldType == long.class || fieldType == Long.class) {
            columnType = ColumnType.BIGINT;
        } else if (fieldType == float.class || fieldType == Float.class) {
            columnType = ColumnType.FLOAT;
        } else if (fieldType == double.class || fieldType == Double.class) {
            columnType = ColumnType.DOUBLE;
        } else if (fieldType == char.class || fieldType == Character.class) {
            columnType = ColumnType.CHAR;
        } else if (fieldType==String.class) {
            columnType = ColumnType.VARCHAR;
        } else {
            return null;
        }

        Column column = new Column();
        column.columnType = columnType;
        column.name = field.getName();
        column.isAutoIncrement = field.isAnnotationPresent(AutoIncrement.class);
        column.isPrimaryKey = field.isAnnotationPresent(PrimaryKey.class);
        column.isNotNull = field.isAnnotationPresent(NotNull.class);
        column.hasDefaultValue = field.isAnnotationPresent(Default.class);
        if (column.hasDefaultValue) {
            Default defaultAnnotation = (Default)field.getAnnotation(Default.class);
            column.defaultValue = defaultAnnotation.value();
        }
        column.maxLength = 256;
        if (field.isAnnotationPresent(MaxLength.class)) {
            MaxLength maxLength = (MaxLength)field.getAnnotation(MaxLength.class);
            column.maxLength = maxLength.value();
        }
        if (field.isAnnotationPresent(ColumnName.class)) {
            ColumnName columnName = (ColumnName)field.getAnnotation(ColumnName.class);
            String name = columnName.value();
            if (!name.trim().isEmpty()) column.name = name;
        }

        return column;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append("`");
        result.append(name);
        result.append("` ");
        result.append(columnType.toString());
        if (columnType == ColumnType.CHAR) {
            result.append("(1)");
        } else if (columnType == ColumnType.VARCHAR) {
            result.append("(");
            result.append(maxLength);
            result.append(")");
        }
        if (isNotNull) result.append(" NOT NULL");
        if (hasDefaultValue) {
            result.append(" DEFAULT '");
            result.append(defaultValue.replace("'", "\\'"));
            result.append("'");
        }
        if (isAutoIncrement) result.append(" AUTO_INCREMENT");

        return result.toString();
    }

    @Override
    public int hashCode() {
        return 731*name.hashCode() + 137*columnType.hashCode() + 1331*(defaultValue==null?0:defaultValue.hashCode()) +
                16*(isAutoIncrement?1:0) /*+ 8*(isPrimaryKey?1:0)*/ + 4*(isNotNull?1:0) + 2*(hasDefaultValue?1:0)
                + 1523*maxLength;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Column && this.hashCode() == ((Column)obj).hashCode());
    }

    public boolean equalsByName(Object obj) {
        return (obj instanceof Column && this.name.equals(((Column)obj).name));
    }
}
