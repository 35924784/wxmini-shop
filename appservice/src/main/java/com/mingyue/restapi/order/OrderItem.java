package com.mingyue.restapi.order;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

//订单项  多个订单项组成订单  加入购物车后生成购物车里的订单项
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderItem {
    private String userid;      //订单项所属用户id
    private String itemid;      //订单项编号
    private String goodscode;   //商品编码
    private String goodsname;   //商品名称
    private int price;          //单价  保存购物车订单项时，不保存单价 但是最终生成订单的时候需要
    private String spec;       //规格编码
    private int amount;         //数量
    private int totalPrice;  //实际总价
    private Date createDate;    //创建时间
    private String status;      //0表示在购物车

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUserid() {
        return this.userid;
    }

    public void setItemid(String itemid) {
        this.itemid = itemid;
    }

    public String getItemid() {
        return this.itemid;
    }

    public void setGoodscode(String goodscode) {
        this.goodscode = goodscode;
    }

    public String getGoodscode() {
        return this.goodscode;
    }

    public void setGoodsname(String goodsname) {
        this.goodsname = goodsname;
    }

    public String getGoodsname() {
        return this.goodsname;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getPrice() {
        return this.price;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public String getSpec() {
        return this.spec;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return this.amount;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public int getTotalPrice() {
        return this.totalPrice;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getCreateDate() {
        return this.createDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }
}