package jatx.reflectdata.android.sqlite;

/**
 * Created by jatx on 26.07.17.
 */
public class ReflectSQLExceptions {
    public static class MoreThanOnePrimaryKeyException extends Exception {}
    public static class NotNullColumnHasNullValueException extends Exception {}
    public static class UnsupportedValueException extends Exception {}
    //public static class BothIgnoreAndReplaceSet extends Exception {}
}
