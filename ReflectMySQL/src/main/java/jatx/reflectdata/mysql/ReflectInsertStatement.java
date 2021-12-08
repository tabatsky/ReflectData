package jatx.reflectdata.mysql;

import jatx.reflectdata.annotations.DoNotCreateNorInsert;
import jatx.reflectdata.annotations.DoNotInsert;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class ReflectInsertStatement {
    private Map<Column, Object> valueMap = new HashMap();
    private Map<Column, Integer> indexMap = new HashMap();

    public static final int INSERT_DEFAULT = 0;
    public static final int INSERT_REPLACE = 1;
    public static final int INSERT_IGNORE = 2;
    private Connection connection;
    private Object object;
    private Class clazz;
    private int initIndex = 0;

    private int insertType;
    private String _tableName;
    private int dbVersion;

    private Map<String, String> functionValueMap = new HashMap();

    public ReflectInsertStatement(Connection connection, Object object) {
        this.connection = connection;
        this.object = object;
        clazz = object.getClass();
        this.insertType = INSERT_DEFAULT;
        _tableName = null;
        dbVersion = 0;
    }

    public ReflectInsertStatement setInsertType(int insertType) {
        this.insertType = insertType;
        return this;
    }

    public ReflectInsertStatement setTableName(String tableName) {
        this._tableName = tableName;
        return this;
    }

    public ReflectInsertStatement setDBVersion(int dbVersion) {
        this.dbVersion = dbVersion;
        return this;
    }

    public ReflectInsertStatement setFunctionValues(String functionValues) {
        String[] arr0 = functionValues.split(";");
        for (String str0 : arr0) {
            String[] arr1 = str0.split(":");
            functionValueMap.put(arr1[0], arr1[1]);
        }
        return this;
    }

    private String generateInsertQuery() throws IllegalAccessException, ReflectSQLExceptions.NotNullColumnHasNullValueException {
        String tableName = (_tableName == null) || (_tableName.isEmpty()) ? Table.getTableName(clazz) : _tableName;

        List<String> setValueStringList = new ArrayList();
        Field[] fields = clazz.getFields();

        for (Field field : fields) {
            int modifiers = field.getModifiers();
            if ((Modifier.isPublic(modifiers)) && (!Modifier.isStatic(modifiers))
                    && (!field.isAnnotationPresent(DoNotInsert.class))
                    && (!field.isAnnotationPresent(DoNotCreateNorInsert.class))) {

                Column column = Column.fromField(field);
                if ((column != null) && (!(column.isAutoIncrement && insertType==INSERT_DEFAULT))  && (dbVersion >= column.fromVersion)) {
                    if (functionValueMap.containsKey(column.name)) {
                        String setValueString = "`" + column.name + "`" + "=" + (String)functionValueMap.get(column.name);

                        setValueStringList.add(setValueString);
                    } else {
                        Object value = null;

                        value = field.get(object);

                        if ((value != null) || (!column.hasDefaultValue)) {
                            if ((column.isNotNull) && (value == null)) {
                                throw new ReflectSQLExceptions.NotNullColumnHasNullValueException();
                            }

                            String setValueString = Const.STRING_BEFORE_NAME + column.name + Const.STRING_AFTER_NAME
                                    + Const.STRING_ASSIGN + (value != null ? Const.STRING_VAR : Const.STRING_NULL);

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
            }
        }
        boolean insertReplaceSet = (insertType == INSERT_REPLACE);

        boolean insertIgnoreSet = (insertType == INSERT_IGNORE);

        String header = (insertReplaceSet ? Const.STRING_REPLACE : Const.STRING_INSERT) +
                (insertIgnoreSet ? Const.STRING_IGNORE : "") + Const.STRING_INTO +
                Const.STRING_BEFORE_NAME + tableName + Const.STRING_AFTER_NAME + Const.STRING_SET;



        String result = header + StringUtils.join(setValueStringList, Const.STRING_GLUE) + Const.STRING_QUERY_ENDING;
        //System.out.println(result);
        return result;
    }

    public PreparedStatement toPreparedStatement() throws ReflectMySQLException {
        try {
            PreparedStatement preparedStatement = null;
            preparedStatement = connection.prepareStatement(generateInsertQuery());

            for (Column column : valueMap.keySet()) {
                Object object = valueMap.get(column);

                preparedStatement.setObject(((Integer)indexMap.get(column)).intValue(), object);
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
