package search.query.impl;

import search.query.AbstractHit;
import search.index.AbstractPosting;
import search.index.AbstractTerm;

import java.util.*;

/**
 * AbstractHit的具体实现类
 */
public class Hit extends AbstractHit {
    /**
     * 缺省构造函数
     */
    public Hit() {
        super();
    }

    /**
     * 构造函数
     *
     * @param docId   : 文档id
     * @param docPath : 文档绝对路径
     */
    public Hit(int docId, String docPath) {
        super(docId, docPath);
    }

    /**
     * 构造函数
     *
     * @param docId              ：文档id
     * @param docPath            ：文档绝对路径
     * @param termPostingMapping ：命中的三元组列表
     */
    public Hit(int docId, String docPath, Map<AbstractTerm, AbstractPosting> termPostingMapping) {
        super(docId, docPath, termPostingMapping);
    }

    /**
     * 构造函数
     *
     * @param docId              ：文档id
     * @param docPath            ：文档绝对路径
     * @param termPostingMapping ：命中的三元组列表
     * @param score              ：得分
     */
    public Hit(int docId, String docPath, Map<AbstractTerm, AbstractPosting> termPostingMapping, double score) {
        super(docId, docPath, termPostingMapping);
        this.score = score;
    }

    /**
     * 获得文档id
     *
     * @return ： 文档id
     */
    @Override
    public int getDocId() {
        return docId;
    }

    /**
     * 获得文档绝对路径
     *
     * @return ：文档绝对路径
     */
    @Override
    public String getDocPath() {
        return docPath;
    }

    /**
     * 获得文档内容
     *
     * @return ： 文档内容
     */
    @Override
    public String getContent() {
        return content;
    }

    /**
     * 设置文档内容
     *
     * @param content ：文档内容
     */
    @Override
    public void setContent(String content) {
        if (content != null && !content.isEmpty())
            this.content = content;
    }

    /**
     * 获得文档得分
     *
     * @return ： 文档得分
     */
    @Override
    public double getScore() {
        return score;
    }

    /**
     * 设置文档得分
     *
     * @param score ：文档得分
     */
    @Override
    public void setScore(double score) {
        this.score = score;
    }

    /**
     * 获得命中的单词和对应的Posting键值对
     *
     * @return ：命中的单词和对应的Posting键值对
     */
    @Override
    public Map<AbstractTerm, AbstractPosting> getTermPostingMapping() {
        return termPostingMapping;
    }

    /**
     * 获得命中结果的字符串表示, 用于显示搜索结果.
     *
     * @return : 命中结果的字符串表示
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("docId: ").append(docId).append(", docPath: ").append(docPath).append(", score: ").append(score).append("\n");
        str.append("content: \n");

        // ANSI转义序列，用于在控制台输出时改变文字颜色
        String blueColor = "\u001B[34m"; // 蓝色
        String resetColor = "\u001B[0m"; // 恢复默认颜色

        // 原始内容
//        str.append(content).append("\n");

        // 高亮后内容
        String highlightedContent = content.replaceAll("\\b(" + getHighlightedWords() + ")\\b", blueColor + "$1" + resetColor); // 将匹配的单词替换为蓝色
        str.append(highlightedContent).append("\n");


        str.append("\nTerm----posting mapping: \n");
        for (Map.Entry<AbstractTerm, AbstractPosting> entry : termPostingMapping.entrySet()) {
            str.append(entry.getKey()).append("  ---->  ").append(entry.getValue()).append("\n");
        }
        return str.toString();
    }

    // 获取需要高亮显示的单词列表，用于构建正则表达式
    private String getHighlightedWords() {
        StringBuilder words = new StringBuilder();
        for (AbstractTerm term : termPostingMapping.keySet()) {
            words.append(term.getContent()).append("|");
        }
        // $匹配结尾
        return words.toString().replaceAll("\\|$", ""); // 移除末尾的 |
    }


    /**
     * 比较两个命中结果的大小，根据score比较
     *
     * @param o ：要比较的命中结果
     * @return ：二个命中结果得分的差值
     */
    @Override
    public int compareTo(AbstractHit o) {
        return (int) (score - o.getScore());
    }

    @Override
    public String[] getAllTerms() {
        // 调用keySet()方法获取Map中所有的键，并存储到一个Set集合中
        Set<AbstractTerm> keySet = termPostingMapping.keySet();
        Set<String> stringSet = new HashSet<>();
        for (AbstractTerm key : keySet) {
            stringSet.add(key.getContent());
        }

        // 方法一：利用toArray(T[] a)方法将Set转换为数组
        // 注意：此方法需要提供一个足够大的数组来存储Set中的所有元素
        // 如果提供的数组太小，将会创建一个新的、大小合适的数组
        // 如果提供的数组太大，则数组中的剩余空间将用null填充
        return stringSet.toArray(new String[0]); // 推荐使用0长度的数组，避免不必要的空间分配
    }

    @Override
    public List<Integer> getAllPositions() {
        List<Integer> mergedPositions = new ArrayList<>();
        for (AbstractPosting posting : termPostingMapping.values()) {
            mergedPositions.addAll(posting.getPositions());
        }
        Set<Integer> uniqueSet = new HashSet<>(mergedPositions);
        return uniqueSet.stream().sorted().toList();
    }

    @Override
    public String getAllPositionsAsString() {
        List<Integer> mergedPositions = new ArrayList<>();
        for (AbstractPosting posting : termPostingMapping.values()) {
            mergedPositions.addAll(posting.getPositions());
        }
        Set<Integer> uniqueSet = new HashSet<>(mergedPositions);
        List<Integer> sortedPositions = uniqueSet.stream().sorted().toList();
        StringBuilder stringBuilder = new StringBuilder();
        for (Integer position : sortedPositions) {
            stringBuilder.append(position).append(" ");
        }
        return stringBuilder.toString();
    }
}
