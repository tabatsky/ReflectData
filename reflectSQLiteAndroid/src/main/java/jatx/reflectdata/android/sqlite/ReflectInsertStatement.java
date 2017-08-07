package jatx.reflectdata.android.sqlite;

import jatx.reflectdata.android.annotations.DoNotCreateNorInsert;
import jatx.reflectdata.android.annotations.DoNotInsert;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by jatx on 26.07.17.
 */
public class ReflectInsertStatement {
    private Object object;
    private Class clazz;
    private int dbVersion;

    private String _tableName;

    private Map<String,String> functionValueMap = new HashMap<>();

    public ReflectInsertStatement(Object object, String tableName, int dbVersion) {
        this.object = object;
        this.clazz = object.getClass();
        _tableName = tableName;
        this.dbVersion = dbVersion;
    }

    public ReflectInsertStatement(Object object, int dbVersion) {
        this(object, null, dbVersion);
    }

    public ReflectInsertStatement(Object object) {
        this(object, 1);
    }

    public ReflectInsertStatement setFunctionValues(String functionValues) {
        String[] arr0 = functionValues.split(";");
        for (String str0: arr0) {
            String[] arr1 = str0.split(":");
            functionValueMap.put(arr1[0], arr1[1]);
        }
        return this;
    }

    private String generateInsertQuery() throws IllegalAccessException, ReflectSQLExceptions.NotNullColumnHasNullValueException, ReflectSQLExceptions.UnsupportedValueException {
        final String tableName = (_tableName==null || _tableName.isEmpty()) ? Table.getTableName(clazz) : _tableName;

        List<String> columnStringList = new ArrayList<>();
        List<String> valueStringList = new ArrayList<>();
        Field[] fields = clazz.getFields();

        for (Field field: fields) {
            int modifiers = field.getModifiers();
            if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers)
                    && !field.isAnnotationPresent(DoNotInsert.class)
                    && !field.isAnnotationPresent(DoNotCreateNorInsert.class)) {
                Column column = Column.fromField(field);
                if (column!=null) {
                    if (dbVersion < column.fromVersion) continue;
                    //if (column.isAutoIncrement) continue;

                    columnStringList.add(column.name);

                    if (functionValueMap.containsKey(column.name)) {
                        String valueString = functionValueMap.get(column.name);
                        valueStringList.add(valueString);
                        continue;
                    }

                    Object value = null;
                    value = field.get(object);
                    if (value==null && column.hasDefaultValue) continue;

                    if (column.isNotNull && value==null) {
                        throw new ReflectSQLExceptions.NotNullColumnHasNullValueException();
                    }

                    String valueString = Value.valueToString(value);
                    valueStringList.add(valueString);
                }
            }
        }

        String result = Const.STRING_INSERT + Const.STRING_INTO +
                Const.STRING_BEFORE_NAME + tableName + Const.STRING_AFTER_NAME +
                Const.STRING_BEFORE_VALUES + StringUtils.join(columnStringList, Const.STRING_GLUE) + Const.STRING_AFTER_VALUES +
                Const.STRING_VALUES +
                Const.STRING_BEFORE_VALUES + StringUtils.join(valueStringList, Const.STRING_GLUE) + Const.STRING_AFTER_VALUES +
                Const.STRING_QUERY_ENDING;

        System.out.println(result);
        return result;
    }

    public String toSQL() throws ReflectSQLiteException {
        try {
            String query = generateInsertQuery();
            return query;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ReflectSQLiteException();
        }
    }

}
