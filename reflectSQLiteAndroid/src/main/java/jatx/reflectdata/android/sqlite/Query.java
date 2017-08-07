package jatx.reflectdata.android.sqlite;

/**
 * Created by jatx on 07.08.17.
 */

public class Query {
    private String query;

    public Query(String query) {
        this.query = query;
    }

    public void setValue(int index, Object value) throws ReflectSQLExceptions.UnsupportedValueException {
        query = query.replace(wrapIndex(index), Value.valueToString(value));
    }


    public static String wrapIndex(int index) {
        return "{" + index + "}";
    }

    public String toSQL() {
        return query;
    }
}
