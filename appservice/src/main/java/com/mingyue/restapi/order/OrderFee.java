package com.mingyue.restapi.order;

//订单费用详情 所有费用 单位 分
public class OrderFee{
    private String orderid;     //订单号

    private String prepayid;    //微信预付款id 用于发送通知

    private int totalPrice;     //订单总价

    private int originPrice;    //订单项原始总价

    private int discount;       //折扣  -- 暂不使用

    private int discountAmount; //折扣优惠费用

    private int waiveFee;       //减免费用 营销活动如砍价减免的费用

    private int shippingFee;    //物流费 基础快递费10元  满多少才物流费 或者同城免物流费

    private int score;          //订单中使用积分的部分

    private int refundfee;      //退款金额

    public OrderFee(){
    }

    public OrderFee(String orderid){
        this.orderid = orderid;
        this.prepayid = null;
        this.totalPrice = 0;
        this.originPrice = 0;
        this.discount = 0;
        this.discountAmount = 0;
        this.waiveFee = 0;
        this.score = 0;
        this.shippingFee = 10;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public String getOrderid() {
        return this.orderid;
    }

    public void setPrepayid(String prepayid) {
        this.prepayid = prepayid;
    }

    public String getPrepayid() {
        return this.prepayid;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public int getTotalPrice() {
        return this.totalPrice;
    }

    public void setOriginPrice(int originPrice) {
        this.originPrice = originPrice;
    }

    public int getOriginPrice() {
        return this.originPrice;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public int getDiscount() {
        return this.discount;
    }

    public void setDiscountAmount(int discountAmount) {
        this.discountAmount = discountAmount;
    }

    public int getDiscountAmount() {
        return this.discountAmount;
    }

    public void setShippingFee(int shippingFee) {
        this.shippingFee = shippingFee;
    }

    public int getShippingFee() {
        return this.shippingFee;
    }

    public void setWaiveFee(int waiveFee) {
        this.waiveFee = waiveFee;
    }

    public int getWaiveFee() {
        return this.waiveFee;
    }

    public int getScore(){
        return score;
    }
    
    public void setScore(int score){
        this.score = score;
    }

    public int getRefundfee(){
        return refundfee;
    }
    
    public void setRefundfee(int refundfee){
        this.refundfee = refundfee;
    }
}