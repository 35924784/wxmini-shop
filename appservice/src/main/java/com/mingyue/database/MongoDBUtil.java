package com.mingyue.database;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import java.util.ArrayList;
import java.util.List;

import com.mingyue.common.ConfigUtil;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

public class MongoDBUtil{

    private static MongoClient client;
    private static MongoDatabase database ;

    //服务启动时调用初始化方法获取client
    static
    {          
        // create codec registry for POJOs
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(),fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        MongoClientURI uri = new MongoClientURI(ConfigUtil.getProperty("mogodb_url"));     
        client = new MongoClient(uri);
        database = client.getDatabase("test").withCodecRegistry(pojoCodecRegistry);
    }

    public static MongoClient getClient()
    {
        return client;
    }

    public static MongoDatabase getDataBase()
    {
        return database;
    }

    public static <T> List<T> getListFromFindIterable(FindIterable<T> findIterable){
        MongoCursor<T> cursor = findIterable.iterator();

        List<T> resultList = new ArrayList<T>();

        try {
            while (cursor.hasNext()) {
                resultList.add(cursor.next());
            }
        } finally {
            cursor.close();
        }

        return resultList;
    }
}