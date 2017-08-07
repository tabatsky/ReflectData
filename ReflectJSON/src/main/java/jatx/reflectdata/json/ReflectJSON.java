package jatx.reflectdata.json;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jatx.debug.Debug;
import jatx.debug.Log;

/**
 * Created by jatx on 25.07.17.
 */
public class ReflectJSON {
    public static final String FIELD_CLASS = "__CLASS__";
    public static final String ERROR_ILLEGAL_ACCESS = "__ERROR_ILLEGAL_ACCESS__";
    public static final String MESSAGE_ERROR_REFLECT_JSON = "errorReflectJsonServer";
    public static final String LOG_TAG_REFLECT_JSON = "reflectJson";

    public static Set<Class> primitiveTypes = new HashSet<Class>();
    public static Set<Class> objectTypes = new HashSet<Class>();
    static {
        objectTypes.add(String.class);
        primitiveTypes.add(char.class);
        objectTypes.add(Character.class);
        primitiveTypes.add(boolean.class);
        objectTypes.add(Boolean.class);
        primitiveTypes.add(byte.class);
        objectTypes.add(Byte.class);
        primitiveTypes.add(short.class);
        objectTypes.add(Short.class);
        primitiveTypes.add(int.class);
        objectTypes.add(Integer.class);
        primitiveTypes.add(long.class);
        objectTypes.add(Long.class);
        primitiveTypes.add(float.class);
        objectTypes.add(Float.class);
        primitiveTypes.add(double.class);
        objectTypes.add(Double.class);
    }

    public static JSONObject toJSON(Object object) {
        try {
            JSONObject jsonObject = new JSONObject();

            Field[] fields = object.getClass().getFields();

            for (Field field : fields) {
                int modifiers = field.getModifiers();
                if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers)) {
                    Class fieldType = field.getType();
                    Object value = field.get(object);
                    if (value==null) {
                        jsonObject.put(field.getName(), null);
                    } if (primitiveTypes.contains(fieldType) || objectTypes.contains(fieldType)) {
                        jsonObject.put(field.getName(), value);
                    } else if (value==null) {
                        jsonObject.put(field.getName(), value);
                    } else if (value instanceof JSONObject || value instanceof JSONArray) {
                        jsonObject.put(field.getName(), value);
                    } else if (value instanceof List) {
                        jsonObject.put(field.getName(), toJSONArray((List)value));
                    } else {
                        if (objectTypes.contains(value.getClass())) {
                            jsonObject.put(field.getName(), value);
                        } else {
                            jsonObject.put(field.getName(), toJSON(value));
                        }
                    }
                }
            }

            jsonObject.put(FIELD_CLASS, object.getClass().getCanonicalName());

