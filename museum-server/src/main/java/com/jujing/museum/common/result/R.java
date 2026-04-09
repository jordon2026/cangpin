package com.jujing.museum.common.result;

import lombok.Data;
import lombok.experimental.Accessors;
import java.io.Serializable;

/**
 * 统一响应体
 */
@Data
@Accessors(chain = true)
public class R<T> implements Serializable {

    private int code;
    private String msg;
    private T data;

    public static <T> R<T> ok() {
        return new R<T>().setCode(200).setMsg("success");
    }

    public static <T> R<T> ok(T data) {
        return new R<T>().setCode(200).setMsg("success").setData(data);
    }

    public static <T> R<T> ok(String msg, T data) {
        return new R<T>().setCode(200).setMsg(msg).setData(data);
    }

    public static <T> R<T> fail() {
        return new R<T>().setCode(500).setMsg("操作失败");
    }

    public static <T> R<T> fail(String msg) {
        return new R<T>().setCode(500).setMsg(msg);
    }

    public static <T> R<T> fail(int code, String msg) {
        return new R<T>().setCode(code).setMsg(msg);
    }

    public static <T> R<T> unauthorized(String msg) {
        return new R<T>().setCode(401).setMsg(msg);
    }

    public static <T> R<T> forbidden(String msg) {
        return new R<T>().setCode(403).setMsg(msg);
    }
}
