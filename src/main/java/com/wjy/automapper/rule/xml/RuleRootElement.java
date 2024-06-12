package com.wjy.automapper.rule.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.serializer.SerializerFeature;

import lombok.Data;

/**
 * @author weijiayu
 * @date 2024/5/23 10:32
 */
@XmlRootElement(name = "rule")
@XmlType(propOrder = {"id", "desc", "serializerFeatures", "rs"})
@Data
public class RuleRootElement {

    private String id;
    private String desc;
    private String serializerFeatures;
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

    @XmlAttribute(name = "serializerFeatures")
    public String getSerializerFeatures() {
        return serializerFeatures;
    }

    @XmlElement(name = "r")
    public List<RuleSingleElement> getRs() {
        return rs;
    }

    public SerializerFeature[] getSerializerFeatureArray() {
        try {
            if (StringUtils.isEmpty(serializerFeatures)) {
                return new SerializerFeature[] {};
            }
            String[] indexList = serializerFeatures.split(",");
            SerializerFeature[] features = SerializerFeature.values();
            SerializerFeature[] settingFeatures = new SerializerFeature[indexList.length];
            for (int i = 0; i < settingFeatures.length; i++) {
                settingFeatures[i] = features[Integer.parseInt(indexList[i])];
            }
            return settingFeatures;
        } catch (Exception e) {
            return new SerializerFeature[] {};
        }
    }
}
