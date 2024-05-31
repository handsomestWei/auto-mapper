package com.wjy.automapper.rule.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;

/**
 * @author weijiayu
 * @date 2024/5/23 10:35
 */
@XmlRootElement(name = "p")
@XmlType(propOrder = {"from", "to", "func", "val"})
@Data
public class RuleSingleElement {

    private String from;
    private String to;
    private String func;
    private String val;

    public RuleSingleElement() {}

    @XmlAttribute(name = "from")
    public String getFrom() {
        return from;
    }

    @XmlAttribute(name = "to")
    public String getTo() {
        return to;
    }

    @XmlAttribute(name = "func")
    public String getFunc() {
        return func;
    }

    @XmlAttribute(name = "val")
    public String getVal() {
        return val;
    }
}
