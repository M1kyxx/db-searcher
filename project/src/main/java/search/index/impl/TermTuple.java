package search.index.impl;

import search.index.AbstractTerm;
import search.index.AbstractTermTuple;

/**
 * AbstractTermTuple的具体实现类
 */
public class TermTuple extends AbstractTermTuple {
    /**
     * 缺省构造函数
     */
    public TermTuple() {
        term = new Term();
        curPos = -1;
    }

    /**
     * 构造函数
     *
     * @param term：单词
     * @param curPos：单词出现的当前位置
     */
    public TermTuple(AbstractTerm term, int curPos) {
        this.term = term;
        this.curPos = curPos;
    }

    /**
     * 判断二个三元组内容是否相同
     *
     * @param obj ：要比较的另外一个三元组
     * @return 如果内容相等（三个属性内容都相等）返回true，否则返回false
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TermTuple) {
            TermTuple termTuple = (TermTuple) obj;
            return term.equals(termTuple.term) && curPos == termTuple.curPos;
        }
        return false;
    }

    /**
     * 获得三元组的字符串表示
     *
     * @return ： 三元组的字符串表示
     */
    @Override
    public String toString() {
        return "<\"" + term.toString() + "\", " + freq + ", " + curPos + ">";
    }
}
