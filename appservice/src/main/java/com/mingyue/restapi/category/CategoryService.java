package com.mingyue.restapi.category;

import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.mingyue.database.MongoDBUtil;
import com.mingyue.restapi.ReturnMessage;
import com.mingyue.restapi.goods.Goods;
import com.mingyue.restapi.goods.GoodsService;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

@Path("/restapi/category")
public class CategoryService {

    private static MongoCollection<Category> collection = MongoDBUtil.getDataBase().getCollection("category", Category.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage getAll() 
    {

        FindIterable<Category> findIterable = collection.find().limit(5);
        List<Category> categoryList = MongoDBUtil.getListFromFindIterable(findIterable);

        ReturnMessage result = new ReturnMessage();
        result.retParams.put("categoryList", categoryList);

        return result;
    }

    //暂不提供新增的方法 就预置2个固定的目录
    //更新目录中的商品列表  路径增加 admin用于防止不是从后台管理界面修改
    @POST
    @Path("/admin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage updateCategoryGoods(Category category)
    {      
        collection.findOneAndReplace(eq("code", category.getCode()), category);

        ReturnMessage result = new ReturnMessage();
        result.retParams.put("category", category);

        return result;
    }

    @GET
    @Path("/goods")
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage getGoodsByCategory(@QueryParam("code") String code) 
    {
        Category findcategory = collection.find(eq("code", code)).first();

        Map<String,Goods> goodsMap = GoodsService.getAllGoods(new HashSet<String>(findcategory.getGoodsList()));
        //泛型方法
        List<Goods> goodsList = new ArrayList<Goods>(goodsMap.values());

        ReturnMessage result = new ReturnMessage();
        result.retParams.put("goodsList", goodsList);

        return result;
    }

}
