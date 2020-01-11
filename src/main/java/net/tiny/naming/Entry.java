package net.tiny.naming;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="value")
public class Entry implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "type")
    private String type;

    @XmlValue
    private String value;

    public Entry() {}

    public Entry(Object value) {
        this(DataType.getDataType(value), value);
    }

    public Entry(DataType type, Object value) {
        if(null == type) {
            this.type = value.getClass().getName();
        } else {
            this.type = type.getName();
        }
        this.value = Transformer.format(value);
    }

    public Entry(String type, Object value) {
        this.type = type;
        this.value = Transformer.format(value);
    }

    public Entry(Class<?> type, Object value) {
        this.type = type.getName();
        this.value = Transformer.format(value);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Parses a given value and converts it to an Object
     * of the given type
     * @return the converted Object
     */
    public Object toValue() {
        return parse(this);
    }

    public Class<?> getClassType() throws ClassNotFoundException {
        DataType dataType = getDataType();
        if(null != dataType) {
            return dataType.getJavaDatatype();
        } else {
            return Class.forName(getType());
        }
    }

    protected DataType getDataType() {
        return DataType.getByName(type);
    }

    public static List<Entry> valueOf(List<Object> body) {
        List<Entry> list = new ArrayList<Entry>();
        for(Object value : body) {
            list.add(new Entry(value));
        }
        return list;
    }

    /**
     * Parses a given value and converts it to an Object
     * of the given type
     * @param entry the entry type
     * @return the converted Object
     * @throws MessageFormatException thrown if the given type is <code>null</code> or
     * the given type is not supported.
     */
    public static Object parse(Entry entry) {
        return Transformer.parse(entry.getValue(), DataType.getByName(entry.getType()));
    }

}
