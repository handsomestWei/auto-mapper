package com.wjy.automapper.schema;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.wjy.automapper.AbsFileMonitor;
import com.wjy.automapper.constant.XmlConstant;
import com.wjy.automapper.schema.xml.SchemaRootElement;
import com.wjy.automapper.util.JsonUtil;
import com.wjy.automapper.util.PathUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author weijiayu
 * @date 2024/5/23 11:16
 */
@Slf4j
public class SchemaManager extends AbsFileMonitor {

    /**
     * key=schemaId。约定schema文件名和schemaId一致，id全局唯一
     */
    private ConcurrentHashMap<String, JSONObject> schemaObjMap = new ConcurrentHashMap<>();
    private String schemaFileDir;
    private FileAlterationMonitor fileMonitor;
    private volatile static SchemaManager schemaManager;

    private SchemaManager() {
        String schemaFileDir = PathUtil.getResourcePath() + XmlConstant.SCHEMA_FILE_DEFAULT_DIR;
        new SchemaManager(schemaFileDir);
    }

    private SchemaManager(String schemaFileDir) {
        if (StringUtils.isEmpty(schemaFileDir)) {
            schemaFileDir = PathUtil.getResourcePath() + XmlConstant.SCHEMA_FILE_DEFAULT_DIR;
        }
        this.schemaFileDir = schemaFileDir;
        fileMonitor = createFileMonitor(schemaFileDir, XmlConstant.SCHEMA_FILE_SUFFIX,
            XmlConstant.FILE_MONITOR_INTERVAL_DEFAULT_SEC, this);
    }

    public static SchemaManager newInstance(String schemaFileDir) {
        if (schemaManager != null) {
            schemaManager.destroy();
        }
        schemaManager = new SchemaManager(schemaFileDir);
        return schemaManager;
    }

    public static SchemaManager getInstance() {
        if (schemaManager == null) {
            synchronized (SchemaManager.class) {
                if (schemaManager == null) {
                    schemaManager = new SchemaManager();
                }
            }
        }
        return schemaManager;
    }

    public void destroy() {
        try {
            if (fileMonitor != null) {
                fileMonitor.stop();
            }
        } catch (Exception e) {
        }
    }

    public JSONObject getTplObjWithXml(String schemaId) {
        try {
            if (StringUtils.isEmpty(schemaId)) {
                return null;
            }
            JSONObject jsonObject = schemaObjMap.get(schemaId);
            if (jsonObject == null) {
                // 延迟加载
                String filePath = schemaFileDir + schemaId + XmlConstant.SCHEMA_FILE_SUFFIX;
                jsonObject = loadSchemaFile(filePath);
            }
            if (jsonObject != null) {
                // 对象池化，基于模板做对象拷贝
                return JsonUtil.deepClone(jsonObject);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public JSONObject getTplObjWithClass(Class pojoClass) {
        try {
            if (pojoClass == null) {
                return null;
            }
            return JsonUtil.genJsonObject(pojoClass);
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
        String schemaId = SchemaUtil.getSchemaId(file.getName());
        if (StringUtils.isEmpty(schemaId)) {
            return;
        }
        if (!schemaObjMap.containsKey(schemaId)) {
            return;
        } else {
            loadSchemaFile(file.getAbsolutePath());
        }
    }

    @Override
    public void onFileDelete(File file) {
        String schemaId = SchemaUtil.getSchemaId(file.getName());
        if (StringUtils.isEmpty(schemaId)) {
            return;
        }
        schemaObjMap.remove(schemaId);
    }

    private JSONObject loadSchemaFile(String filePath) {
        SchemaRootElement rootElement = SchemaUtil.loadSchemaFile(filePath);
        if (rootElement != null) {
            String schemaId = rootElement.getId();
            if (StringUtils.isEmpty(schemaId)) {
                return null;
            }
            JSONObject jsonObject = SchemaUtil.xmlDocToJson(rootElement);
            schemaObjMap.put(schemaId, jsonObject);
            return jsonObject;
        } else {
            return null;
        }
    }

}
