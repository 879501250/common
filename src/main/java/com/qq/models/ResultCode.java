package com.qq.models;

//定义一个返回的状态码枚举类
public enum ResultCode {
    //1.提供当前枚举类的对象，多个对象之间用","隔开，末尾对象";"结束
        /**
         * 操作成功
         */
        SUCCESS(200,"操作成功"),
        /**
         * 参数校验错误
         */
        PARAMETER_CHECK_ERROR(400, "参数校验错误"),
        UNLOGIN_ERROR(401, "用户未登录或登录状态超时失效"),
        AUTH_VALID_ERROR(403, "用户权限不足"),
        METHOD_NOT_ALLOWED(405, "访问方式不正确，GET请求使用POST方式访问"),
        SYSTEM_ERROR(500, "系统错误"),
        /**
         * 业务异常  不同的业务状态码可以向下顺延
         */
        BUSINESS_ERROR(600, "业务异常");

    //2.声明Season对象的属性:private final修饰
    private final Integer code;
    private final String message;

    //3.构造器,给对象属性赋值（默认是私有的）
    private ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    //4.其他诉求1：获取枚举类对象的属性
    public String getMessage() {
        return message;
    }

    public Integer getCode() {
        return code;
    }
}
