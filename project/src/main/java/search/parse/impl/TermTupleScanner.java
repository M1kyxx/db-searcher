package search.parse.impl;

import search.parse.AbstractTermTupleScanner;
import search.index.AbstractTermTuple;
import search.index.impl.Term;
import search.index.impl.TermTuple;
import search.util.Config;
import search.util.StringSplitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * AbstractTermTupleScanner的具体实现类
 */
public class TermTupleScanner extends AbstractTermTupleScanner {
    /**
     * tempParts作为String列表，存储文本文件中经分词得到的所有单词
     */
    private List<String> tempParts = new ArrayList<>();

    /**
     * curPos标记遍历tempParts时的当前位置
     */
    private int curPos = 0;

    /**
     * 缺省构造函数
     */
    public TermTupleScanner() {
        super();
    }

    /**
     * 构造函数
     *
     * @param input：指定输入流对象，应该关联到一个文本文件
     */
    public TermTupleScanner(BufferedReader input) {
        super(input);
    }

    /**
     * 获得下一个三元组
     *
     * @return : 下一个三元组；如果到了流的末尾，返回null
     */
    @Override
    public AbstractTermTuple next() {
        if (tempParts.isEmpty()) {
            try {
                String line;
                while ((line = input.readLine()) != null) {
                    StringSplitter splitter = new StringSplitter();
                    splitter.setSplitRegex(Config.STRING_SPLITTER_REGEX);
                    tempParts.addAll(splitter.splitByRegex(line));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (curPos < tempParts.size()) {
            if (Config.IGNORE_CASE)
                return new TermTuple(new Term(tempParts.get(curPos).toLowerCase()), curPos++);
            else
                return new TermTuple(new Term(tempParts.get(curPos)), curPos++);
        } else
            return null;
    }
}
