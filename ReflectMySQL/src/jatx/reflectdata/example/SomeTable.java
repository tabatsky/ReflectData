package jatx.reflectdata.example;

import jatx.reflectdata.annotations.*;

@TableName("some")
public class SomeTable
{
    @PrimaryKey
    @AutoIncrement
    public int id;
    @DoNotCreateNorInsert
    public int intNotCreate;
    @DoNotCreateNorInsert
    public String stringNotCreate;
    @DoNotInsert
    public int intNotInsert;
    @DoNotInsert
    public String stringNotInsert;
    @DoNotCreateNorInsert
    public int intNotCreateNorInsert;
    @DoNotCreateNorInsert
    public String stringNotCreateNorInsert;
    @Default("0")
    public int intV0;
    @Default("")
    public String stringV0;
    @FromVersion(1)
    public int intV1;
    @FromVersion(1)
    public String stringV1 = "v1";
    @FromVersion(2)
    @Default("2")
    public int intV2;
    @Default("two")
    @FromVersion(2)
    public String stringV2;
    @FromVersion(3)
    @Default("3")
    public int intV3;
    @Default("three")
    @FromVersion(3)
    public String stringV3;
}
