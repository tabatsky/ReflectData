package jatx.reflectdata.mysql;

import jatx.reflectdata.annotations.*;

import java.lang.reflect.Field;

public class Column {
    public String name;
    public ColumnType columnType;
    public boolean isAutoIncrement;
    public boolean isPrimaryKey;
    public boolean isNotNull;
    public boolean hasDefaultValue;
    public String defaultValue;

    private Column() {}

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
        TEXT,
        OTHER;

        public String toString() {
            switch (this) {
                case BOOL:
                    return Const.STRING_BOOL;
                case TINYINT:
                    return Const.STRING_TINYINT;
                case SMALLINT:
                    return Const.STRING_SMALLINT;
                case INT:
                    return Const.STRING_INT;
                case BIGINT:
                    return Const.STRING_BIGINT;
                case FLOAT:
                    return Const.STRING_FLOAT;
                case DOUBLE:
                    return Const.STRING_DOUBLE;
                case CHAR:
                case VARCHAR:
                    return Const.STRING_VARCHAR;
                case TEXT:
                    return Const.STRING_TEXT;
            }

            return "OTHER";
        }
    }

    public int maxLength = 256;
    public int fromVersion = 0;

    public static Column fromField(Field field) {
    return fromField(field, "#");
  }

    public static Column fromField(Field field, String configVariant) {
        Class fieldType = field.getType();
        ColumnType columnType;
        if ((fieldType == Boolean.TYPE) || (fieldType == Boolean.class)) {
            columnType = ColumnType.BOOL;
        } else if ((fieldType == Byte.TYPE) || (fieldType == Byte.class)) {
            columnType = ColumnType.TINYINT;
        } else if ((fieldType == Short.TYPE) || (fieldType == Short.class)) {
            columnType = ColumnType.SMALLINT;
        } else if ((fieldType == Integer.TYPE) || (fieldType == Integer.class)) {
            columnType = ColumnType.INT;
        } else if ((fieldType == Long.TYPE) || (fieldType == Long.class)) {
            columnType = ColumnType.BIGINT;
        } else if ((fieldType == Float.TYPE) || (fieldType == Float.class)) {
            columnType = ColumnType.FLOAT;
        } else if ((fieldType == Double.TYPE) || (fieldType == Double.class)) {
            columnType = ColumnType.DOUBLE;
        } else if ((fieldType == Character.TYPE) || (fieldType == Character.class)) {
            columnType = ColumnType.CHAR;
        } else if (fieldType == String.class) {
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
            Default defaultAnnotation = (jatx.reflectdata.annotations.Default)field.getAnnotation(jatx.reflectdata.annotations.Default.class);
            column.defaultValue = defaultAnnotation.value();
        }
        column.maxLength = Table.getDefaultMaxLength(field.getDeclaringClass());
        if (field.isAnnotationPresent(MaxLength.class)) {
            MaxLength maxLength = (MaxLength)field.getAnnotation(MaxLength.class);
            column.maxLength = maxLength.value();
        }
        column.fromVersion = 0;
        if (field.isAnnotationPresent(FromVersion.class)) {
            FromVersion fromVersion = (FromVersion)field.getAnnotation(FromVersion.class);
            column.fromVersion = fromVersion.value();
        }
        if ((columnType == ColumnType.VARCHAR) && (column.maxLength == Integer.MAX_VALUE)) {
            column.columnType = ColumnType.TEXT;
        }
        if (field.isAnnotationPresent(ColumnName.class)) {
            ColumnName columnName = (ColumnName)field.getAnnotation(ColumnName.class);
            String name = columnName.value();
            if (!name.trim().isEmpty()) {
                column.name = name;
            }
        }
        if (field.isAnnotationPresent(ResultSetConfig.class)) {
            if (configVariant == null) return null;
            ResultSetConfig resultSetConfig = (ResultSetConfig)field.getAnnotation(ResultSetConfig.class);
            String configStr = resultSetConfig.value();
            String[] configArr = configStr.split(";");
            boolean variantFound = false;
            for (String config : configArr) {
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
            if (column.name.equals("#")) {
                return null;
            }
        }
        return column;
    }

    public Object getDefaultValue() {
        if (!hasDefaultValue) return null;
        switch (columnType) {
            case BOOL:
                return Boolean.valueOf(Boolean.parseBoolean(defaultValue));
            case TINYINT:
                return Byte.valueOf(Byte.parseByte(defaultValue));
            case SMALLINT:
                return Short.valueOf(Short.parseShort(defaultValue));
            case INT:
                return Integer.valueOf(Integer.parseInt(defaultValue));
            case BIGINT:
                return Long.valueOf(Long.parseLong(defaultValue));
            case FLOAT:
                return Float.valueOf(Float.parseFloat(defaultValue));
            case DOUBLE:
                return Double.valueOf(Double.parseDouble(defaultValue));
            case CHAR:
            case VARCHAR:
            case TEXT:
                return defaultValue;
        }
        return null;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append("`");
        result.append(name);
        result.append("`");
        result.append(columnType.toString());
        if (columnType == ColumnType.CHAR) {
            result.append("(1)");
        } else if (columnType == ColumnType.VARCHAR) {
            result.append("(");
            result.append(maxLength);
            result.append(")");
        }
        if (isNotNull) { result.append(Const.STRING_NOT_NULL);
        }

        if (hasDefaultValue) {
            result.append(Const.STRING_DEFAULT);
        }
        if (isAutoIncrement) { result.append(Const.STRING_AUTO_INCREMENT);
        }
        return result.toString();
    }

    public int hashCode() {
        return 731 * name.hashCode() + 137 * columnType.hashCode() + 1331 * (defaultValue == null ? 0 : defaultValue.hashCode()) + 16 * (isAutoIncrement ? 1 : 0) + 4 * (isNotNull ? 1 : 0) + 2 * (hasDefaultValue ? 1 : 0) + 1523 * maxLength;
    }

    public boolean equals(Object obj)
    {
        return ((obj instanceof Column)) && (hashCode() == ((Column)obj).hashCode());
    }

    public boolean equalsByName(Object obj) {
        return ((obj instanceof Column)) && (name.equals(name));
    }
}
