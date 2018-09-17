package com.mingyue.common;

import java.io.File;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationUtils;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//加载配置 另外新增定时任务 定时刷新配置文件  commons-configuration 本身就有reload能力，没采用，使用了定时任务来刷新
public class ConfigUtil {

    public static volatile Configuration config =  null;

    private static Logger LOG = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    public static synchronized void loadConfig() {
        LOG.debug("begin load configurations");
        Configurations configs = new Configurations();
        try {
            Configuration newconfig = configs.properties(new File("conf/config.properties"));

            config = newconfig;  //加载完成后替换变量
            // access configuration properties
        } catch (Exception e) {
            // Something went wrong
            e.printStackTrace();
        }
    }
    //简单的判空，避免第一次
    public static String getProperty(String key){
        if(config == null){
            loadConfig();
        }
        return config.getString(key);
    }

    public static void printProperties(){
        LOG.info(ConfigurationUtils.toString(config)); 
    }
}