package com.wjy.automapper;

import java.util.concurrent.TimeUnit;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import lombok.extern.slf4j.Slf4j;

/**
 * @author weijiayu
 * @date 2024/5/23 11:46
 */
@Slf4j
public abstract class AbsFileMonitor extends FileAlterationListenerAdaptor {

    /**
     * 定时监听文件变化
     *
     * @param directoryName
     * @param suffix
     * @param intervalSec
     * @param listener
     * @return org.apache.commons.io.monitor.FileAlterationMonitor
     * @author weijiayu
     * @date 2024/5/23 11:44
     */
    public FileAlterationMonitor createFileMonitor(String directoryName, String suffix, long intervalSec,
        FileAlterationListener listener) {
        try {
            FileAlterationObserver observer = new FileAlterationObserver(directoryName,
                FileFilterUtils.and(FileFilterUtils.fileFileFilter(), FileFilterUtils.suffixFileFilter(suffix)), null);
            observer.addListener(listener);
            FileAlterationMonitor monitor = new FileAlterationMonitor(TimeUnit.SECONDS.toMillis(intervalSec), observer);
            monitor.start();
            return monitor;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
}
