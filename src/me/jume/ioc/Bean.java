package me.jume.ioc;

import java.util.ArrayList;
import java.util.List;

public class Bean {

     private String name;
     private String className;
     private String scope;
     private List<Property> props = new ArrayList<Property>();

     public void addProp(Property property){
         props.add(property);
     }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public List<Property> getProps() {
        return props;
    }

    public void setProps(List<Property> props) {
        this.props = props;
    }
}
