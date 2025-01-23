package search.index.impl;

import search.index.AbstractDocumentBuilder;
import search.index.AbstractIndex;
import search.index.AbstractIndexBuilder;
import search.util.FileUtil;

import java.io.*;
import java.util.List;

/**
 * AbstractIndexBuilder的具体实现类
 */
public class IndexBuilder extends AbstractIndexBuilder {

    /**
     * 构造函数
     *
     * @param docBuilder：文件构造工厂
     */
    public IndexBuilder(AbstractDocumentBuilder docBuilder) {
        super(docBuilder);
    }

    /**
     * 构建指定目录下的所有文本文件的倒排索引.
     *      需要遍历和解析目录下的每个文本文件, 得到对应的Document对象，再依次加入到索引，并将索引保存到文件.
     * @param rootDirectory ：指定目录
     * @return ：构建好的索引
     */
    @Override
    public AbstractIndex buildIndex(String rootDirectory) {
        AbstractIndex index = new Index();
        AbstractDocumentBuilder docBuilder = new DocumentBuilder();
        List<String> fileNames = FileUtil.list(rootDirectory);
        for(String fileName : fileNames)
            index.addDocument(docBuilder.build(docId++, fileName, new File(fileName)));
        return index;
    }
}
