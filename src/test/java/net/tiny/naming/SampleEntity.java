package net.tiny.naming;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="entity")
public class SampleEntity {

    @XmlElement(name = "mapentry")
    private List<ValueEntry> mapEntries = new ArrayList<ValueEntry>();
    
    @XmlElementWrapper(name = "list")
    @XmlElement(name = "entry")
    private List<Entry> listEntries = new ArrayList<Entry>();
    
    @XmlJavaTypeAdapter(CDATAAdapter.class)
	@XmlElement(name = "object")
    private Object object;
    
	@XmlElement(name = "prop")
    private Properties prop;
	
    public List<ValueEntry> getMapEntries() {
        return this.mapEntries;
    }
    
    public void setMapEntries(List<ValueEntry> list) {
        this.mapEntries = list;
    }
    
    public List<Entry> getListEntries() {
        return this.listEntries;
    }
    
    public void setListEntries(List<Entry> list) {
        this.listEntries = list;
    }
    
    public void setObject(Object obj) {
        this.object = obj;
    }
    
    public Object getObject() {
        return this.object;
    }
    
    public void setProp(Properties prop) {
        this.prop = prop;
    }

    public Properties getProp() {
        return this.prop;
    }
}
