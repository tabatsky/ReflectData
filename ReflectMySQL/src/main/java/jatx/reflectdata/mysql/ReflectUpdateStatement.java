package jatx.reflectdata.mysql;

import jatx.reflectdata.annotations.DoNotCreateNorInsert;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

public class ReflectUpdateStatement
{
    private Map<Column, Object> valueMap = new HashMap();
    private Map<Column, Integer> indexMap = new HashMap();

    private Connection connection;
    private Object object;
    private Class clazz;
    private String _tableName;
    private int initIndex = 0;

    private Set<String> updateColumnNameSet = new HashSet();
    private String whereExpr;
    private Object[] whereValues;

    public ReflectUpdateStatement(Connection connection, Object object, String[] updateColumnNames, String whereExpr, Object[] whereValues, String tableName) {
        this.connection = connection;
        this.object = object;
        clazz = object.getClass();
        _tableName = tableName;

        for (String name : updateColumnNames) {
            updateColumnNameSet.add(name);
        }

        this.whereExpr = whereExpr;
        this.whereValues = whereValues;
    }

    public ReflectUpdateStatement(Connection connection, Object object, String[] updateColumnNames, String whereExpr, Object[] whereValues) {
        this(connection, object, updateColumnNames, whereExpr, whereValues, null);
    }

    private String generateUpdateQuery() throws IllegalAccessException, ReflectSQLExceptions.NotNullColumnHasNullValueException {
        String tableName = (_tableName == null) || (_tableName.isEmpty()) ? Table.getTableName(clazz) : _tableName;

        List<String> setValueStringList = new ArrayList();
        Field[] fields = clazz.getFields();

        for (Field field : fields) {
            int modifiers = field.getModifiers();
            if ((Modifier.isPublic(modifiers)) && (!Modifier.isStatic(modifiers)) && (!field.isAnnotationPresent(DoNotCreateNorInsert.class)))
            {
                Column column = Column.fromField(field);
                if ((column != null) &&
                        (updateColumnNameSet.contains(column.name))) {
                    Object value = null;
                    value = field.get(object);
                    if ((column.isNotNull) && (value == null)) {
                        throw new ReflectSQLExceptions.NotNullColumnHasNullValueException();
                    }

                    String setValueString = Const.STRING_BEFORE_NAME + column.name + Const.STRING_AFTER_NAME +
                            Const.STRING_ASSIGN + (value != null ? Const.STRING_VAR : Const.STRING_NULL);

                    setValueStringList.add(setValueString);

                    if (value != null) {
                        if ((value.getClass() == Character.TYPE) || (value.getClass() == Character.class)) {
                            valueMap.put(column, String.valueOf(value));
                        } else {
                            valueMap.put(column, value);
                        }
                        indexMap.put(column, Integer.valueOf(++initIndex));
                    }
                }
            }
        }


        String header = Const.STRING_UPDATE + Const.STRING_BEFORE_NAME + tableName + Const.STRING_AFTER_NAME + Const.STRING_SET;

        String result = header + StringUtils.join(setValueStringList, Const.STRING_GLUE) + Const.STRING_WHERE + whereExpr + Const.STRING_QUERY_ENDING;

        //System.out.println(result);
        return result;
    }

    public PreparedStatement toPreparedStatement() throws ReflectMySQLException {
        try {
            PreparedStatement preparedStatement = null;
            preparedStatement = connection.prepareStatement(generateUpdateQuery());
            int index = 0;
            for (Column column : valueMap.keySet()) {
                Object object = valueMap.get(column);
                preparedStatement.setObject(((Integer)indexMap.get(column)).intValue(), object);
                index++;
            }
            for (Object value : whereValues) {
                if ((value != null) && ((value.getClass() == Character.TYPE) || (value.getClass() == Character.class))) {
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
    }

    public String toSQLString() throws ReflectMySQLException {
        return toPreparedStatement().toString();
    }
}
