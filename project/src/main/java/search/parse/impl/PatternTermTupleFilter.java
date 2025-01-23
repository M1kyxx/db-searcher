package search.parse.impl;

import search.parse.AbstractTermTupleFilter;
import search.parse.AbstractTermTupleStream;
import search.index.AbstractTermTuple;

/**
 * AbstractTermTupleFilter用于过滤含非字母字符的单词的具体实现类
 */
public class PatternTermTupleFilter extends AbstractTermTupleFilter {
    /**
     * 构造函数
     *
     * @param input ：Filter的输入，类型为AbstractTermTupleStream
     */
    public PatternTermTupleFilter(AbstractTermTupleStream input) {
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
//            if (tuple.term.getContent().matches(Config.TERM_FILTER_PATTERN))
                return tuple;
        return null;
    }
}
