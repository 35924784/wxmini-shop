package com.mingyue.restapi.goods;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Filters.regex;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.alibaba.fastjson.JSONObject;
import com.mingyue.common.CacheHelper;
import com.mingyue.common.DateHelper;
import com.mingyue.database.MongoDBUtil;
import com.mingyue.restapi.ReturnMessage;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.ehcache.spi.loaderwriter.BulkCacheLoadingException;
import org.ehcache.spi.loaderwriter.BulkCacheWritingException;
import org.ehcache.spi.loaderwriter.CacheLoaderWriter;

@Path("/restapi/goods")
public class GoodsService {

    private static Logger LOG = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    private static MongoCollection<Goods> collection = MongoDBUtil.getDataBase().getCollection("goods", Goods.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage getAll(@QueryParam("pageSize") int pageSize,@QueryParam("curPage") int curPage,
            @QueryParam("release") String onlyRelease) 
    {

        Long totalNum;
        FindIterable<Goods> findIterable;
        if("Y".equals(onlyRelease)){
            findIterable = collection.find(eq("status","R")).skip((curPage-1) * pageSize).limit(pageSize);
            totalNum = collection.count(eq("status","R"));
        }else {
            findIterable = collection.find().skip((curPage-1) * pageSize).limit(pageSize);
            totalNum = collection.count();
        }

        List<Goods> goodsList = MongoDBUtil.getListFromFindIterable(findIterable);

        ReturnMessage result = new ReturnMessage();
        result.retParams.put("goodsList", goodsList);
        result.retParams.put("totalNum", totalNum);
        result.retParams.put("curPage", curPage);

        return result;
    }

    @GET
    @Path("/{code}")
    @Produces(MediaType.APPLICATION_JSON)
    public Goods getGoods(@PathParam("code") String code) {
        //缓存
        return CacheHelper.getGoodsCache().get(code);
    }

    //批量获取商品信息
    public static Map<String,Goods> getAllGoods(Set<String> codeset) {
        //缓存
        return CacheHelper.getGoodsCache().getAll(codeset);
    }

    @GET
    @Path("/byclassify")
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage getGoodsByClassify(@QueryParam("classify") String classify,
            @QueryParam("pageSize") int pageSize, @QueryParam("curPage") int curPage,@QueryParam("release") String onlyRelease) {

        Long totalNum;
        FindIterable<Goods> findIterable;
        if("Y".equals(onlyRelease)){
            findIterable = collection.find(and(eq("classify",classify),eq("status","R"))).skip((curPage-1) * pageSize).limit(pageSize);
            totalNum = collection.count(and(eq("classify",classify),eq("status","R")));
        }else {
            findIterable = collection.find(eq("classify",classify)).skip((curPage-1) * pageSize).limit(pageSize);
            totalNum = collection.count(eq("classify",classify));
        }

        List<Goods> goodsList = MongoDBUtil.getListFromFindIterable(findIterable);

        ReturnMessage result = new ReturnMessage();
        result.retParams.put("goodsList", goodsList);
        result.retParams.put("totalNum", totalNum);
        result.retParams.put("curPage", curPage);

        return result;
    }

    @POST
    @Path("/admin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage newGoods(Goods goods)
    {
        String time = DateHelper.getDateString("yyMMddhhmmss");
        goods.setCode(time);   //取时间作为商品编码

        collection.insertOne(goods);

        ReturnMessage result = new ReturnMessage();
        result.retParams.put("code", time);
        return result;
    }

    @POST
    @Path("/admin/update")
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage updateGoods(Goods goods)
    {   
        collection.findOneAndReplace(eq("code", goods.getCode()), goods);
        CacheHelper.getGoodsCache().put(goods.getCode(), goods);
        return new ReturnMessage();
    }

    @POST
    @Path("/admin/{code}/status")
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage changeStatus(@PathParam("code") String code, String status)
    {
        Document update = new Document();
        update.put("status", status);

        collection.updateOne(eq("code", code),new Document("$set", update));
        //重新加载缓存
        CacheHelper.getGoodsCache().remove(code);
        CacheHelper.getGoodsCache().get(code);
        return new ReturnMessage();
    }

    @POST
    @Path("/admin/{code}/images")
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage updateGoodsImages(@PathParam("code") String code, List<String> images)
    {        
        Document update = new Document();
        update.put("images", images);
        collection.updateOne(eq("code", code),new Document("$set", update));

        return new ReturnMessage();
    }

    @POST
    @Path("/admin/{code}/detailimgs")
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage updateGoodsDetailImages(@PathParam("code") String code, List<String> detailImgs)
    {        
        Document update = new Document();
        update.put("detailImgs", detailImgs);
        collection.updateOne(eq("code", code),new Document("$set", update));

        return new ReturnMessage();
    }

    @DELETE
    @Path("/admin/{code}")
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage delGoods(@PathParam("code") String code)
    {
        collection.deleteOne(eq("code", code));
        CacheHelper.getGoodsCache().remove(code);
        return new ReturnMessage();
    }

    //查询暂不支持分页 限制最多返回20条 pageSize和curPage 暂无用
    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage searchGoods(@QueryParam("keyword") String keyword,@QueryParam("pageSize") int pageSize,@QueryParam("curPage") int curPage)
    {
        try {
			keyword = URLDecoder.decode(keyword, "utf-8");
		} catch (UnsupportedEncodingException e) {
			LOG.error(e.getMessage());
		}
        //根据名称进行模糊查询       
        Pattern pattern = Pattern.compile("^" + keyword + ".*$", Pattern.CASE_INSENSITIVE);
        // 根据name like keyword%查询
        FindIterable<Goods> findIterable = collection.find(regex("name",pattern)).limit(20);
        LOG.info(" select * from user where name like " + keyword);
        Long totalNum = collection.count(regex("name",pattern));
        
        List<Goods> goodsList = MongoDBUtil.getListFromFindIterable(findIterable);

        ReturnMessage result = new ReturnMessage();
        result.retParams.put("goodsList", goodsList);
        result.retParams.put("totalNum", totalNum);
        result.retParams.put("curPage", curPage);

        return result;
    }


    public static class GoodsLoaderWriter implements CacheLoaderWriter<String, Goods>{
    
        public Goods load(String code) throws Exception{
            LOG.info("Load Goods Cache code:" + code);
            return collection.find(eq("code", code)).first();
        }
    
        public Map<String, Goods> loadAll(Iterable<? extends String> keys) throws BulkCacheLoadingException, Exception {
            LOG.info("Begin loadAll Goods:"+ JSONObject.toJSONString(keys));

            FindIterable<Goods> findIterable = collection.find(in("code", keys));

            Map<String,Goods> goodsInfoMap = new HashMap<String,Goods>();
            MongoCursor<Goods> d_cursor = findIterable.iterator();
            try {
                while (d_cursor.hasNext()) {
                    Goods goods = d_cursor.next();
                    goodsInfoMap.put(goods.getCode(), goods);
                }
            } finally {
                d_cursor.close();
            }

            return goodsInfoMap;
        }
    
        public void write(String code,Goods theGoods) throws Exception{
            //不需要
        }
    
        public void writeAll(Iterable<? extends Map.Entry<? extends String, ? extends Goods>> userinfos) throws BulkCacheWritingException, Exception{
            //不需要
        }
    
        public void delete(String key) throws Exception{
            //不需要
        }
    
        public void deleteAll(Iterable<? extends String> keys) throws BulkCacheWritingException, Exception {
            //不需要
        }
    }
}