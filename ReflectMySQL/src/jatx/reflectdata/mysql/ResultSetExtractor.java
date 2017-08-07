package jatx.reflectdata.mysql;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ResultSetExtractor {
    public static List extractResultSet(ResultSet resultSet, Class clazz) throws ReflectMySQLException
    {
        return extractResultSet(resultSet, clazz, "#");
    }

    public static List extractResultSet(ResultSet resultSet, Class clazz, String configVariant) throws ReflectMySQLException {
        try {
            List result = new ArrayList();
            Map<Field, Column> columnMap = new HashMap();

            for (Field field : clazz.getFields()) {
                int modifiers = field.getModifiers();
                if ((Modifier.isPublic(modifiers)) && (!Modifier.isStatic(modifiers))) {
                    Column column = Column.fromField(field, configVariant);
                    if (column != null) { columnMap.put(field, column);
                    }
                }
            }
            while (resultSet.next()) {
                Object row = clazz.newInstance();
                for (Map.Entry<Field, Column> entry : columnMap.entrySet()) {
                    Column column = (Column)entry.getValue();
                    Field field = (Field)entry.getKey();
                    Object value = null;
                    switch (column.columnType) {
                        case BOOL:
                            value = Boolean.valueOf(resultSet.getBoolean(column.name));
                            break;
                        case TINYINT:
                            value = Byte.valueOf(resultSet.getByte(column.name));
                            break;
                        case SMALLINT:
                            value = Short.valueOf(resultSet.getShort(column.name));
                            break;
                        case INT:
                            value = Integer.valueOf(resultSet.getInt(column.name));
                            break;
                        case BIGINT:
                            value = Long.valueOf(resultSet.getLong(column.name));
                            break;
                        case FLOAT:
                            value = Float.valueOf(resultSet.getFloat(column.name));
                            break;
                        case DOUBLE:
                            value = Double.valueOf(resultSet.getDouble(column.name));
                            break;
                        case CHAR:
                            String strValue = resultSet.getString(column.name);
                            value = strValue != null ? Character.valueOf(strValue.charAt(0)) : null;
                            break;
                        case VARCHAR:
                        case TEXT:
                            value = resultSet.getString(column.name);
                    }

                    if (value != null) field.set(row, value);
                }
                result.add(row);
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ReflectMySQLException();
        }
    }
}
