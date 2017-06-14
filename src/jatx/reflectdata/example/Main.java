package jatx.reflectdata.example;

import jatx.reflectdata.annotations.*;
import jatx.reflectdata.mysql.MySQLClient;
import jatx.reflectdata.mysql.MySQLQueryGenerator;

import java.util.Date;
import java.util.List;

/**
 * Created by jatx on 13.06.17.
 */
public class Main {

    public static void main(String[] args) {
        try {
            System.out.println(MySQLQueryGenerator.generateCreateTableQuery(PersonV2.class));

            MySQLClient client = new MySQLClient("login", "password", "dbName");
            client.connect();

            client.createTable(PersonV1.class);
            client.alterTable(PersonV1.class, PersonV2.class);

            PersonV2 person = new PersonV2();
            person.createTime = new Date().getTime();
            person.firstName = "Ivan";
            person.lastName = "Ivanov";
            client.insert(person);

            person.age = 28;
            person.createTime = new Date().getTime();
            person.address = "Zimbabve";
            client.insert(person);

            person.createTime = new Date().getTime();
            person.firstName = "John";
            person.lastName = "Johnson";
            person.someLetter = 'i';
            client.insert(person);

            List selected = client.select(PersonV2.class);
            System.out.println("Rows: " + selected.size());
            for (Object obj: selected) {
                System.out.println(obj);
            }

            client.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @IfNotExists
    @InsertIgnore
    public static class PersonV1 {
        @AutoIncrement
        @PrimaryKey
        public int id;
        @NotNull
        public String fullName;
        @Default("18")
        public Integer age;
        @Default("")
        public String address;
    }

    @IfNotExists
    @InsertReplace
    @TableName("persons")
    public static class PersonV2 {
        @AutoIncrement
        @PrimaryKey
        public int id;
        @NotNull
        public long createTime;
        @NotNull
        public String firstName;
        @NotNull
        public String lastName;
        @Default("21")
        public Integer age;
        @Default("")
        @MaxLength(1024)
        public String address;
        public Character someLetter;

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("id: ");
            sb.append(id);
            sb.append(", createTime: ");
            sb.append(createTime);
            sb.append(", firstName: ");
            sb.append(firstName);
            sb.append(", lastName: ");
            sb.append(lastName);
            sb.append(", age: ");
            sb.append(age);
            sb.append(", address: ");
            sb.append(address);
            sb.append(", someLetter: ");
            sb.append(someLetter);
            return sb.toString();
        }
    }
}
