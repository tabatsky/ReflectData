package jatx.reflectdata.mysql;

import com.sun.deploy.util.StringUtils;
import jatx.reflectdata.annotations.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jatx on 13.06.17.
 */
public class MySQLQueryGenerator {
    private static final String queryEnding = ";";

    public static String generateCreateTableQuery(Class clazz) throws MoreThanOnePrimaryKeyException {
        List<Column> columnList = new ArrayList<>();
        Field[] fields = clazz.getFields();

        for (Field field: fields) {
            int modifiers = field.getModifiers();
            if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers)) {
                Column column = Column.fromField(field);
                if (column!=null) columnList.add(column);
            }
        }

        boolean ifNotExists = clazz.isAnnotationPresent(IfNotExists.class);

        String header = "CREATE TABLE " + (ifNotExists?"IF NOT EXISTS":"") + " `" + getTableName(clazz) + "` (\n";

        int primaryKeyCount = 0;
        String primaryKeyName = "";

        List<String> columnStringList = new ArrayList<>();

        for (Column column: columnList) {
            if (column.isPrimaryKey || column.isAutoIncrement) {
                primaryKeyCount++;
                primaryKeyName = column.name;
            }
            columnStringList.add(column.toString());
        }

        if (primaryKeyCount>1) throw new MoreThanOnePrimaryKeyException();
        String footer = "";
        if (primaryKeyCount==1) {
            footer = ",\nPRIMARY KEY (`" + primaryKeyName + "`))";
        } else {
            footer = ")";
        }

        return header + StringUtils.join(columnStringList, ",\n") + footer + queryEnding;
    }

    public static List<String> generateAlterTableQueries(Class oldClazz, Class newClazz) throws MoreThanOnePrimaryKeyException, PrimaryKeyMismatchException {
        List<String> result = new ArrayList<>();

        String oldTableName = getTableName(oldClazz);
        String newTableName = getTableName(newClazz);

        if (!oldTableName.equals(newTableName)) {
            String renameQuery = "RENAME TABLE `" + oldTableName + "` TO `" + newTableName + "`" + queryEnding;
            result.add(renameQuery);
        }

        List<Column> oldColumns = new ArrayList<>();
        List<Column> newColumns = new ArrayList<>();

        for (Field field: oldClazz.getFields()) {
            int modifiers = field.getModifiers();
            if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers)) {
                Column column = Column.fromField(field);
                if (column!=null) oldColumns.add(column);
            }
        }

        for (Field field: newClazz.getFields()) {
            int modifiers = field.getModifiers();
            if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers)) {
                Column column = Column.fromField(field);
                if (column!=null) newColumns.add(column);
            }
        }

        List<Column> columnsToDrop = new ArrayList<>();
        List<Column> columnsToAdd = new ArrayList<>();

        Map<Column,Column> columnsToChange = new HashMap<>();

        Column oldPrimaryKey = null;
        for (Column oldColumn: oldColumns) {
            boolean containsName = false;
            boolean containsSame = false;
            Column newWithSameName = null;
            for (Column newColumn: newColumns) {
                if (oldColumn.equalsByName(newColumn)) {
                    containsName = true;
                    newWithSameName = newColumn;
                    if (oldColumn.equals(newColumn)) containsSame = true;
                }
            }
            if (!containsSame && !containsName) {
                columnsToDrop.add(oldColumn);
            } else if (!containsSame) {
                columnsToChange.put(oldColumn, newWithSameName);
            }
            if (oldColumn.isPrimaryKey) oldPrimaryKey = oldColumn;
        }

        int newPrimaryKeyCount = 0;
        Column newPrimaryKey = null;
        for (Column newColumn: newColumns) {
            boolean containsName = false;
            for (Column oldColumn: oldColumns) {
                if (oldColumn.equalsByName(newColumn)) {
                    containsName = true;
                }
            }
            if (!containsName) columnsToAdd.add(newColumn);
            if (newColumn.isPrimaryKey || newColumn.isAutoIncrement) {
                newPrimaryKeyCount++;
                newPrimaryKey = newColumn;
            }
        }

        if (newPrimaryKeyCount>1) throw new MoreThanOnePrimaryKeyException();
        if (oldPrimaryKey!=null && !oldPrimaryKey.equals(newPrimaryKey)) throw new PrimaryKeyMismatchException();

        for (Column column: columnsToDrop) {
            String dropQuery = "ALTER TABLE `" + newTableName + "` DROP COLUMN `" + column.name + "`" + queryEnding;
            result.add(dropQuery);
        }

        for (Column column: columnsToAdd) {
            String addQuery = "ALTER TABLE `" + newTableName + "` ADD COLUMN " + column.toString() + queryEnding;
            result.add(addQuery);
        }

        for (Map.Entry<Column,Column> entry: columnsToChange.entrySet()) {
            String changeQuery = "ALTER TABLE `" + newTableName + "` CHANGE COLUMN `" + entry.getKey().name +
                    "` " + entry.getValue().toString() + queryEnding;
            result.add(changeQuery);
        }

        return result;
    }

    public static String generateInsertQuery(Object obj) throws IllegalAccessException, NotNullColumnHasNullValueException, BothInsertIgnoreAndReplaceSet {
        List<String> valueStringList = new ArrayList<>();
        Field[] fields = obj.getClass().getFields();

        for (Field field: fields) {
            int modifiers = field.getModifiers();
            if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers)) {
                Column column = Column.fromField(field);
                if (column!=null) {
                    if (column.isAutoIncrement) continue;
                    Object value = field.get(obj);
                    if (value==null && column.hasDefaultValue) continue;
                    if (column.isNotNull && value==null) {
                        throw new NotNullColumnHasNullValueException();
                    }
                    String valueString = value!=null ? "'" + value.toString().replace("'","\\'") + "'" : "NULL";
                    String setValueString = "`"+column.name+"`="+valueString;
                    valueStringList.add(setValueString);
                    //System.out.println(setValueString);
                }
            }
        }

        boolean insertIgnoreSet = obj.getClass().isAnnotationPresent(InsertIgnore.class);
        boolean insertReplaceSet = obj.getClass().isAnnotationPresent(InsertReplace.class);

        if (insertIgnoreSet && insertReplaceSet) throw new BothInsertIgnoreAndReplaceSet();

        String header = (insertReplaceSet ? "REPLACE " : "INSERT ")
                + (insertIgnoreSet ? "IGNORE " : "")
                + "INTO `" + getTableName(obj.getClass()) + "` SET ";

        return header + StringUtils.join(valueStringList, ", ") + queryEnding;
    }

    public static String generateUpdateQuery(Object obj) throws IllegalAccessException, NotNullColumnHasNullValueException, BothInsertIgnoreAndReplaceSet, NoPrimaryKeyException, MoreThanOnePrimaryKeyException, PrimaryKeyNullValueException {
        List<String> valueStringList = new ArrayList<>();
        Field[] fields = obj.getClass().getFields();

        String primaryKeyName = "";
        Object primaryKeyValue = null;
        int primaryKeyCount = 0;

        for (Field field: fields) {
            int modifiers = field.getModifiers();
            if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers)) {
                Column column = Column.fromField(field);
                if (column!=null) {
                    if (!column.isPrimaryKey && !column.isAutoIncrement) {
                        Object value = field.get(obj);
                        if (value == null && column.hasDefaultValue) continue;
                        if (column.isNotNull && value == null) {
                            throw new NotNullColumnHasNullValueException();
                        }
                        String valueString = value != null ? "'" + value.toString().replace("'", "\\'") + "'" : "NULL";
                        String setValueString = "`" + column.name + "`=" + valueString;
                        valueStringList.add(setValueString);
                        //System.out.println(setValueString);
                    } else {
                        primaryKeyCount++;
                        primaryKeyName = column.name;
                        primaryKeyValue = field.get(obj);
                    }
                }
            }
        }

        if (primaryKeyCount==0) throw new NoPrimaryKeyException();
        if (primaryKeyCount>1) throw new MoreThanOnePrimaryKeyException();
        if (primaryKeyValue==null) throw new PrimaryKeyNullValueException();
        String primaryKeyValueString = primaryKeyValue.toString().replace("'", "\\'") + "'";

        String header = "UPDATE `" + getTableName(obj.getClass()) + "` SET ";
        String footer = " WHERE `" + primaryKeyName + "`='" + primaryKeyValueString;

        return header + StringUtils.join(valueStringList, ", ") + footer + queryEnding;
    }

    public static String getTableName(Class clazz) {
        String tableName = "";
        if (clazz.isAnnotationPresent(TableName.class)) {
            TableName tableNameAnnotation = (TableName)clazz.getAnnotation(TableName.class);
            tableName = tableNameAnnotation.value();
        }

        if (tableName.isEmpty()) tableName = clazz.getSimpleName() + "_table";

        return tableName;
    }

    public static class MoreThanOnePrimaryKeyException extends Exception {}
    public static class NoPrimaryKeyException extends Exception {}
    public static class PrimaryKeyNullValueException extends Exception {}
    public static class PrimaryKeyMismatchException extends Exception {}
    public static class NotNullColumnHasNullValueException extends Exception {}
    public static class BothInsertIgnoreAndReplaceSet extends Exception {}
}
