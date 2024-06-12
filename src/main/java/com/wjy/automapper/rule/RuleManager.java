package com.wjy.automapper.rule;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.lang3.StringUtils;

import com.wjy.automapper.AbsFileMonitor;
import com.wjy.automapper.constant.XmlConstant;
import com.wjy.automapper.rule.xml.RuleRootElement;
import com.wjy.automapper.util.PathUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author weijiayu
 * @date 2024/5/24 14:31
 */
@Slf4j
public class RuleManager extends AbsFileMonitor {

    /**
     * key=ruleId。约定rule文件名和ruleId一致，id全局唯一
     */
    private ConcurrentHashMap<String, RuleRootElement> ruleMap = new ConcurrentHashMap<>();
    private String ruleFileDir;
    private FileAlterationMonitor fileMonitor;
    private volatile static RuleManager ruleManager;

    private RuleManager() {
        String ruleFileDir = PathUtil.getResourcePath() + XmlConstant.RULE_FILE_DEFAULT_DIR;
        new RuleManager(ruleFileDir);
    }

    private RuleManager(String ruleFileDir) {
        if (StringUtils.isEmpty(ruleFileDir)) {
            ruleFileDir = PathUtil.getResourcePath() + XmlConstant.SCHEMA_FILE_DEFAULT_DIR;
        }
        this.ruleFileDir = ruleFileDir;
        fileMonitor = createFileMonitor(ruleFileDir, XmlConstant.RULE_FILE_SUFFIX,
            XmlConstant.FILE_MONITOR_INTERVAL_DEFAULT_SEC, this);
    }

    public static RuleManager newInstance(String ruleFileDir) {
        if (ruleManager != null) {
            ruleManager.destroy();
        }
        ruleManager = new RuleManager(ruleFileDir);
        return ruleManager;
    }

    public static RuleManager getInstance() {
        if (ruleManager == null) {
            synchronized (RuleManager.class) {
                if (ruleManager == null) {
                    ruleManager = new RuleManager();
                }
            }
        }
        return ruleManager;
    }

    public void destroy() {
        try {
            if (fileMonitor != null) {
                fileMonitor.stop();
            }
        } catch (Exception e) {
        }
    }

    public RuleRootElement getRule(String ruleId) {
        try {
            if (StringUtils.isEmpty(ruleId)) {
                return null;
            }
            RuleRootElement rootElement = ruleMap.get(ruleId);
            if (rootElement == null) {
                // 延迟加载
                String filePath = ruleFileDir + ruleId + XmlConstant.RULE_FILE_SUFFIX;
                rootElement = loadRuleFile(filePath);
            }
            return rootElement;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void onFileCreate(File file) {
        // do not thing
    }

    @Override
    public void onFileChange(File file) {
        String ruleId = RuleUtil.getRuleId(file.getName());
        if (StringUtils.isEmpty(ruleId)) {
            return;
        }
        if (!ruleMap.containsKey(ruleId)) {
            return;
        } else {
            loadRuleFile(file.getAbsolutePath());
        }
    }

    @Override
    public void onFileDelete(File file) {
        String ruleId = RuleUtil.getRuleId(file.getName());
        if (StringUtils.isEmpty(ruleId)) {
            return;
        }
        ruleMap.remove(ruleId);
    }

    private RuleRootElement loadRuleFile(String filePath) {
        RuleRootElement rootElement = RuleUtil.loadRuleFile(filePath);
        if (rootElement != null && StringUtils.isNotEmpty(rootElement.getId())) {
            String ruleId = rootElement.getId();
            ruleMap.put(ruleId, rootElement);
            return rootElement;
        } else {
            return null;
        }
    }
}
