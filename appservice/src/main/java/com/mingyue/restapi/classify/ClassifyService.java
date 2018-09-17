package com.mingyue.restapi.classify;

import static com.mongodb.client.model.Filters.eq;

import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.mingyue.database.MongoDBUtil;
import com.mingyue.restapi.ReturnMessage;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

@Path("/restapi/classify")
public class ClassifyService{

    private static MongoCollection<Classify> collection = MongoDBUtil.getDataBase().getCollection("classify", Classify.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage getAll(@QueryParam("pageSize") int pageSize,@QueryParam("curPage") int curPage) {        
        Long totalNum = collection.count();
        FindIterable<Classify> findIterable = collection.find().skip((curPage-1) * pageSize).limit(pageSize);
        List<Classify> classifyList = MongoDBUtil.getListFromFindIterable(findIterable);

        ReturnMessage result = new ReturnMessage();
        result.retParams.put("classifyList", classifyList);
        result.retParams.put("totalNum", totalNum);
        result.retParams.put("curPage", curPage);

        return result;
    }

    @POST
    @Path("/admin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage newClassify(Classify classify)
    {    
        String code = Long.toString(new Date().getTime());
        classify.setCode(code);   //取1970年以来毫秒数作为商品编码

        collection.insertOne(classify);

        ReturnMessage result = new ReturnMessage();
        result.retParams.put("code", code);
        return result;
    }

    @POST
    @Path("/admin/{code}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage updateClassify(Classify classify)
    {
        collection.findOneAndReplace(eq("code", classify.getCode()),classify);
        return new ReturnMessage();
    }

    @DELETE
    @Path("/admin/{code}")
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage deleteClassify(@PathParam("code") String code)
    {    
        collection.deleteOne(eq("code", code));
        return new ReturnMessage();
    }
}