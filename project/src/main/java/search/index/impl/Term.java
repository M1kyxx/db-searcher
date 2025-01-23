package search.index.impl;

import search.index.AbstractTerm;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * AbstractTerm的具体实现类
 */
public class Term extends AbstractTerm {

    /**
     * 缺省构造函数
     */
    public Term() {
        content = "";
    }

    /**
     * 构造函数
     *
     * @param content ：Term内容
     */
    public Term(String content) {
        this.content = content;
    }

    /**
     * 判断两个Term内容是否相同
     *
     * @param obj ：要比较的另外一个Term
     * @return 如果内容相等返回true，否则返回false
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Term) {
            Term term = (Term) obj;
            return content.equals(term.content);
        }
        return false;
    }

    /**
     * 返回Term的字符串表示
     *
     * @return 字符串
     */
    @Override
    public String toString() {
        return content;
    }

    /**
     * 获取content内容
     *
     * @return content内容
     */
    @Override
    public String getContent() {
        return content;
    }

    /**
     * 设置content内容
     *
     * @param content：content内容
     */
    @Override
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 按字典序比较两个Term大小
     *
     * @param o ： 要比较的Term对象
     * @return ： 返回二个Term对象的字典序差值
     */
    @Override
    public int compareTo(AbstractTerm o) {
        return content.compareTo(o.getContent());
    }

    /**
     * 将内容序列化写入二进制文件
     *
     * @param out :输出流对象
     */
    @Override
    public void writeObject(ObjectOutputStream out) {
        try {
            // 将this对象的成员依次序列化
            out.writeObject(this.content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将二进制文件反序列化写入内存
     *
     * @param in ：输入流对象
     */
    @Override
    public void readObject(ObjectInputStream in) {
        // 将this对象的成员依次反序列化，注意和序列化次序一致
        try {
            this.content = (String) (in.readObject());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
