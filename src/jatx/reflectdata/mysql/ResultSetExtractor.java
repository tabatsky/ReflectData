package jatx.reflectdata.mysql;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jatx on 14.06.17.
 */
public class ResultSetExtractor {
    public static List extractResultSet(ResultSet resultSet, Class clazz) throws SQLException, IllegalAccessException, InstantiationException {
        List result = new ArrayList<>();
        Map<Field,Column> columnMap = new HashMap<>();

        for (Field field: clazz.getFields()) {
            int modifiers = field.getModifiers();
            if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers)) {
                Column column = Column.fromField(field);
                if (column!=null) columnMap.put(field, column);
            }
        }

        while (resultSet.next()) {
            Object row = clazz.newInstance();
            for (Map.Entry<Field,Column> entry: columnMap.entrySet()) {
                Column column = entry.getValue();
                Field field = entry.getKey();
                Object value = null;
                switch (column.columnType) {
                    case BOOL:
                        value = resultSet.getBoolean(column.name);
                        break;
                    case TINYINT:
                        value = resultSet.getByte(column.name);
                        break;
                    case SMALLINT:
                        value = resultSet.getShort(column.name);
                        break;
                    case INT:
                        value = resultSet.getInt(column.name);
                        break;
                    case BIGINT:
                        value = resultSet.getLong(column.name);
                        break;
                    case FLOAT:
                        value = resultSet.getFloat(column.name);
                        break;
                    case DOUBLE:
                        value = resultSet.getDouble(column.name);
                        break;
                    case CHAR:
                        String strValue = resultSet.getString(column.name);
                        value = strValue!=null?strValue.charAt(0):null;
                        break;
                    case VARCHAR:
                        value = resultSet.getString(column.name);
                        break;
                }
                if (value!=null) field.set(row, value);
            }
            result.add(row);
        }

        return result;
    }
}
