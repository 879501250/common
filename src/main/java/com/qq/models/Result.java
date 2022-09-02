package com.qq.models;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

//定义一个统一返回的数据格式
@Data
public class Result {
    //是否成功
    private Boolean success;
    //返回状态码
    private int code;
    //返回消息
    private String message;
    //返回数据
    private Map<String, Object> data = new HashMap<String, Object>();

    //私有化构造器
    private Result() {
    }

    //成功的静态方法
    public static Result success() {
        Result result = new Result();
        result.setSuccess(true);
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage("成功~");
        return result;
    }

    //失败的静态方法
    public static Result error() {
        Result result = new Result();
        result.setSuccess(false);
        result.setCode(ResultCode.SYSTEM_ERROR.getCode());
        result.setMessage("失败~");
        return result;
    }

    /**
     * 以下方法都是return this，返回调用方法的对象本身，方便链式编程
     * r.success.message
     */
    public Result success(Boolean success){
        this.setSuccess(success);
        return this;
    }
    public Result message(String message){
        this.setMessage(message);
        return this;
    }
    public Result code(Integer code){
        this.setCode(code);
        return this;
    }
    public Result data(String key, Object value){
        this.data.put(key, value);
        return this;
    }
    public Result data(Map<String, Object> map){
        this.setData(map);
        return this;
    }
}
