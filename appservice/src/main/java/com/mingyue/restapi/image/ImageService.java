package com.mingyue.restapi.image;

import static com.mongodb.client.model.Filters.all;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.exclude;

//import java.io.File;
import java.io.IOException;
import java.io.InputStream;
//import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
//import javax.ws.rs.core.MultivaluedMap;

import com.mingyue.common.ValidateUtils;
import com.mingyue.database.MongoDBUtil;
import com.mingyue.restapi.ReturnMessage;
//import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;

//import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.jboss.resteasy.annotations.cache.Cache;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

@Path("/restapi/image")
public class ImageService{

    private static Logger LOG = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    private static MongoCollection<Image> collection = MongoDBUtil.getDataBase().getCollection("image", Image.class);

    @POST 
    @Path("/uploadfile")
    @Consumes("multipart/form-data")
    @Produces("application/json;charset=UTF-8")
    public ReturnMessage uploadFile(MultipartFormDataInput input)
    {
        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        List<InputPart> inputParts = uploadForm.get("file");
        List<InputPart> encodefileNameParts = uploadForm.get("encodefilename");
        LOG.error("Upload Begin");

        String fileName = "unknow";
        //为了获取URL编码后的文件名  通过 formdata 中 encodefilename 参数 传过来
        if (ValidateUtils.isNotEmptyCollection(encodefileNameParts))
        {
            for (InputPart inputPart : encodefileNameParts) {
                try {
                    fileName = inputPart.getBodyAsString();
                    fileName = URLDecoder.decode(fileName, "utf-8");
                } catch (Exception e) {
                    e.printStackTrace();
                    fileName = "exception";
                }
            }
        }
        

        String fileid= "";

        for (InputPart inputPart : inputParts) {
            try {
                LOG.error("filename:" + fileName);
                //convert the uploaded file to inputstream
                InputStream inputStream = inputPart.getBody(InputStream.class,null);
                byte [] bytes = IOUtils.toByteArray(inputStream);

                /*
                File file = new File(fileName);
                file.createNewFile();
                FileUtils.writeByteArrayToFile(file, bytes);
                */

                Image image = new Image(fileName,bytes);

                MongoDatabase database = MongoDBUtil.getDataBase();
                final MongoCollection<Image> collection = database.getCollection("image", Image.class);
                collection.insertOne(image);

                fileid = image.getFileid();

                LOG.error("Upload Done");
    
              } catch (IOException e) {
                e.printStackTrace();
              }
        }

        ReturnMessage result = new ReturnMessage();
        result.retParams.put("fileid", fileid);
        return result;
    }

    //原本通过获取formdata默认传入的文件名,但是RestEasy使用的Mime4J并不支持UTF-8解析文件名。因此要么文件名
    //传encodeURI转码过的，要么通过另一个参数传进来
    /*
    private String getFileName(MultivaluedMap<String, String> headers) {
        String[] contentDisposition = headers.getFirst("Content-Disposition").split(";");

        for (String filename : contentDisposition) {
            
            if ((filename.trim().startsWith("filename"))) {
                
                String[] name = filename.split("=");
                String finalFileName = name[1].trim().replaceAll("\"", "");
                try {
                    finalFileName = URLDecoder.decode(finalFileName, "utf-8");
                    return finalFileName;
				} catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return "exception";
				}
            }
        }
        return "unknown";
    }
    */

    //返回图片详情
    @GET 
    @Path("/{fileid}")
    @Cache(maxAge = 1800)
    @Produces("image/jpeg,image/png")
    public byte[] getImageData(@PathParam("fileid") String fileid) throws IOException
    {

        Image theIamge = collection.find(eq("fileid", fileid)).first();
        return theIamge.getFileData();
    }

    //返回所有image 除了图片内容
    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage getImages(@QueryParam("pageSize") int pageSize,@QueryParam("curPage") int curPage) throws IOException
    {

        FindIterable<Image>  findIterable = collection.find().projection(exclude("fileData"))
                            .skip((curPage-1) * pageSize).limit(pageSize);

        List<Image> images = MongoDBUtil.getListFromFindIterable(findIterable);
        Long totalNum = collection.count();
        
        ReturnMessage result = new ReturnMessage();
        result.retParams.put("images", images);
        result.retParams.put("totalNum", totalNum);
        return result;
    }

    @DELETE
    @Path("/admin/{fileid}")
    @Produces("application/json;charset=UTF-8")
    public ReturnMessage deleteImage(@PathParam("fileid") String fileid) throws IOException
    {
        MongoDatabase database = MongoDBUtil.getDataBase();
        Document doc = database.getCollection("goods").find(all("images",Arrays.asList(fileid))).first();
        if(doc != null)
        {
            return new ReturnMessage("999","图片被商品使用中，请先删除相关商品：" + doc.getString("code") + " " + doc.getString("name"));
        }

        DeleteResult delresult = collection.deleteOne(eq("fileid", fileid));
        ReturnMessage result = new ReturnMessage();
        result.retParams.put("fileid", fileid);

        if (!delresult.wasAcknowledged()){
            result = new ReturnMessage("1","删除失败");       
        }
        
        return result;
    }

    @POST 
    @Path("/admin/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ReturnMessage updateImage(Map<String,String> request)
    {
        String fileid = request.get("fileid");
        String isbanner = request.get("isbanner");
        String fileName = request.get("fileName");
        
        Document update = new Document();
        update.put("isbanner", isbanner.equals("Y"));
        update.put("fileName", fileName);

        collection.updateOne(eq("fileid", fileid),new Document("$set", update));

        return new ReturnMessage();
    }

}