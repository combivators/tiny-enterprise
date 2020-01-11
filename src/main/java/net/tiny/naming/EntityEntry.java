package net.tiny.naming;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="object")
public final class EntityEntry extends ValueEntry {

	private static final long serialVersionUID = 1L;

	@XmlElementWrapper(name = "properties")
    @XmlElement(name = "property")
    private List<ValueEntry> properties;

	public EntityEntry() {
	}

	public EntityEntry(String name, Object obj) {
		this(name, obj.getClass(), obj);
	}

	public EntityEntry(String name, Class<?> classType, Object obj) {
		if(DataType.isValidType(classType)) {
			// Like ValueEntry
			setName(name);
			setType(DataType.getByClass(classType).getName());
			setValue(Transformer.format(obj));
		} else {
			setName(name);
	        setType(classType.getName());
	        properties = toProperties(classType, obj);
		}
    }

    public List<ValueEntry> getProperties() {
        return this.properties;
    }

    public void setProperties(List<ValueEntry> list) {
        this.properties = list;
    }

    public Object toValue() {
    	if(null == properties || properties.isEmpty()) {
    		return super.parse(this);
    	} else {
    		return toValue(this.properties);
    	}
    }

    List<ValueEntry> toProperties(Class<?> classType, Object obj) {
    	try {
	    	List<ValueEntry> list =  new ArrayList<ValueEntry>();
			BeanInfo javaBean = Introspector.getBeanInfo(classType);
			PropertyDescriptor[] descriptors = javaBean.getPropertyDescriptors();
			for(PropertyDescriptor property : descriptors) {
				Method getter = property.getReadMethod();
				Method setter = property.getWriteMethod();
				if (null == getter || null == setter)
					continue;
				Class<?> type = getter.getReturnType();
				Object value = null;
				value = getter.invoke(obj);
				if(value != null && DataType.isValidType(type)) {
					ValueEntry entry = new ValueEntry(property.getName(), value);
					list.add(entry);
				//} else {
					//throw new IllegalArgumentException("Can not support property class type '" + type.getName() +"'");
				}
			}
			return list;
    	} catch (RuntimeException ex) {
    		throw ex;
    	} catch (Exception ex) {
			throw new IllegalArgumentException("Can not support property class type '" + classType.getName() +"' - " + obj.toString(), ex);
    	}
    }

    Object toValue(List<ValueEntry> list) {
    	try {
    		Class<?> classType = getClassType();
    		Object object = classType.newInstance();
    		BeanInfo javaBean = Introspector.getBeanInfo(classType);
    		PropertyDescriptor[] descriptors = javaBean.getPropertyDescriptors();
    		for(ValueEntry entry : list) {
    			Method setter = getWriteMethod(descriptors, entry.getName());
    			if(null != setter) {
    				Object value = entry.toValue();
    				if(null != value) {
    					setter.invoke(object, value);
    				}
    			}
    		}
    		return object;
    	} catch(Exception ex) {
    		return null;
    	}
    }

    private Method getWriteMethod(PropertyDescriptor[] descriptors, String name) {
		for(PropertyDescriptor property : descriptors) {
			if(property.getName().equals(name)) {
				Method setter = property.getWriteMethod();
				return setter;
			}
		}
		return null;
    }

}
