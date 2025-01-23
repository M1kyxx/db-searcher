package search.run;

import search.index.AbstractDocumentBuilder;
import search.index.AbstractIndex;
import search.index.AbstractIndexBuilder;
import search.index.impl.DocumentBuilder;
import search.index.impl.Index;
import search.index.impl.IndexBuilder;
import search.query.AbstractHit;
import search.query.AbstractIndexSearcher;
import search.query.Sort;
import search.query.impl.IndexSearcher;
import search.query.impl.SimpleSorter;
import search.util.Config;

import java.io.File;
import java.util.Scanner;

/**
 * 测试交互
 */
public class EngineStart {
    /**
     * 交互程序入口
     *
     * @param args : 命令行参数
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("欢迎使用基于内存的搜索引擎！");

        String indexFile = Config.INDEX_DIR + "index.dat";
        String string;
        boolean isUpdate = false;
        File indexFile2Test = new File(Config.INDEX_DIR + "index.dat");
        if(indexFile2Test.length() != 0) {
            System.out.println("索引文件已存在，是否需要更新索引[输入y/n]: ");
            string = scanner.nextLine();
            if (string.equalsIgnoreCase("y")) {
                isUpdate = true;
            }
        }
        if(indexFile2Test.length() == 0 || isUpdate) {
            System.out.println("索引文件不存在或为空，开始建立索引...");
            System.out.println("搜索区域默认为text文件夹，若无需更改搜索区域请输入'ok'，否则请输入文件夹路径: ");
            string = scanner.nextLine();
            String rootDir;
            if (string.equalsIgnoreCase("ok"))
                rootDir = Config.DOC_DIR;
            else
                rootDir = string;

            AbstractDocumentBuilder documentBuilder = new DocumentBuilder();
            AbstractIndexBuilder indexBuilder = new IndexBuilder(documentBuilder);
            System.out.println("开始以" + rootDir + "为搜索区域建立索引...");
            // 开始时间
            long stime1 = System.currentTimeMillis();
            AbstractIndex index = indexBuilder.buildIndex(rootDir);
            index.optimize();
            // 结束时间
            long etime1 = System.currentTimeMillis();
            // 计算执行时间
            System.out.printf("索引建立耗时：%d 毫秒\n", (etime1 - stime1));
            System.out.println("索引如下: ");
            System.out.println(index);  //控制台打印index的内容

            //测试保存到文件
            index.save(new File(indexFile));    //索引保存到文件
            System.out.println("索引已保存至二进制文件" + indexFile);
        }

        //测试从文件读取
        System.out.println("是否测试从二进制文件中读取索引[输入y/n]: ");
        string = scanner.nextLine();
        if (string.equalsIgnoreCase("y")) {
            AbstractIndex index2 = new Index();  //创建一个空的index
            index2.load(new File(indexFile));       //从文件加载对象的内容
            System.out.println("从二进制文件" + indexFile + "中读取得索引如下: ");
            System.out.println(index2);  //控制台打印index2的内容
        }

        Sort simpleSorter = new SimpleSorter();
        AbstractIndexSearcher searcher = new IndexSearcher();
        searcher.open(indexFile);
        while (true) {
            System.out.println("索引读取完毕，正在进行搜索...");
//            System.out.println("搜索共有四种模式，根据输入的格式进行模式选择");
//            System.out.println("a.输入一个单词，搜索包含该单词的文件，格式为: \"word\"");
//            System.out.println("b.输入两个单词，搜索包含该短语的文件，格式为: \"word word\"");
//            System.out.println("c.输入三个单词，其中第二个单词为and，搜索同时包含其余两个单词的文件，格式为: \"word and word\"");
//            System.out.println("d.输入三个单词，其中第二个单词为or，搜索包含任意一个单词的文件，格式为: \"word or word\"");
            System.out.println("仅接收满足一定格式的搜索词，具体格式在用户手册中给出");
            System.out.println("输入0即可退出");
            System.out.println("请进行输入: ");
            string = scanner.nextLine().toLowerCase();
            // 开始时间
            long stime = System.currentTimeMillis();
            String[] strIn = string.split(" ");
            if (strIn.length == 1 && strIn[0].equalsIgnoreCase("0")) {
                break;
            }
            AbstractHit[] hits = searcher.search(strIn, simpleSorter);
            if (hits == null) {
                System.out.println("输入格式有误，请重新输入");
            } else if (hits.length > 0) {
                System.out.println("搜索结果如下: ");
                for (AbstractHit hit : hits) {
                    System.out.println(hit);
                }
            } else
                System.out.println("未搜索到内容");
            // 结束时间
            long etime = System.currentTimeMillis();
            // 计算执行时间
            System.out.printf("本次搜索耗时：%d 毫秒\n", (etime - stime));
        }
        System.out.println("搜索结束，程序已退出，感谢使用！");
    }
}
