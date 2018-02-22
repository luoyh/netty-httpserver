package org.roy.netty;

/**
 * <p>
 * Created: Feb 22, 2018 2:01:36 PM
 * </p>
 * 
 * @author luoyh(Roy)
 * @version 1.0
 * @since 1.7
 */
public class R<T> {

    public static final int OK = 200;
    public static final int ERR = 300;

    private int code;
    private String msg;
    private T data;
    
    public static <T> Builder<T> builder() {
        return new Builder<T>();
    }
    
    public static class Builder<T> {
        
        private int code;
        private String msg;
        private T data;
        
        public Builder<T> code(int code) {
            this.code = code;
            return this;
        }
        
        public Builder<T> msg(String msg) {
            this.msg = msg;
            return this;
        }
        
        public Builder<T> data(T data) {
            this.data = data;
            return this;
        }
        
        public R<T> build() {
            R<T> r = x(code, data);
            r.setMsg(msg);
            return r;
        }
    }
    
    private static <T> R<T> x(int code, T data) {
        R<T> r = new R<T>();
        r.setCode(code);
        r.setData(data);
        return r;
    }

    public static <T> R<T> ok() {
        return ok(null);
    }
    
    public static <T> R<T> ok(T data) {
        return x(OK, data);
    }
    
    public static <T> R<T> err() {
        return err(null);
    }
    
    public static <T> R<T> err(T data) {
        return x(ERR, data);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

}
