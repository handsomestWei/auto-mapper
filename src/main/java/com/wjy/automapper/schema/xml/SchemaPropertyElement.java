package com.wjy.automapper.schema.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;

/**
 * @author weijiayu
 * @date 2024/5/22 17:17
 */
@XmlRootElement(name = "p")
@XmlType(propOrder = {"name", "desc", "type", "defVal", "eg", "ps"})
@Data
public class SchemaPropertyElement {

    private String name;
    private String desc;
    private String type;
    private String defVal;
    private String eg;
    private List<SchemaPropertyElement> ps;

    public SchemaPropertyElement() {}

    @XmlAttribute(name = "name")
    public String getName() {
        return name;
    }

    @XmlAttribute(name = "desc")
    public String getDesc() {
        return desc;
    }

    @XmlAttribute(name = "type")
    public String getType() {
        return type;
    }

    @XmlAttribute(name = "defVal")
    public String getDefVal() {
        return defVal;
    }

    @XmlAttribute(name = "eg")
    public String getEg() {
        return eg;
    }

    @XmlElement(name = "p")
    public List<SchemaPropertyElement> getPs() {
        return ps;
    }
}
