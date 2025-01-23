package search.index.impl;

import search.index.AbstractPosting;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

/**
 * AbstractPosting的具体实现类
 */
public class Posting extends AbstractPosting {
    /**
     * 缺省构造函数
     */
    public Posting() {
    }

    /**
     * 构造函数
     *
     * @param docId  ：包含单词的文档id
     * @param curPos ：单词在文档里出现的位置
     */
    public Posting(int docId, int curPos) {
        this.docId = docId;
        this.freq = 1;
        positions.add(curPos);
    }

    /**
     * 构造函数
     *
     * @param docId     ：包含单词的文档id
     * @param freq      ：单词在文档里出现的次数
     * @param positions ：单词在文档里出现的位置
     */
    public Posting(int docId, int freq, List<Integer> positions) {
        super(docId, freq, positions);
    }

    /**
     * 判断二个Posting内容是否相同
     *
     * @param obj ：要比较的另外一个Posting
     * @return 如果内容相等返回true，否则返回false
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Posting) {
            Posting posting = (Posting) obj;
            return docId == posting.docId && freq == posting.freq && positions.containsAll(posting.positions) && posting.positions.containsAll(positions);
        }
        return false;
    }

    /**
     * 返回Posting的字符串表示
     *
     * @return 字符串
     */
    @Override
    public String toString() {
        return "{docId: " + docId + ", freq: " + freq + ", positions: " + positions + "}";
    }

    /**
     * 返回包含单词的文档id
     *
     * @return ：文档id
     */
    @Override
    public int getDocId() {
        return docId;
    }

    /**
     * 设置包含单词的文档id
     *
     * @param docId：包含单词的文档id
     */
    @Override
    public void setDocId(int docId) {
        this.docId = docId;
    }

    /**
     * 返回单词在文档里出现的次数
     *
     * @return ：出现次数
     */
    @Override
    public int getFreq() {
        return freq;
    }

    /**
     * 设置单词在文档里出现的次数
     *
     * @param freq:单词在文档里出现的次数
     */
    @Override
    public void setFreq(int freq) {
        this.freq = freq;
    }

    /**
     * 返回单词在文档里出现的位置列表
     *
     * @return ：位置列表
     */
    @Override
    public List<Integer> getPositions() {
        return positions;
    }

    /**
     * 设置单词在文档里出现的位置列表
     *
     * @param positions：单词在文档里出现的位置列表
     */
    @Override
    public void setPositions(List<Integer> positions) {
        this.positions = positions;
    }

    /**
     * 比较二个Posting对象的大小（根据docId）
     *
     * @param o： 另一个Posting对象
     * @return ：二个Posting对象的docId的差值
     */
    @Override
    public int compareTo(AbstractPosting o) {
        return docId - o.getDocId();
    }

    /**
     * 对内部positions从小到大排序
     */
    @Override
    public void sort() {
        positions.sort(Comparator.naturalOrder());
    }

    /**
     * 写到二进制文件
     *
     * @param out :输出流对象
     */
    @Override
    public void writeObject(ObjectOutputStream out) {
        try {
            // 将this对象的成员依次序列化
            out.writeObject(this.docId);
            out.writeObject(this.freq);
            Integer[] obj = new Integer[positions.size()];
            positions.toArray(obj);
            out.writeObject(obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从二进制文件读
     *
     * @param in ：输入流对象
     */
    @Override
    public void readObject(ObjectInputStream in) {
        // 将this对象的成员依次反序列化，注意和序列化次序一致
        try {
            this.docId = (int) (in.readObject());
            this.freq = (int) (in.readObject());
            Integer[] obj = (Integer[]) in.readObject();
            this.positions = Arrays.asList(obj);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
