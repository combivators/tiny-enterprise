package net.tiny.naming;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="binding")
public class BindingObject implements Cloneable {

    public enum ActionType {
        @XmlEnumValue("bind")
        BIND,

        @XmlEnumValue("rebind")
        REBIND,

        @XmlEnumValue("unbind")
        UNBIND,


        @XmlEnumValue("lookup")
        LOOKUP,

        @XmlEnumValue("list")
        LIST,

        @XmlEnumValue("rename")
        RENAME,

        @XmlEnumValue("create-context")
        CREATE_CONTEXT,

        @XmlEnumValue("destroy-context")
        DESTROY_CONTEXT,
    }

    @XmlAttribute(name = "path", required = true)
    private String path;

    @XmlAttribute(name = "name", required = true)
    private String name;

    @XmlAttribute(name = "action", required = true)
    private String action;

    @XmlJavaTypeAdapter(CDATAAdapter.class)
    @XmlElement(name = "object")
    private Object object;

    public BindingObject() {}

    public BindingObject(String path, String name, ActionType action) {
        this(path, name, action, null);
    }

    public BindingObject(String path, String name, ActionType action, Object obj) {
        this.path   = path;
        this.name   = name;
        this.action = action.name().toLowerCase();
        this.object = obj;
    }

    public Object getValue() {
        return this.object;
    }

    public String getPath() {
        return this.path;
    }

    public String getName() {
        return this.name;
    }

    public String getAction() {
        return this.action;
    }

    public ActionType getActionType() {
        return ActionType.valueOf(action.toUpperCase());
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setValue(Object obj) {
        this.object = obj;
    }

    public boolean hasContent() {
        return this.object != null;
    }

    @Override
    public String toString() {
        return "[[" + this.name + "]" + this.path + " " + this.action + " " + this.object + "]";
    }
}
