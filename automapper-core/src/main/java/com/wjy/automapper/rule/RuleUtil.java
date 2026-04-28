package com.wjy.automapper.rule;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

import com.wjy.automapper.constant.XmlConstant;
import com.wjy.automapper.rule.xml.RuleRootElement;
import com.wjy.automapper.util.XmlUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author weijiayu
 * @date 2024/5/23 10:41
 */
@Slf4j
public class RuleUtil extends XmlUtil {

    public static RuleRootElement loadRuleFile(String filePath) {
        try {
            return (RuleRootElement)unmarshaller(RuleRootElement.class, new File(filePath));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public static String getRuleId(String ruleFileName) {
        try {
            if (StringUtils.isEmpty(ruleFileName) || !ruleFileName.endsWith(XmlConstant.RULE_FILE_SUFFIX)) {
                return null;
            }
            return ruleFileName.split(XmlConstant.RULE_FILE_SUFFIX)[0];
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
}
