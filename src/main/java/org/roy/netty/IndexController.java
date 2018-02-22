package org.roy.netty;

/**
 * <p>Created: Feb 22, 2018 9:55:58 AM</p>
 * 
 * @author luoyh(Roy)
 * @version 1.0
 * @since 1.7
 */
@Rest("/api")
public class IndexController {
    
    @Rest("/index.do")
    public String index() {
        return "hello world: " + System.currentTimeMillis();
    }

    @Rest("/welcome")
    public R<String> wel(Integer id, String name) {
        if (null == id) {
            return R.<String>builder().code(R.ERR).msg("id must be not null").build();
        }
        return R.<String>builder().code(id).msg(name).build();
    }

}
