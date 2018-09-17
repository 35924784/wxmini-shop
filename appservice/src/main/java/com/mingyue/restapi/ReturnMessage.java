package com.mingyue.restapi;

import java.util.HashMap;
import java.util.Map;

public class ReturnMessage{
    public String retCode;
    public String retMessage;
    public Map<String,Object> retParams;

    public ReturnMessage()
    {
        retCode = "0";
        retMessage = "成功";
        retParams = new HashMap<String,Object>();
    }

    public ReturnMessage(String retCode, String retMessage)
    {
        this.retCode = retCode;
        this.retMessage = retMessage;
        retParams = new HashMap<String,Object>();
    }
}