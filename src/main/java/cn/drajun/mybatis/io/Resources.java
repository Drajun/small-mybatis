package cn.drajun.mybatis.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * 通过类加载器获得resource的辅助类
 * classloader可以获取classes路径下的配置文件，
 * 而resources编译后就在classes目录下，通过classloader可以加载后可以执行获取里面的值。
 * 直接在input stream里传file参数，需要确定file的绝对路径，对于变动的环境不太适应
 */
public class Resources {

    public static Reader getResourceAsReader(String resource) throws IOException{
        return new InputStreamReader(getResourceAsStream(resource));
    }

    private static InputStream getResourceAsStream(String resource) throws IOException{
        ClassLoader[] classLoaders = getClassLoaders();
        for(ClassLoader classLoader : classLoaders){
            InputStream inputStream = classLoader.getResourceAsStream(resource);
            if(null != inputStream){
                return inputStream;
            }
        }
        throw new IOException("Could not find resource " + resource);
    }

    private static ClassLoader[] getClassLoaders(){
        return new ClassLoader[]{
                ClassLoader.getSystemClassLoader(),
                Thread.currentThread().getContextClassLoader()
        };
    }

    /**
     * 加载一个类
     * @param className
     * @return
     * @throws ClassNotFoundException
     */
    public static Class<?> classForName(String className) throws ClassNotFoundException{
        return Class.forName(className);
    }

}
