package com.mingyue.restapi.order;

import java.util.Date;
import java.util.List;

import com.mingyue.restapi.address.Address;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Order {
    private String orderid;         //订单号

    private String userid;          //用户id

    private List<OrderItem> orderItemList;   //订单项列表

    private int totalPrice;      //订单总价 

    private Address addrinfo;       //直接保存地址信息 其中一些字段多余，但是这样简单些

    private String logisticCompany; //物流公司 顺丰/圆通等

    private String logisticNumber;  //物流单号

    private String remark;          //备注

    private boolean evaluated;      //是否已评价

    private boolean hasPaid;        //是否已付费  另外表保存付费信息

    private String refundStatus;    //退款状态 1:发起退款 2：退款成功 3：退款失败

    private String status;          //状态  已取消等

    private Date statusDate;   //状态于何时发生变化

    private Date createDate;  //订单创建时间

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public String getOrderid() {
        return this.orderid;
    }

    public String getUserid(){
        return userid;
    }
    
    public void setUserid(String userid){
        this.userid = userid;
    }

    public void setOrderItemList(List<OrderItem> orderItemList) {
        this.orderItemList = orderItemList;
    }

    public List<OrderItem> getOrderItemList() {
        return this.orderItemList;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public int getTotalPrice() {
        return this.totalPrice;
    }

    public void setAddrinfo(Address addrinfo) {
        this.addrinfo = addrinfo;
    }

    public Address getAddrinfo() {
        return this.addrinfo;
    }
    
    public void setLogisticCompany(String logisticCompany) {
        this.logisticCompany = logisticCompany;
    }

    public String getLogisticCompany() {
        return this.logisticCompany;
    }

    public void setLogisticNumber(String logisticNumber) {
        this.logisticNumber = logisticNumber;
    }

    public String getLogisticNumber() {
        return this.logisticNumber;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getRemark() {
        return this.remark;
    }

    public void setEvaluated(boolean evaluated) {
        this.evaluated = evaluated;
    }

    public boolean getEvaluated() {
        return this.evaluated;
    }

    public void setHasPaid(boolean hasPaid) {
        this.hasPaid = hasPaid;
    }

    public boolean getHasPaid() {
        return this.hasPaid;
    }

    public void setRefundStatus(String refundStatus) {
        this.refundStatus = refundStatus;
    }

    public String getRefundStatus() {
        return this.refundStatus;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatusDate(Date statusDate) {
        this.statusDate = statusDate;
    }

    public Date getStatusDate() {
        return this.statusDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getCreateDate() {
        return this.createDate;
    }
    
}