package search.parse.impl;

import search.parse.AbstractTermTupleFilter;
import search.parse.AbstractTermTupleStream;
import search.index.AbstractTermTuple;
import search.util.StopWords;

/**
 * AbstractTermTupleFilter用于过滤停用词的具体实现类
 */
public class StopWordTermTupleFilter extends AbstractTermTupleFilter {
    /**
     * 构造函数
     *
     * @param input ：Filter的输入，类型为AbstractTermTupleStream
     */
    public StopWordTermTupleFilter(AbstractTermTupleStream input) {
        super(input);
    }

    /**
     * 获得下一个三元组
     *
     * @return : 下一个三元组；如果到了流的末尾，返回null
     */
    @Override
    public AbstractTermTuple next() {
        AbstractTermTuple tuple;
        while ((tuple = input.next()) != null)
            if (!StopWords.STOP_WORDS.contains(tuple.term.getContent()))
                return tuple;
        return null;
    }
}
