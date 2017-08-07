package jatx.reflectdata.android.sqlite;

import android.database.DatabaseUtils;

import jatx.debug.Log;
import jatx.reflectdata.android.annotations.*;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by jatx on 14.06.17.
 */
public class Column {
    public enum ColumnType {
        BOOL,
        BYTE,
        SHORT,
        INT,
        LONG,
        FLOAT,
        DOUBLE,
        CHAR,
        STRING,
        OTHER;

        @Override
        public String toString() {
            switch (this) {
                case BOOL:
                case BYTE:
                case SHORT:
                case INT:
                case LONG:
                    return Const.STRING_INTEGER;
                case FLOAT:
                case DOUBLE:
                    return Const.STRING_REAL;
                case CHAR:
                case STRING:
                    return Const.STRING_TEXT;
            }

            return "OTHER";
        }
    }

    public String name;
    public ColumnType columnType;
    public boolean isAutoIncrement;
    public boolean isPrimaryKey;
    public boolean isUnique;
    public boolean isNotNull;
    public boolean hasDefaultValue;
    public String defaultValue;
    public int fromVersion = 0;
    public boolean onConflictReplace;
    public boolean onConflictIgnore;

    public static Column fromField(Field field) {
        return fromField(field, "#");
    }

    public static Column fromField(Field field, String configVariant) {
        Class fieldType = field.getType();
        ColumnType columnType;
        if (fieldType == boolean.class || fieldType == Boolean.class) {
            columnType = ColumnType.BOOL;
        } else if (fieldType == byte.class || fieldType == Byte.class) {
            columnType = ColumnType.BYTE;
        } else if (fieldType == short.class || fieldType == Short.class) {
            columnType = ColumnType.SHORT;
        } else if (fieldType == int.class || fieldType == Integer.class) {
            columnType = ColumnType.INT;
        } else if (fieldType == long.class || fieldType == Long.class) {
            columnType = ColumnType.LONG;
        } else if (fieldType == float.class || fieldType == Float.class) {
            columnType = ColumnType.FLOAT;
        } else if (fieldType == double.class || fieldType == Double.class) {
            columnType = ColumnType.DOUBLE;
        } else if (fieldType == char.class || fieldType == Character.class) {
            columnType = ColumnType.CHAR;
        } else if (fieldType==String.class) {
            columnType = ColumnType.STRING;
        } else {
            return null;
        }

        Column column = new Column();
        column.columnType = columnType;
        column.name = field.getName();
        //column.isAutoIncrement = field.isAnnotationPresent(AutoIncrement.class);
        column.isAutoIncrement = false;
        column.isPrimaryKey = field.isAnnotationPresent(PrimaryKey.class);
        column.isUnique = field.isAnnotationPresent(Unique.class);
        column.isNotNull = field.isAnnotationPresent(NotNull.class);
        column.onConflictIgnore = field.isAnnotationPresent(OnConflictIgnore.class);
        column.onConflictReplace = field.isAnnotationPresent(OnConflictReplace.class);
        if (column.onConflictReplace && column.onConflictIgnore) {
            //throw new ReflectSQLExceptions.BothIgnoreAndReplaceSet();
        }
        column.hasDefaultValue = field.isAnnotationPresent(Default.class);
        if (column.hasDefaultValue) {
            Default defaultAnnotation = (Default)field.getAnnotation(Default.class);
            column.defaultValue = defaultAnnotation.value();
        }
        column.fromVersion = 0;
        if (field.isAnnotationPresent(FromVersion.class)) {
            FromVersion fromVersion = (FromVersion) field.getAnnotation(FromVersion.class);
            column.fromVersion = fromVersion.value();
        }
        if (field.isAnnotationPresent(ColumnName.class)) {
            ColumnName columnName = (ColumnName)field.getAnnotation(ColumnName.class);
            String name = columnName.value();
            if (!name.trim().isEmpty()) column.name = name;
        }

        if (field.isAnnotationPresent(CursorConfig.class)) {
            if (configVariant == null) return null;
            CursorConfig cursorConfig = (CursorConfig) field.getAnnotation(CursorConfig.class);
            String configStr = cursorConfig.value();
            String[] configArr = configStr.split(";");
            boolean variantFound = false;
            for (String config: configArr) {
                String[] mapping = config.split(":");
                String key = mapping[0].trim();
                String value = mapping[1].trim();
                if (configVariant.equals(key)) {
                    column.name = value;
                    variantFound = true;
                    break;
                }
            }
            if (!variantFound) return null;
            if (column.name.equals("#")) return null;
        }

        return column;
    }

    public Object getDefaultValue() {
        if (!hasDefaultValue) return null;
        switch (columnType) {
            case BOOL:
                return Boolean.parseBoolean(defaultValue);
            case BYTE:
                return Byte.parseByte(defaultValue);
            case SHORT:
                return Short.parseShort(defaultValue);
            case INT:
                return Integer.parseInt(defaultValue);
            case LONG:
                return Long.parseLong(defaultValue);
            case FLOAT:
                return Float.parseFloat(defaultValue);
            case DOUBLE:
                return Double.parseDouble(defaultValue);
            case CHAR:
                return defaultValue.charAt(0);
            case STRING:
                return defaultValue;
        }
        return null;
    }

    public String getDefaultValueAsString() throws ReflectSQLExceptions.UnsupportedValueException {
        Object value = getDefaultValue();
        return Value.valueToString(value);
    }

    public String asString() throws ReflectSQLExceptions.UnsupportedValueException {
        StringBuilder result = new StringBuilder();

        result.append(Const.STRING_BEFORE_NAME);
        result.append(name);
        result.append(Const.STRING_AFTER_NAME);
        result.append(columnType.toString());
        if (isPrimaryKey) {
            result.append(Const.STRING_PRIMARY_KEY);
            if (onConflictIgnore) {
                result.append(Const.STRING_ON_CONFLICT_IGNORE);
            } else if (onConflictReplace) {
                result.append(Const.STRING_ON_CONFLICT_REPLACE);
            }
        } else if (isUnique) {
            result.append(Const.STRING_UNIQUE);
            if (onConflictIgnore) {
                result.append(Const.STRING_ON_CONFLICT_IGNORE);
            } else if (onConflictReplace) {
                result.append(Const.STRING_ON_CONFLICT_REPLACE);
            }
        } else if (isAutoIncrement) {
            result.append(Const.STRING_AUTO_INCREMENT);
        } else if (isNotNull) {
            result.append(Const.STRING_NOT_NULL);
        } else if (hasDefaultValue) {
            result.append(Const.STRING_DEFAULT.replace("?", getDefaultValueAsString()));
        }

        return result.toString();
    }

    @Override
    public int hashCode() {
        return 731*name.hashCode() + 137*columnType.hashCode() + 1331*(defaultValue==null?0:defaultValue.hashCode()) +
                16*(isAutoIncrement?1:0) /*+ 8*(isPrimaryKey?1:0)*/ + 4*(isNotNull?1:0) + 2*(hasDefaultValue?1:0);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Column && this.hashCode() == ((Column)obj).hashCode());
    }

    public boolean equalsByName(Object obj) {
        return (obj instanceof Column && this.name.equals(((Column)obj).name));
    }

    public static String wrapName(String name) {
        return "{" + name + "}";
    }
}
