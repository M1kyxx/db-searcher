package search.index.impl;

import search.index.AbstractDocument;
import search.index.AbstractDocumentBuilder;
import search.index.AbstractTermTuple;
import search.parse.AbstractTermTupleStream;
import search.parse.impl.LengthTermTupleFilter;
import search.parse.impl.PatternTermTupleFilter;
import search.parse.impl.StopWordTermTupleFilter;
import search.parse.impl.TermTupleScanner;

import java.io.*;

/**
 * AbstractDocumentBuilder的具体实现类
 */
public class DocumentBuilder extends AbstractDocumentBuilder {
    /**
     * 由解析文本文档得到的TermTupleStream,构造Document对象.
     *
     * @param docId           : 文档id
     * @param docPath         : 文档绝对路径
     * @param termTupleStream : 文档对应的TermTupleStream
     * @return ：Document对象
     */
    @Override
    public AbstractDocument build(int docId, String docPath, AbstractTermTupleStream termTupleStream) {
        AbstractDocument doc = new Document(docId, docPath);
        AbstractTermTuple tuple;
        while ((tuple = termTupleStream.next()) != null)
            doc.addTuple(tuple);
        System.out.println(docPath);
        return doc;
    }

    /**
     * 由给定的File,构造Document对象.
     * 该方法利用输入参数file构造出AbstractTermTupleStream子类对象后,内部调用
     * AbstractDocument build(int docId, String docPath, AbstractTermTupleStream termTupleStream)
     *
     * @param docId   : 文档id
     * @param docPath : 文档绝对路径
     * @param file    : 文档对应File对象
     * @return : Document对象
     */
    @Override
    public AbstractDocument build(int docId, String docPath, File file) {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file)));
            AbstractTermTupleStream ts = new PatternTermTupleFilter(
                    new LengthTermTupleFilter(new StopWordTermTupleFilter(new TermTupleScanner(reader))));
            return build(docId, docPath, ts);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