            return jsonObject;
        } catch (Exception e) {
            Log.e(LOG_TAG_REFLECT_JSON, Debug.exceptionToString(e));
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(FIELD_CLASS, ERROR_ILLEGAL_ACCESS);
            return jsonObject;
        }
    }

    public static Object fromJSON(JSONObject jsonObject, Class clazz) {
        String className = (String) jsonObject.get(FIELD_CLASS);
        if (className.equals(ERROR_ILLEGAL_ACCESS)) {
            Log.e(LOG_TAG_REFLECT_JSON, "illegalAccess");
            return null;
        }

        try {
            Object object = clazz.newInstance();

            Field[] fields = clazz.getFields();

            for (Field field : fields) {
                int modifiers = field.getModifiers();
                if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers)) {
                    Class fieldType = field.getType();
                    final Object value;
                    if (jsonObject.containsKey(field.getName())) {
                        value = jsonObject.get(field.getName());
                    } else {
                        value = null;
                    }
                    if (value == null) {
                        field.set(object, null);
                    } else if (primitiveTypes.contains(fieldType)) {
                        if (fieldType == boolean.class) {
                            field.setBoolean(object, (Boolean) value);
                        } else if (fieldType == char.class) {
                            field.setChar(object, (Character) value);
                        } else if (fieldType == byte.class) {
                            field.setByte(object, ((Number) value).byteValue());
                        } else if (fieldType == short.class) {
                            field.setShort(object, ((Number) value).shortValue());
                        } else if (fieldType == int.class) {
                            field.setInt(object, ((Number) value).intValue());
                        } else if (fieldType == long.class) {
                            field.setLong(object, ((Number) value).longValue());
                        } else if (fieldType == float.class) {
                            field.setFloat(object, ((Number) value).floatValue());
                        } else if (fieldType == double.class) {
                            field.setDouble(object, ((Number) value).doubleValue());
                        }
                    } else if (objectTypes.contains(fieldType)) {
                        //Log.e(field.getName(), value.getClass().getCanonicalName());
                        if (fieldType == Boolean.class) {
                            field.set(object, (Boolean) value);
                        } else if (fieldType == Character.class) {
                            field.set(object, (Character) value);
                        } else if (fieldType == Byte.class) {
                            field.set(object, ((Number) value).byteValue());
                        } else if (fieldType == Short.class) {
                            field.set(object, ((Number) value).shortValue());
                        } else if (fieldType == Integer.class) {
                            //field.set(object, new Integer(((Number) value).intValue()));
                            field.set(object, ((Number) value).intValue());
                        } else if (fieldType == Long.class) {
                            field.set(object, ((Number) value).longValue());
                        } else if (fieldType == Float.class) {
                            field.set(object, ((Number) value).floatValue());
                        } else if (fieldType == Double.class) {
                            field.set(object, ((Number) value).doubleValue());
                        } else {
                            field.set(object, value);
                        }
                    } else if (fieldType == Object.class && objectTypes.contains(value.getClass())) {
                        field.set(object, value);
                    } else if (value instanceof JSONObject) {
                        if (fieldType==JSONObject.class || fieldType==Object.class) {
                            field.set(object, value);
                        } else {
                            field.set(object, fromJSON((JSONObject)value, fieldType));
                        }
                    } else if (value instanceof JSONArray) {
                        if (fieldType==JSONArray.class || fieldType==Object.class) {
                            field.set(object, value);
                        } else if (fieldType==List.class) {
                            try {
                                ParameterizedType listType = (ParameterizedType) field.getGenericType();
                                Type[] types = listType.getActualTypeArguments();
                                Class listClass = (Class) types[0];
                                field.set(object, fromJSONArray((JSONArray) value, listClass));
                            } catch (Exception e) {
                                Log.e(LOG_TAG_REFLECT_JSON, "cannot use generic list");
                                field.set(object, fromJSONArray((JSONArray) value, null));
                            }
                        }
                    }
                }
            }

            return object;
        } catch (Exception e) {
            Log.e(LOG_TAG_REFLECT_JSON, Debug.exceptionToString(e));
            return null;
        }
    }

    public static Object fromJSON(String jsonObjectString, Class clazz) {
        try {
            return fromJSON((JSONObject) new JSONParser().parse(jsonObjectString), clazz);
        } catch (Exception e) {
            Log.e(LOG_TAG_REFLECT_JSON, Debug.exceptionToString(e));
            return null;
        }
    }

    public static JSONArray toJSONArray(List list) {
        JSONArray jsonArray = new JSONArray();

        for (Object object: list) {
            if (object==null) {
                jsonArray.add(null);
            } else if (objectTypes.contains(object.getClass())) {
                jsonArray.add(object);
            } else if (object instanceof JSONObject || object instanceof JSONArray) {
                jsonArray.add(object);
            } else if (object instanceof List) {
                jsonArray.add(toJSONArray((List)object));
            } else {
                jsonArray.add(toJSON(object));
            }
        }

        return jsonArray;
    }

    public static List fromJSONArray(JSONArray jsonArray, Class clazz) {
        List<Object> list = new ArrayList<Object>();

        for (int i=0; i<jsonArray.size(); i++) {
            Object object = jsonArray.get(i);
            if (object==null) {
                list.add(null);
            } else if (clazz==null || object.getClass()==clazz && objectTypes.contains(object.getClass())) {
                list.add(object);
            } else if (object instanceof JSONObject) {
                list.add(fromJSON((JSONObject)object, clazz));
            } else if (object instanceof JSONArray) {
                list.add(fromJSONArray((JSONArray)object, clazz));
            }
        }

        return list;
    }

    public static List fromJSONArray(String jsonArrayString, Class clazz) {
        try {
            return fromJSONArray((JSONArray) new JSONParser().parse(jsonArrayString), clazz);
        } catch (Exception e) {
            Log.e(LOG_TAG_REFLECT_JSON, Debug.exceptionToString(e));
            return null;
        }
    }
}
