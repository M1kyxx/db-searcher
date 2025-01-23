package search.index.impl;

import search.index.*;

import java.io.*;
import java.util.*;

/**
 * AbstractIndex的具体实现类
 */
public class Index extends AbstractIndex {
    /**
     * 返回索引的字符串表示
     *
     * @return 索引的字符串表示
     */
    @Override
    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append("docId----docPath mapping:\n");
        for (Map.Entry<Integer, String> entry : docIdToDocPathMapping.entrySet()) {
            str.append(entry.getKey()).append("   ---->   ").append(entry.getValue()).append("\n");
        }
        str.append("\nPostings:\n");
        for (Map.Entry<AbstractTerm, AbstractPostingList> entry : termToPostingListMapping.entrySet()) {
            str.append(entry.getKey()).append("   ---->   ").append(entry.getValue()).append("\n");
        }
        return str.toString();
    }

    /**
     * 添加文档到索引，更新索引内部的HashMap
     *
     * @param document ：文档的AbstractDocument子类型表示
     */
    @Override
    public void addDocument(AbstractDocument document) {
        // 添加当前文件docId和docPath的映射关系
        docIdToDocPathMapping.put(document.getDocId(), document.getDocPath());
        // 得到当前文件的三元组列表
        List<AbstractTermTuple> tuples = document.getTuples();
        // 遍历三元组，对每个Term，或新建或添加由当前文件构成的Posting
        for (AbstractTermTuple tuple : tuples) {
            // Term已存在
            if (termToPostingListMapping.containsKey(tuple.term)) {
                AbstractPostingList list = termToPostingListMapping.get(tuple.term);
                int idx;
                // 如果docId已存在于PostingList中，则该docId代表的Posting更新freq和positions
                if ((idx = list.indexOf(document.getDocId())) >= 0) {
                    AbstractPosting posting = list.get(idx);
                    // freq + 1
                    posting.setFreq(posting.getFreq() + 1);
                    List<Integer> pos = posting.getPositions();
                    // positions添加当前位置
                    pos.add(tuple.curPos);
                    posting.setPositions(pos);
                // docId不存在，新建Posting
                } else {
                    list.add(new Posting(document.getDocId(), tuple.curPos));
                }
            // Term不存在，新建PostingList，添加Posting
            } else {
                termToPostingListMapping.put(tuple.term, new PostingList());
                AbstractPostingList list = termToPostingListMapping.get(tuple.term);
                list.add(new Posting(document.getDocId(), tuple.curPos));
            }
        }
    }

    /**
     * 从索引文件里加载已经构建好的索引.内部调用FileSerializable接口方法readObject即可
     * @param file ：索引文件
     */
    @Override
    public void load(File file) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            this.readObject(ois);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将在内存里构建好的索引写入到文件. 内部调用FileSerializable接口方法writeObject即可
     * @param file ：写入的目标索引文件
     */
    @Override
    public void save(File file) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
            this.writeObject(oos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 返回指定单词的PostingList
     *
     * @param term : 指定的单词
     * @return ：指定单词的PostingList;如果索引字典没有该单词，则返回null
     */
    @Override
    public AbstractPostingList search(AbstractTerm term) {
        if (termToPostingListMapping.containsKey(term)) {
            return termToPostingListMapping.get(term);
        }
        return null;
    }

    /**
     * 返回索引的字典.字典为索引里所有单词的并集
     *
     * @return ：索引中Term列表
     */
    @Override
    public Set<AbstractTerm> getDictionary() {
        return new HashSet<>(termToPostingListMapping.keySet());
    }

    /**
     * <pre>
     * 对索引进行优化，包括：
     *      对索引里每个单词的PostingList按docId从小到大排序
     *      同时对每个Posting里的positions从小到大排序
     * 在内存中把索引构建完后执行该方法
     * </pre>
     */
    @Override
    public void optimize() {
        for (AbstractPostingList list : termToPostingListMapping.values()) {
            list.sort();
            for (int i = 0; i < list.size(); i++) {
                list.get(i).sort();
            }
        }
    }

    /**
     * 根据docId获得对应文档的完全路径名
     *
     * @param docId ：文档id
     * @return : 对应文档的完全路径名
     */
    @Override
    public String getDocName(int docId) {
        return docIdToDocPathMapping.get(docId);
    }

    /**
     * 写到二进制文件
     *
     * @param out :输出流对象
     */
    @Override
    public void writeObject(ObjectOutputStream out) {
        try {
            out.writeObject(this.docIdToDocPathMapping);
            out.writeObject(this.termToPostingListMapping);
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
        try {
            this.docIdToDocPathMapping = (Map<Integer, String>) (in.readObject());
            this.termToPostingListMapping = (Map<AbstractTerm, AbstractPostingList>) (in.readObject());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
