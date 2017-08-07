package jatx.reflectdata.android.sqlite;

import android.database.Cursor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jatx.debug.Log;

/**
 * Created by jatx on 14.06.17.
 */
public class CursorExtractor {
    public static List extractCursor(Cursor cursor, Class clazz) throws ReflectSQLiteException {
        return extractCursor(cursor, clazz, "#");
    }

    public static List extractCursor(Cursor cursor, Class clazz, String configVariant) throws ReflectSQLiteException {
        try {
            List result = new ArrayList<>();
            Map<Field, Column> columnMap = new HashMap<>();

            for (Field field : clazz.getFields()) {
                int modifiers = field.getModifiers();
                if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers)) {
                    Column column = Column.fromField(field, configVariant);
                    if (column != null) columnMap.put(field, column);
                }
            }

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Object row = clazz.newInstance();
                for (Map.Entry<Field, Column> entry : columnMap.entrySet()) {
                    Column column = entry.getValue();
                    Field field = entry.getKey();
                    Object value = null;
                    if (!cursor.isNull(cursor.getColumnIndex(column.name))) {
                        switch (column.columnType) {
                            case BOOL:
                                value = (cursor.getShort(cursor.getColumnIndex(column.name)) == 1);
                                break;
                            case BYTE:
                                value = (byte) cursor.getShort(cursor.getColumnIndex(column.name));
                                break;
                            case SHORT:
                                value = cursor.getShort(cursor.getColumnIndex(column.name));
                                break;
                            case INT:
                                value = cursor.getInt(cursor.getColumnIndex(column.name));
                                break;
                            case LONG:
                                value = cursor.getLong(cursor.getColumnIndex(column.name));
                                break;
                            case FLOAT:
                                value = cursor.getFloat(cursor.getColumnIndex(column.name));
                                break;
                            case DOUBLE:
                                value = cursor.getDouble(cursor.getColumnIndex(column.name));
                                break;
                            case CHAR:
                                String strValue = cursor.getString(cursor.getColumnIndex(column.name));
                                value = strValue != null ? strValue.charAt(0) : null;
                                break;
                            case STRING:
                                value = cursor.getString(cursor.getColumnIndex(column.name));
                                break;
                        }
                    } else {
                        Log.e(column.name, "null");
                    }
                    //if (value != null) field.set(row, value);
                    field.set(row, value);
                }
                result.add(row);
                cursor.moveToNext();
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ReflectSQLiteException();
        }
    }

}
