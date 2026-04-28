package com.wjy.automapper.schema.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;

/**
 * @author weijiayu
 * @date 2024/5/22 17:01
 */
@XmlRootElement(name = "schema")
@XmlType(propOrder = {"id", "desc", "ps"})
@Data
public class SchemaRootElement {

    private String id;
    private String desc;
    private List<SchemaPropertyElement> ps;

    public SchemaRootElement() {}

    public SchemaRootElement(String id, String desc) {
        this.id = id;
        this.desc = desc;
    }

    @XmlAttribute(name = "id")
    public String getId() {
        return id;
    }

    @XmlAttribute(name = "desc")
    public String getDesc() {
        return desc;
    }

    // @XmlElementWrapper(name = "ps")
    @XmlElement(name = "p")
    public List<SchemaPropertyElement> getPs() {
        return ps;
    }
}
