package jatx.reflectdata.example;

import jatx.reflectdata.mysql.*;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            MySQLClient client = new MySQLClient("root", "1234567", "testing");
            java.sql.Connection connection = client.connect();

            String query1 = new ReflectCreateTableStatement(connection, SomeTable.class)
                    .setTableName("someV1").setIfNotExists(false).setDBVersion(1).toSQLString();
            System.out.println(query1);

            SomeTable someTable = new SomeTable();

            String query2 = new ReflectInsertStatement(connection, someTable).setDBVersion(2).setFunctionValues("intV2:RAND()").toSQLString();
            System.out.println(query2);

            String query3 = new ReflectUpdateStatement(connection, someTable,
                    new String[] { "stringNotCreate", "intNotInsert", "intV1", "stringV1" },
                    "id=? AND stringV0=?", new Object[] { null, 'f' }).toSQLString();

            System.out.println(query3);

            List<String> queryList = new ReflectUpgradeTableStatement(connection, SomeTable.class, 1, 3)
                    .toSQLStringList();
            for (String query: queryList) {
                System.out.println(query);
            }

            client.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
