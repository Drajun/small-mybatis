package cn.drajun.mybatis.session;

/**
 * 结果上下文
 */
public interface ResultContext {

    /**
     * 获取结果
     * @return
     */
    Object getResultObject();

    /**
     * 获取记录数
     * @return
     */
    int getResultCount();
}
