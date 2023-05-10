package cn.drajun.mybatis.reflection.invoker;

/**
 * 调用者接口
 */
public interface Invoker {

    // target对象调用了某个方法, args是该方法的参数
    Object invoke(Object target, Object[] args) throws Exception;

    Class<?> getType();
}
