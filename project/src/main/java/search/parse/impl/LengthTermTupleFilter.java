package search.parse.impl;

import search.parse.AbstractTermTupleFilter;
import search.parse.AbstractTermTupleStream;
import search.index.AbstractTermTuple;
import search.util.Config;

/**
 * AbstractTermTupleFilter用于过滤过长或过短词的具体实现类
 */
public class LengthTermTupleFilter extends AbstractTermTupleFilter {
    /**
     * 构造函数
     *
     * @param input ：Filter的输入，类型为AbstractTermTupleStream
     */
    public LengthTermTupleFilter(AbstractTermTupleStream input) {
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
            if (tuple.term.getContent().length() >= Config.TERM_FILTER_MINLENGTH && tuple.term.getContent().length() <= Config.TERM_FILTER_MAXLENGTH)
                return tuple;
        return null;
    }
}
