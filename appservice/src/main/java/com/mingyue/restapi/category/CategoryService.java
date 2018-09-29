package com.mingyue.restapi.category;

import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.mingyue.common.CacheHelper;
import com.mingyue.database.MongoDBUtil;
import com.mingyue.restapi.ReturnMessage;
import com.mingyue.restapi.goods.Goods;
import com.mingyue.restapi.goods.GoodsService;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

@Path("/restapi/category")
public class CategoryService {

    private static MongoCollection<Category> collection = MongoDBUtil.getDataBase().getCollection("category",
            Category.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage getAll() {

        FindIterable<Category> findIterable = collection.find().limit(5);
        List<Category> categoryList = MongoDBUtil.getListFromFindIterable(findIterable);

        for (Category category : categoryList) {
            boolean hasDeletedGoods = false; // 含有删除商品时未删除商品目录的数据
            List<String> goodsList = category.getGoodsList();
            Iterator<String> it = goodsList.iterator();
            while (it.hasNext()) {
                String code = it.next();
                Goods goods = CacheHelper.getGoodsCache().get(code);
                if (null == goods) {
                    it.remove();
                    hasDeletedGoods = true;
                }
            }

            if (hasDeletedGoods) {
                updateCategoryGoods(category);
            }
        }

        ReturnMessage result = new ReturnMessage();
        result.retParams.put("categoryList", categoryList);

        return result;
    }

    // 暂不提供新增的方法 就预置2个固定的目录
    // 更新目录中的商品列表 路径增加 admin用于防止不是从后台管理界面修改
    @POST
    @Path("/admin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage updateCategoryGoods(Category category) {
        collection.findOneAndReplace(eq("code", category.getCode()), category);

        ReturnMessage result = new ReturnMessage();
        result.retParams.put("category", category);

        return result;
    }

    @GET
    @Path("/goods")
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage getGoodsByCategory(@QueryParam("code") String code) {
        Category findcategory = collection.find(eq("code", code)).first();

        Map<String, Goods> goodsMap = GoodsService.getAllGoods(new HashSet<String>(findcategory.getGoodsList()));
        // 泛型方法
        List<Goods> goodsList = new ArrayList<Goods>(goodsMap.values());
            Iterator<Goods> it = goodsList.iterator();
            while (it.hasNext()) {
                Goods goods = it.next();
                if (null == goods || "D".equals(goods.getStatus()))
                {
                    it.remove();
                }
            }

        ReturnMessage result = new ReturnMessage();
        result.retParams.put("goodsList", goodsList);

        return result;
    }

}
