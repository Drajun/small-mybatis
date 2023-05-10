package cn.drajun.mybatis.reflection.property;

import java.util.Iterator;

/**
 * 属性分解器
 */
public class PropertyTokenizer implements Iterable<PropertyTokenizer>, Iterator<PropertyTokenizer> {

    private String name;
    private String indexedName;
    private String index;
    private String children;

    public PropertyTokenizer(String fullname){
        // 例如：班级[0].学生.成绩
        // name:班级   indexedName:班级[0]   index:0   children:学生.成绩
        // 找这个点 .
        int delim = fullname.indexOf(".");
        if(delim > -1){
            name = fullname.substring(0, delim);
            children = fullname.substring(delim + 1);
        }
        else{
            name = fullname;
            children = null;
        }
        // 找到[ ]中的数字
        indexedName = name;
        delim = name.indexOf('[');
        if(delim > -1){
            index = name.substring(delim+1, name.length()-1);
            name = name.substring(0, delim);
        }
    }

    @Override
    public Iterator<PropertyTokenizer> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return children != null;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove is not supported, as it has no meaning in the context of properties.");
    }

    // 取得下一个,非常简单，直接再通过儿子来new另外一个实例
    @Override
    public PropertyTokenizer next() {
        return new PropertyTokenizer(children);
    }

    public String getName() {
        return name;
    }

    public String getIndexedName() {
        return indexedName;
    }

    public String getIndex() {
        return index;
    }

    public String getChildren() {
        return children;
    }
}
