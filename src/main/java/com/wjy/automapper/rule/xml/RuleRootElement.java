package com.wjy.automapper.rule.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;

/**
 * @author weijiayu
 * @date 2024/5/23 10:32
 */
@XmlRootElement(name = "rule")
@XmlType(propOrder = {"id", "desc", "rs"})
@Data
public class RuleRootElement {

    private String id;
    private String desc;
    private List<RuleSingleElement> rs;

    public RuleRootElement() {}

    @XmlAttribute(name = "id")
    public String getId() {
        return id;
    }

    @XmlAttribute(name = "desc")
    public String getDesc() {
        return desc;
    }

    @XmlElement(name = "r")
    public List<RuleSingleElement> getRs() {
        return rs;
    }
}
