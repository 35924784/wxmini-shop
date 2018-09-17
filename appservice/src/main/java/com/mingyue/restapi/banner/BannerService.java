package com.mingyue.restapi.banner;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.exclude;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.mingyue.database.MongoDBUtil;
import com.mingyue.restapi.ReturnMessage;
import com.mingyue.restapi.image.Image;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

@Path("/restapi/banner")
public class BannerService{

    private static MongoCollection<Image> collection = MongoDBUtil.getDataBase().getCollection("image",Image.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage getBanners() {

        FindIterable<Image> findIterable = collection.find(eq("isbanner", true)).projection(exclude("fileData")).limit(5);

        List<Image> bannerList = MongoDBUtil.getListFromFindIterable(findIterable);

        ReturnMessage result = new ReturnMessage();
        result.retParams.put("bannerList", bannerList);
        return result;
    }
}