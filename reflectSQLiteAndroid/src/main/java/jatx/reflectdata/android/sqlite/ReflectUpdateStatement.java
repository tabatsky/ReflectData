package jatx.reflectdata.android.sqlite;

import jatx.reflectdata.android.annotations.DoNotCreateNorInsert;
import jatx.reflectdata.android.annotations.DoNotInsert;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by jatx on 06.08.17.
 */
public class ReflectUpdateStatement {
    private Object object;
    private Class clazz;
    private String _tableName;

    private Set<String> updateColumnNameSet = new HashSet<>();
    private String whereExpr;
    private Object[] whereValues;

    public ReflectUpdateStatement(Object object, String[] updateColumnNames, String whereExpr, Object[] whereValues, String tableName) {
        this.object = object;
        this.clazz = object.getClass();
        _tableName = tableName;

        for (String name: updateColumnNames) {
            updateColumnNameSet.add(name);
        }

        this.whereExpr = whereExpr;
        this.whereValues = whereValues;
    }


    public ReflectUpdateStatement(Object object, String[] updateColumnNames, String whereExpr, Object[] whereValues) {
        this(object, updateColumnNames, whereExpr, whereValues, null);
    }

    private String generateUpdateQuery() throws IllegalAccessException, ReflectSQLExceptions.NotNullColumnHasNullValueException, ReflectSQLExceptions.UnsupportedValueException {
        final String tableName = (_tableName==null || _tableName.isEmpty()) ? Table.getTableName(clazz) : _tableName;

        List<String> setValueStringList = new ArrayList<>();
        Field[] fields = clazz.getFields();

        for (Field field: fields) {
            int modifiers = field.getModifiers();
            if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers)
                    && !field.isAnnotationPresent(DoNotCreateNorInsert.class)) {
                Column column = Column.fromField(field);
                if (column!=null) {
                    if (updateColumnNameSet.contains(column.name)) {
                        Object value = null;
                        value = field.get(object);
                        if (column.isNotNull && value == null) {
                            throw new ReflectSQLExceptions.NotNullColumnHasNullValueException();
                        }

                        String setValueString = Const.STRING_BEFORE_NAME + column.name + Const.STRING_AFTER_NAME
                                + Const.STRING_ASSIGN + Value.valueToString(value);
                        setValueStringList.add(setValueString);
                    }
                }
            }
        }

        String header = Const.STRING_UPDATE + Const.STRING_BEFORE_NAME + tableName + Const.STRING_AFTER_NAME + Const.STRING_SET;

        String result = header + StringUtils.join(setValueStringList, Const.STRING_GLUE) +
                Const.STRING_WHERE + whereExpr + Const.STRING_QUERY_ENDING;
        System.out.println(result);
        return result;
    }

    /*
    public PreparedStatement toPreparedStatement() throws ReflectMySQLException {
        try {
            PreparedStatement preparedStatement = null;
            preparedStatement = connection.prepareStatement(generateUpdateQuery());
            int index = 0;
            for (Column column: valueMap.keySet()) {
                Object object = valueMap.get(column);
                preparedStatement.setObject(indexMap.get(column), object);
                ++index;
            }
            for (Object value: whereValues) {
                if (value!=null && (value.getClass() == char.class || value.getClass() == Character.class)) {
                    preparedStatement.setObject(++index, String.valueOf(value));
                } else {
                    preparedStatement.setObject(++index, value);
                }
            }
            return preparedStatement;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ReflectMySQLException();
        }
    }*/

    public String toSQL() throws ReflectSQLiteException {
        try {
            String queryStr = generateUpdateQuery();
            int index = 0;
            Query query = new Query(queryStr);
            for (Object value: whereValues) {
                query.setValue(++index, value);
            }
            return query.toSQL();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ReflectSQLiteException();
        }
    }
}
