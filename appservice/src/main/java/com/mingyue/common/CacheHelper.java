package com.mingyue.common;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import com.mingyue.restapi.goods.Goods;
import com.mingyue.restapi.goods.GoodsService.GoodsLoaderWriter;
import com.mingyue.restapi.order.Order;
import com.mingyue.restapi.order.OrderFee;
import com.mingyue.restapi.user.User;
import com.mingyue.restapi.user.UserLoaderWriter;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.builders.WriteBehindConfigurationBuilder;

public class CacheHelper {
    private static CacheManager cacheManager;

    static {

        cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .withCache("imageCache",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, byte[].class,
                                ResourcePoolsBuilder.heap(20)))
                .withCache("thumbnailCache",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, byte[].class,
                                ResourcePoolsBuilder.heap(100)))
                .withCache("userinfoCache",
                                CacheConfigurationBuilder
                                        .newCacheConfigurationBuilder(String.class, User.class, ResourcePoolsBuilder.heap(1000))
                                        .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofHours(48))))
                .withCache("qrcodeCache",
                        CacheConfigurationBuilder
                                .newCacheConfigurationBuilder(String.class, byte[].class,
                                        ResourcePoolsBuilder.heap(200))
                                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofDays(3))))
                .withCache("orderFeeCache", // 缓存订单号对应的订单费用信息
                        CacheConfigurationBuilder
                                .newCacheConfigurationBuilder(String.class, OrderFee.class,
                                        ResourcePoolsBuilder.heap(200))
                                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofHours(6))))
                .withCache("orderRequestCache",    //缓存算费时提交的订单信息
                        CacheConfigurationBuilder
                                .newCacheConfigurationBuilder(String.class, Order.class,
                                        ResourcePoolsBuilder.heap(200))
                                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofHours(6))))
                .withCache("goodsCache", // 商品缓存  1天有效期  如果有问题了的就再次重新上下架来更新缓存
                                CacheConfigurationBuilder
                                        .newCacheConfigurationBuilder(String.class, Goods.class,
                                                ResourcePoolsBuilder.heap(50))
                                        .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofDays(1)))
                                        .withLoaderWriter(new GoodsLoaderWriter()))
                .withCache("usersessionCache",    //缓存Write-Behind
                                CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, User.class, ResourcePoolsBuilder.heap(100))
                                    .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofHours(1)))
                                    .withLoaderWriter(new UserLoaderWriter()) 
                                    .add(WriteBehindConfigurationBuilder 
                                        .newBatchedWriteBehindConfiguration(3, TimeUnit.MINUTES, 6)
                                        .queueSize(8)   //注意不能小于queueSize，否则无法初始化
                                        .concurrencyLevel(2) 
                                        .enableCoalescing()))
                .build(true);
    }

    public static CacheManager getCacheManager() {
        return cacheManager;
    }

    public static Cache<String, byte[]> getImageCache() {
        return cacheManager.getCache("imageCache", String.class, byte[].class);
    }

    public static Cache<String, byte[]> getThumbnailCache() {
        return cacheManager.getCache("thumbnailCache", String.class, byte[].class);
    }

    public static Cache<String, User> getUserSessionCache() {
        return cacheManager.getCache("usersessionCache", String.class, User.class);
    }

    public static Cache<String, User> getUserInfoCache() {
        return cacheManager.getCache("userinfoCache", String.class, User.class);
    }

    public static Cache<String, byte[]> getQrcodeCache() {
        return cacheManager.getCache("qrcodeCache", String.class, byte[].class);
    }

    public static Cache<String, OrderFee> getOrderFeeCache() {
        return cacheManager.getCache("orderFeeCache", String.class, OrderFee.class);
    }

    public static Cache<String, Order> getOrderRequestCache() {
        return cacheManager.getCache("orderRequestCache", String.class, Order.class);
    }

    public static Cache<String, Goods> getGoodsCache() {
        return cacheManager.getCache("goodsCache", String.class, Goods.class);
    }

}