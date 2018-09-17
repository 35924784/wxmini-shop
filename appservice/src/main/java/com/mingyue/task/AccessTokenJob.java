package com.mingyue.task;

import java.util.Date;

import com.mingyue.common.AccessTokenUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.resteasy.util.DateUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class AccessTokenJob implements Job {
    private static Logger LOG = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LOG.info(Thread.currentThread().getName() + " test job begin " + DateUtil.formatDate(new Date()));
        LOG.info(AccessTokenUtil.getAccessToken());
    }
}