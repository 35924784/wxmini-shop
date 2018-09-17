package com.mingyue.restapi.user;

import static com.mongodb.client.model.Filters.eq;

import java.util.Iterator;
import java.util.Map;

import com.mingyue.database.MongoDBUtil;
import com.mongodb.client.MongoCollection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ehcache.spi.loaderwriter.BulkCacheLoadingException;
import org.ehcache.spi.loaderwriter.BulkCacheWritingException;
import org.ehcache.spi.loaderwriter.CacheLoaderWriter;


public class UserLoaderWriter implements CacheLoaderWriter<String, User>{

    private static Logger LOG = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    
    private static MongoCollection<User> collection = MongoDBUtil.getDataBase().getCollection("user", User.class);

    //会有缓存穿透，但是实际情况中乐观认为不会存在很多这种场景
    public User load(String sessionid) throws Exception{
        LOG.info("LoadCache sessionid:" + sessionid);
        return collection.find(eq("sessionid", sessionid)).first();
    }

    public Map<String, User> loadAll(Iterable<? extends String> keys) throws BulkCacheLoadingException, Exception {
        LOG.info("not support loadAll");
        return null;
    }

    public void write(String sessionid,User userinfo) throws Exception{
        LOG.info("WriteCache userid:" + userinfo.getUserid());
        collection.findOneAndReplace(eq("userid", userinfo.getUserid()), userinfo);
    }

    public void writeAll(Iterable<? extends Map.Entry<? extends String, ? extends User>> userinfos) throws BulkCacheWritingException, Exception{
        Iterator<? extends Map.Entry<? extends String, ? extends User>> it = userinfos.iterator();

        Map.Entry<? extends String, ? extends User> temp;
        while(it.hasNext()){
            temp = it.next();
            User userinfo = temp.getValue();
            collection.replaceOne(eq("userid", userinfo.getUserid()), userinfo);
        }
        
    }

    public void delete(String key) throws Exception{
        LOG.info("not support delete");
    }

    public void deleteAll(Iterable<? extends String> keys) throws BulkCacheWritingException, Exception {
        LOG.info("not support deleteAll");
    }
}