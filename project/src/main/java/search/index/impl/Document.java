package search.index.impl;

import search.index.AbstractDocument;
import search.index.AbstractTermTuple;

import java.util.ArrayList;
import java.util.List;

/**
 * AbstractDocument的具体实现类
 */
public class Document extends AbstractDocument {
    /**
     * 缺省构造函数
     */
    public Document() {
        this.docId = -1;
        this.docPath = "unknown";
        this.tuples = new ArrayList<>();
    }

    /**
     * 构造函数
     *
     * @param docId：文档id
     * @param docPath：文档绝对路径
     */
    public Document(int docId, String docPath) {
        this.docId = docId;
        this.docPath = docPath;
        this.tuples = new ArrayList<>();
    }

    /**
     * 构造函数
     *
     * @param docId:   文档id
     * @param docPath: 文档绝对路径
     * @param tuples:  三元组列表
     */
    public Document(int docId, String docPath, List<AbstractTermTuple> tuples) {
        this.docId = docId;
        this.docPath = docPath;
        this.tuples = tuples;
    }

    /**
     * 获取文档id
     *
     * @return ：文档id
     */
    @Override
    public int getDocId() {
        return docId;
    }

    /**
     * 设置文档id
     *
     * @param docId：文档id
     */
    @Override
    public void setDocId(int docId) {
        this.docId = docId;
    }

    /**
     * 获取文档绝对路径
     *
     * @return ：文档绝对路径
     */
    @Override
    public String getDocPath() {
        return docPath;
    }

    /**
     * 设置文档绝对路径
     *
     * @param docPath ：文档绝对路径
     */
    @Override
    public void setDocPath(String docPath) {
        this.docPath = docPath;
    }

    /**
     * 获得文档的三元组列表
     *
     * @return ：文档的三元组列表
     */
    @Override
    public List<AbstractTermTuple> getTuples() {
        return tuples;
    }

    /**
     * 向三元组列表添加三元组, 要求不能有内容重复的三元组
     *
     * @param tuple ：要添加的三元组
     */
    @Override
    public void addTuple(AbstractTermTuple tuple) {
        if (!tuples.contains(tuple))
            tuples.add(tuple);
    }

    /**
     * 判断是否包含指定的三元组
     *
     * @param tuple ： 指定的三元组
     * @return ： 如果包含指定的三元组，返回true;否则返回false
     */
    @Override
    public boolean contains(AbstractTermTuple tuple) {
        return tuples.contains(tuple);
    }

    /**
     * 获得指定下标位置的三元组
     *
     * @param index：指定下标位置
     * @return ： 三元组
     */
    @Override
    public AbstractTermTuple getTuple(int index) {
        return tuples.get(index);
    }

    /**
     * 返回文档对象包含的三元组的个数
     *
     * @return ：文档对象包含的三元组的个数
     */
    @Override
    public int getTupleSize() {
        return tuples.size();
    }

    /**
     * 获得Document的字符串表示
     *
     * @return ： Document的字符串表示
     */
    @Override
    public String toString() {
        StringBuffer string = new StringBuffer("docId: " + docId + "\ndocPath: " + docPath + "\ntuples:\n");
        for (AbstractTermTuple tuple : tuples) {
            string.append("\t").append(tuple).append("\n");
        }
        return string.toString();
    }
}
