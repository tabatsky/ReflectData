package jatx.reflectdata.android.sqlite;

import android.database.DatabaseUtils;

import jatx.debug.Log;

/**
 * Created by jatx on 07.08.17.
 */

public class Value {
    public static String valueToString(Object value) throws ReflectSQLExceptions.UnsupportedValueException {
        if (value==null) {
            return Const.STRING_NULL;
        } else if (value instanceof Boolean) {
            return (Boolean) value ? "1" : "0";
        } else if (value instanceof Character) {
            return DatabaseUtils.sqlEscapeString(String.valueOf(value));
        } else if (value instanceof Number) {
            return String.valueOf(value);
        } else if (value instanceof String) {
            return DatabaseUtils.sqlEscapeString((String)value);
        } else {
            Log.e("value class", value.getClass().getCanonicalName());
            throw new ReflectSQLExceptions.UnsupportedValueException();
        }
    }
}
