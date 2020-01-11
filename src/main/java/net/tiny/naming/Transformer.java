package net.tiny.naming;

import java.util.Collections;
import java.util.HashMap;

public class Transformer extends HashMap<DataType, Converter> {

    private static final long serialVersionUID = 1L;

    private static Transformer instance = new Transformer();

    private Transformer() {
        put(DataType.BYTE, Converter.BYTE);
        put(DataType.CHAR, Converter.CHAR);
        put(DataType.BOOLEAN, Converter.BOOLEAN);
        put(DataType.INTEGER, Converter.INTEGER);
        put(DataType.SHORT, Converter.SHORT);
        put(DataType.LONG, Converter.LONG);
        put(DataType.FLOAT, Converter.FLOAT);
        put(DataType.DOUBLE, Converter.DOUBLE);
        put(DataType.STRING, Converter.STRING);
        put(DataType.DATE, Converter.DATE);
        put(DataType.DATETIME, Converter.DATETIME);
        put(DataType.TIMESTAMP, Converter.TIMESTAMP);
        put(DataType.CALENDAR, Converter.CALENDAR);
        put(DataType.LIST, Converter.LIST);
        put(DataType.PROPERTIES, Converter.PROPERTIES);
        put(DataType.MAP, Converter.MAP);
        put(DataType.STRING_ARRAY, Converter.STRING_ARRAY);
        put(DataType.BOOLEAN_ARRAY, Converter.BOOLEAN_ARRAY);
        put(DataType.INTEGER_ARRAY, Converter.INTEGER_ARRAY);
        put(DataType.LONG_ARRAY, Converter.LONG_ARRAY);
        put(DataType.FLOAT_ARRAY, Converter.FLOAT_ARRAY);
        put(DataType.DOUBLE_ARRAY, Converter.DOUBLE_ARRAY);
        put(DataType.BYTE_ARRAY, Converter.BYTE_ARRAY);
        put(DataType.CHAR_ARRAY, Converter.CHAR_ARRAY);
        put(DataType.CDATA, Converter.CDATA);
        put(DataType.XML_DATA, Converter.XML_DATA);
        //put(DataType.BEAN, Converter.BEAN);
        Collections. unmodifiableMap(this);
    }

    public static Transformer getInstance() {
        return instance;
    }

    public static String format(Object value) {
        String ret = null;
        if(value != null) {
            if(DataType.isValidType(value)) {
                DataType type = DataType.getDataType(value);
                if(DataType.ENUM.equals(type)) {
                    return value.toString();
                } else {
                    Converter converter = instance.get(type);
                    ret = converter.format(value);
                }
            } else {
                throw new IllegalArgumentException("Can not format object '" + value.toString() +"'");
            }
        }
        return ret;
    }

    /**
     * Parses a given value and converts it to an Object of the given data type
     *
     * @param value the String representation of the Object value
     * @param type the target type
     * @return the converted Object
     */
    public static Object parse(String value, DataType type) {
        if(value == null) {
            return null;
        }
        if (type == null) {
            type = DataType.guessTypeByValue(value);
            if (type == null) {
                throw new IllegalArgumentException("Null is not a valid type.");
            }
        }
        Converter converter = instance.get(type);
        return converter.parse(value);
    }

    /**
     * Parses a given value and converts it to an Object of the given class type.
     *
     * @param value the String representation of the Object value
     * @param classType the target class type
     * @return the converted Object
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T> T parse(String value, Class<T> classType) {
        if (classType.isEnum()) {
            return (T)classType.cast(Enum.valueOf((Class<? extends Enum>)classType, value));
        } else {
            DataType type = DataType.getByClass(classType);
            return (T)parse(value, type);
        }
    }

}
