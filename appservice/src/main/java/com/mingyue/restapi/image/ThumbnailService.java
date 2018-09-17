package com.mingyue.restapi.image;

import static com.mongodb.client.model.Filters.eq;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.mingyue.common.CacheHelper;
import com.mingyue.database.MongoDBUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.resteasy.annotations.cache.Cache;

import net.coobird.thumbnailator.Thumbnails;

@Path("/restapi/thumbnail")
public class ThumbnailService{

    private static Logger LOG = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    //获取图片的缩略图 因为某些上传的图片可能有点大 微信小程序需要返回缩略图 
    //主要时间应该是浪费在从MongoDB查询及生成缩略图 并且使用缓存加速
    //https://github.com/coobird/thumbnailator 使用thumbnailator
    @GET 
    @Path("/{fileid}")
    @Cache(maxAge = 1800) 
    @Produces("image/jpeg,image/png")
    public byte[] getImage(@PathParam("fileid") String fileid) throws IOException {
        fileid = fileid.replace(".jpg", "");

        org.ehcache.Cache<String,byte[]> thumbnailCache = CacheHelper.getThumbnailCache();

        if (thumbnailCache.get(fileid) != null)
        {
            LOG.info("return from cache.");
            return thumbnailCache.get(fileid);
        }

        MongoDatabase database = MongoDBUtil.getDataBase();
        final MongoCollection<Image> collection = database.getCollection("image", Image.class);
        Image theIamge = collection.find(eq("fileid", fileid)).first();

        ByteArrayInputStream bais = new ByteArrayInputStream(theIamge.getFileData());
        try {
            BufferedImage img = ImageIO.read(bais);

            //如果图片不是很大就直接返回算了
            if (img.getWidth() <= 400 || img.getHeight() <= 300 
                    || theIamge.getFileData().length <= 250 * 1024)
            {
                LOG.error("return original image.");
                thumbnailCache.put(fileid,theIamge.getFileData());
                return theIamge.getFileData();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Thumbnails.of(img).size(400, 300).outputFormat("jpg").toOutputStream(baos);
            byte[] bytes = baos.toByteArray();

            thumbnailCache.put(fileid,bytes);
            return bytes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}