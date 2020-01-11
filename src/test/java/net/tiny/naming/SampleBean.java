package net.tiny.naming;

import java.io.Serializable;

public class SampleBean implements Sample, Serializable {
    
	private static final long serialVersionUID = 1L;

	private String name;
    
    public SampleBean(String name) {
        if(name == null)
            throw new IllegalArgumentException("name is null");
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
    	this.name = name;
    }
}