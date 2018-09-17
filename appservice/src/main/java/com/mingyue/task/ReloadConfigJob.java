package com.mingyue.task;

import java.util.Date;

import com.mingyue.common.ConfigUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.resteasy.util.DateUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
//其实现在的代码并没有用到，都是final String，改了配置之后重启吧 哈哈
public class ReloadConfigJob implements Job {
    private static Logger LOG = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LOG.info(Thread.currentThread().getName() + " ReloadConfigJob begin " + DateUtil.formatDate(new Date()));
        ConfigUtil.loadConfig();
        //ConfigUtil.printProperties();  //打印
    }
}