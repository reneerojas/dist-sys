/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Renee
 */
public class ComExecutor {

    private ExecutorService executorService;
    private final LinkedBlockingQueue<Runnable> taskList;

    private ComExecutor() {
        taskList = new LinkedBlockingQueue<>();
    }
    
    public final void start(){
        executorService = new ThreadPoolExecutor(
                2,
                5,
                10,
                TimeUnit.SECONDS,
                taskList);
    }

    public static ComExecutor getInstance() {
        return ComExecutorHolder.INSTANCE;
    }

    private static class ComExecutorHolder {

        private static final ComExecutor INSTANCE = new ComExecutor();
    }

    public ExecutorService getExecutor() {
        return executorService;
    }
    
    public void removeAll(){
        taskList.clear();
    }
    
    public int taskNumber(){
        return taskList.size();
    }
}
