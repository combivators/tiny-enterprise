package net.tiny.naming;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="value")
public class ValueEntry extends Entry {

	private static final long serialVersionUID = 1L;

	@XmlAttribute(name = "name")
    private String name;

	public ValueEntry() {}

	public ValueEntry(String name, DataType type, Object value) {
		super(type, value);
		setName(name);
	}

	public ValueEntry(String name, String type, Object value) {
		super(type, value);
		setName(name);
	}

	public ValueEntry(String name, Class<?> type, Object value) {
		super(type, value);
		setName(name);
	}

	public ValueEntry(String name, Object value) {
		this(name, DataType.getDataType(value), value);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static List<ValueEntry> valueOf(Properties properties) {
		List<ValueEntry> list = new ArrayList<ValueEntry>();
        Enumeration<?> names = properties.keys();
        while(names.hasMoreElements()) {
            String name = (String)names.nextElement();
            Object value = properties.getProperty(name);
            list.add(new ValueEntry(name, value));
        }
        return list;
	}
}
