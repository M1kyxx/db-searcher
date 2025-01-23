# API接口文档

项目由`JAVA`语言编写，主体部分由5个`package`（软件包）直接构成，本文档首先介绍其内容与作用，再分别介绍其中`API`接口说明与调用。

---

## 软件包介绍

1. `search.index`：包含与倒排索引数据结构相关的抽象类、构建索引时用到的中间数据结构的抽象类以及规定如何构建文档对象与倒排索引的抽象类，其`impl`包中包含各个抽象类的具体实现。
2. `search.parse`：与`java.io`包类似，该包的类在对文件系统访问时**基于流访问**，采用设计模式中的**装饰者模式**进行设计。包含将文档看作三元组流的抽象类，与其两个抽象子类，分别进行三元组读取与过滤，其`impl`包中包含三元组读取抽象类的具体实现与三个基于不同过滤规则的三元组过滤抽象类的具体实现。
3. `search.query`：包含进行全文检索的抽象类、搜索命中结果的抽象类以及对命中结果计算得分和排序的接口，其`impl`包中包含了各抽象类的具体实现与使用多种排序策略的接口实现类。
4. `search.run`：包含主程序与测试程序。
5. `search.util`：包含各种工具类。

---

## API接口介绍

为突出重点，仅对抽象类及其中较为重要的方法进行详细介绍，忽略其实现类与构造器等基础方法。**粗体**接口名或类名表示该接口或类对用户可见，将在搜索过程中使用。

**一、`search.index`**

1. `FileSerializable`接口
   
   ```java
   public interface FileSerializable extends java.io.Serializable
   ```
   
   * 实现类：`AbstractTerm`、`AbstractPosting`、`AbstractPostingList`、`AbstractIndex`
   * 说明：定义文件序列化接口。实现了该接口的类的实例可以通过该接口定义的两个方法，分别将实例内容序列化与反序列化，即将其内容写入二进制文件或从二进制文件中读取内容。
   * 方法
   
   | 限定符与方法名     | 接收参数           | 返回类型 | 功能说明                     |
   | ------------------ | ------------------ | -------- | ---------------------------- |
   | public writeObject | ObjectOutputStream | void     | 将内容序列化写入二进制文件   |
   | public readObject  | ObjectInputStream  | void     | 将二进制文件反序列化写入内存 |
   
2. `AbstractTerm`抽象类

   ```java
   public abstract class AbstractTerm implements Comparable<AbstractTerm>, FileSerializable
   ```

   * 实现类：`Term`
   * 成员

   | 类型             | 字段    | 说明     |
   | ---------------- | ------- | -------- |
   | protected String | content | Term内容 |

   * 说明：`AbstractTerm`是`Term`对象的抽象父类，`Term`对象表示文本文档里的一个单词。

     必须实现两个接口：`Comparable`，可根据字典序比较大小，为了加速检索过程，字典需要将单词进行排序；`FileSerializable`，可序列化到文件或从文件反序列化。

   * 方法

    | 限定符与方法名     | 接收参数           | 返回类型 | 功能说明                     |
    | ------------------ | ------------------ | -------- | ---------------------------- |
    | public toString    | void               | String   | 返回Term的字符串表示         |
    | public equals      | Object             | boolean  | 判断两个Term是否相同         |
    | public getContent  | void               | String   | 获取content内容              |
    | public setContent  | String             | void     | 设置content内容              |
    | public compareTo   | AbstractTerm       | int      | 按字典序比较两个Term大小     |
    | public writeObject | ObjectOutputStream | void     | 将内容序列化写入二进制文件   |
    | public readObject  | ObjectInputStream  | void     | 将二进制文件反序列化写入内存 |

3. `AbstractTermTuple`抽象类

   ```java
   public abstract class AbstractTermTuple
   ```

   * 实现类：`TermTuple`
   * 成员


   | 类型                | 字段   | 说明               |
   | ------------------- | ------ | ------------------ |
   | public int          | curPos | 单词出现的当前位置 |
   | public final int=1  | freq   | 出现次数，始终为1  |
   | public AbstractTerm | term   | 单词               |

   * 说明：`AbstractTermTuple`是所有`TermTuple`对象的抽象父类，一个`TermTuple`对象为三元组，其三个元素分别为单词、出现频率、出现的当前位置。解析某个文档时，每解析到一个单词，产生一个三元组，其中`freq`始终为1，因为单词出现了一次。
   * 方法

   | 限定符与方法名  | 接收参数 | 返回类型 | 功能说明                   |
   | --------------- | -------- | -------- | -------------------------- |
   | public toString | void     | String   | 返回三元组的字符串表示     |
   | public equals   | Object   | boolean  | 判断两个三元组内容是否相同 |

4. `AbstractDocument`

   ```java
   public abstract class AbstractDocument
   ```

   * 实现类：`Document`
   * 成员

   | 类型                                | 字段    | 说明                 |
   | ----------------------------------- | ------- | -------------------- |
   | protected int                       | docId   | 文档ID               |
   | protected String                    | docPath | 文档绝对路径         |
   | protected List\<AbstractTermTuple\> | tuples  | 文档包含的三元组列表 |

   * 说明：`AbstractDocument`是文档对象的抽象父类，文档对象是解析一个文本文件得到的结果，文档对象包含文档id、文档的绝对路径、文档包含的三元组对象列表。
   * 方法

   | 限定符与方法名      | 接收参数          | 返回类型                  | 功能说明                 |
   | ------------------- | ----------------- | ------------------------- | ------------------------ |
   | public getDocId     | void              | int                       | 获取文档ID               |
   | public setDocId     | int               | void                      | 设置文档ID               |
   | public getDocPath   | void              | String                    | 获取文档绝对路径         |
   | public setDocPath   | String            | void                      | 设置文档绝对路径         |
   | public getTuples    | void              | List\<AbstractTermTuple\> | 获取文档的三元组列表     |
   | public setTuples    | AbstractTermTuple | void                      | 向三元组列表添加三元组   |
   | public contains     | AbstractTermTuple | boolean                   | 判断是否包含输入的三元组 |
   | public getTuple     | int               | AbstractTermTuple         | 获取指定下标的三元组     |
   | public getTupleSize | void              | int                       | 获取文档包含三元组的个数 |
   | public toString     | void              | String                    | 获得Document的字符串表示 |

5. **`AbstractDocumentBuilder`**

   ```java
   public abstract class AbstractDocumentBuilder
   ```

   * 实现类：`DocumentBuilder`
   * 说明：`AbstractDocumentBuilder`是`Document`构造器的抽象父类，`Document`构造器的功能是由文本文档生成的`File`或解析文本文档得到的`TermTupleStream`，产生`Document`对象。
   * 方法

   | 限定符与方法名 | 接收参数                                                     | 返回类型         | 功能说明                                        |
   | -------------- | ------------------------------------------------------------ | ---------------- | ----------------------------------------------- |
   | public build   | int 文档ID；String 文档绝对路径；File 文档对应File对象       | AbstractDocument | 从输入的File对象构造Document对象                |
   | public build   | int 文档ID；String 文档绝对路径；AbstractTermTupleStream 文档对应的TermTupleStream | AbstractDocument | 从解析文本得到的TermTupleStream构造Document对象 |

6. `AbstractPosting`

   ```java
   public abstract class AbstractPosting implements Comparable<AbstractPosting>, FileSerializable
   ```

   * 实现类：`Posting`
   * 成员

   | 类型                       | 字段      | 说明                       |
   | -------------------------- | --------- | -------------------------- |
   | protected int              | docId     | 包含单词的文档ID           |
   | protected int              | freq      | 单词在文档里出现的频数     |
   | protected  List\<Integer\> | positions | 单词在文档里出现的位置列表 |

   * 说明：`AbstractPosting`是`Posting`对象的抽象父类，`Posting`对象代表倒排索引里每一项， 一个`Posting`对象包括：包含单词的文档id，单词在文档里出现的次数，单词在文档里出现的位置列表（位置下标不是以字符为编号，而是以单词为单位进行编号）。

     必须实现下面二个接口：`Comparable`，可比较大小（按照`docId`大小排序），当检索词为多个单词时，需要求这多个单词对应的`PostingList`的交集，如果每个`PostingList`按`docId`从小到大排序，可以提高求交集的效率；`FileSerializable`，可序列化到文件或从文件反序列化。

   * 方法

   | 限定符与方法名      | 接收参数           | 返回类型        | 功能说明                |
   | ------------------- | ------------------ | --------------- | ----------------------- |
   | **public equals**   | Object             | boolean         | 判断两个Posting是否相同 |
   | public getDocId     | void               | int             | 获取文档id              |
   | public setDocId     | int                | void            | 设置文档id              |
   | public getFreq      | void               | int             | 获取单词的出现次数      |
   | pubilc setFreq      | int                | void            | 设置单词的出现次数      |
   | public getPositions | void               | List\<Integer\> | 获取单词的出现位置列表  |
   | public setPositions | List\<Integer\>    | void            | 设置单词的出现位置列表  |
   | public compareTo    | AbstractPosting    | int             | 比较两个Posting大小     |
   | **public sort**     | void               | void            | 对内部positions进行排序 |
   | public readObject   | ObjectInputStream  | void            | 从二进制文件读取内容    |
   | public writeObject  | ObjectOutputStream | void            | 写入二进制文件          |

7. `AbstractPostingList`

   ```java
   public abstract class AbstractPostingList implements FileSerializable
   ```

   * 实现类：`PostingList`
   * 成员

   | 类型                             | 字段 | 说明                                                |
   | -------------------------------- | ---- | --------------------------------------------------- |
   | protected List\<AbstractPosting> | list | Posting列表，Posting必须是AbstractPosting子类型对象 |

   * 说明：`AbstractPostingList`是所有`PostingList`对象的抽象父类，`PostingList`对象包含了一个单词的`Posting`列表。

     必须实现`FileSerializable`接口，可序列化到文件或从文件反序列化

   * 方法

   | 限定符与方法名     | 接收参数                | 返回类型        | 功能说明                     |
   | ------------------ | ----------------------- | --------------- | ---------------------------- |
   | public toString    | void                    | String          | 返回PostingList的字符串表示  |
   | public add         | AbstractPosting         | void            | 添加Posting，不能重复        |
   | public add         | List\<AbstractPosting\> | void            | 添加Posting列表，不能重复    |
   | public get         | int                     | AbstractPosting | 获取指定下标的Posting        |
   | pubilc indexOf     | AbstractPosting         | int             | 返回某个Posting的下标        |
   | public indexOf     | int                     | int             | 获取指定文档id的Posting下标  |
   | public contains    | AbstractPosting         | boolean         | 判断是否包含指定Posting对象  |
   | public remove      | AbstractPosting         | void            | 移除指定Posting              |
   | public remove      | int                     | void            | 移除指定下标的Posting        |
   | public size        | void                    | int             | 获取PostingList的大小        |
   | public clear       | void                    | void            | 清空PostingList              |
   | public isEmpty     | void                    | boolean         | 判断PostingList是否为空      |
   | **public sort**    | void                    | void            | 根据文档id对PostingList排序  |
   | public writeObject | ObjectOutputStream      | void            | 将内容序列化写入二进制文件   |
   | public readObject  | ObjectInputStream       | void            | 将二进制文件反序列化写入内存 |

8. **`AbstractIndex`**

   ```java
   public abstract class AbstractIndex implements FileSerializable
   ```

   * 实现类：`Index`
   * 成员

   | 类型                                              | 字段                     | 说明                                            |
   | ------------------------------------------------- | ------------------------ | ----------------------------------------------- |
   | protected Map<Integer,  String>                   | docIdToDocPathMapping    | key为docId，value为对应的docPath                |
   | protected  Map<AbstractTerm, AbstractPostingList> | termToPostingListMapping | key为Term对象，value对应倒排索引PostingList对象 |

   * 说明：`AbstractIndex`是内存中的倒排索引对象的抽象父类，一个倒排索引对象包含了一个文档集合的倒排索引，内存中的倒排索引结构为`HashMap`，`key`为`Term`对象，`value`为对应的`PostingList`对象，另外在`AbstractIndex`里还定义了从`docId`和`docPath`之间的映射关系，必须实现`FileSerializable`接口，可序列化到文件或从文件反序列化
   * 方法

   | 限定符与方法名       | 接收参数           | 返回类型            | 功能说明                                                     |
   | -------------------- | ------------------ | ------------------- | ------------------------------------------------------------ |
   | public toString      | void               | String              | 获得索引的字符串表示                                         |
   | public addDocument   | AbstractDocument   | void                | 添加文档到索引                                               |
   | public load          | File               | void                | 从索引文件里反序列化出索引                                   |
   | public save          | File               | void                | 将内存中索引序列化到文件                                     |
   | pubilc search        | AbstractTerm       | AbstractPostingList | 获取指定单词的PostingList                                    |
   | public getDictionary | void               | Set\<AbstractTerm\> | 获取索引中的所有单词                                         |
   | public optimize      | void               | void                | 对索引进行优化：  PostingList按照文档id排序  Posting中的positions排序 |
   | public getdocName    | int                | String              | 获取文档id对应的绝对路径                                     |
   | public writeObject   | ObjectOutputStream | void                | 将内容写入二进制文件                                         |
   | public readObject    | ObjectInputStream  | void                | 从二进制文件读取内容                                         |

   

7. **`AbstractIndexBuilder`**

   ```java
   public abstract class AbstractIndexBuilder
   ```

   * 实现类：`IndexBuilder`
   * 成员
   
   | 类型                              | 字段       | 说明                                                    |
   | --------------------------------- | ---------- | ------------------------------------------------------- |
   | protected AbstractDocumentBuilder | docBuilder | 利用AbstractDocumentBuilder对象构建逐个构建Document对象 |
   | protected int                     | docId      | 每解析一个文档写入索引后，计数器值+1                    |

   * 说明：`AbstractIndexBuilder`是索引构造器的抽象父类，需要实例化一个具体子类对象完成索引构造的工作。
   * 方法
   
   | 限定符与方法名        | 接收参数 | 返回类型      | 功能说明                               |
   | --------------------- | -------- | ------------- | -------------------------------------- |
   | **public buildIndex** | String   | AbstractIndex | 构建指定目录下的所有文本文件的倒排索引 |
   

**二、`search.parse`**

1. `AbstractTermTupleStream`

   ```java
   public abstract class AbstractTermTupleStream
   ```

   * 实现类：`AbstractTermTupleFilter`、`AbstractTermTupleScanner`
   * 说明：`AbstractTermTupleStream`是各种`TermFreqPosTupleStream`对象的抽象父类，`TermFreqPosTupleStream`是三元组`TermTuple`流对象，包含了解析文本文件得到的三元组序列
   * 方法

   | 限定符与方法名  | 接收参数 | 返回类型          | 功能说明                         |
   | --------------- | -------- | ----------------- | -------------------------------- |
   | **public next** | void     | AbstractTermTuple | 获取下一个三元组；流末尾返回null |
   | public close    | void     | void              | 关闭流                           |

2. `AbstractTermTupleScanner`

   ```java
   public abstract class AbstractTermTupleScanner extends AbstractTermTupleStream
   ```

   * 实现类：`TermTupleScanner`
   * 成员

   | 类型                     | 字段      | 字段和说明                                            |
   | ------------------------ | --------- | ----------------------------------------------------- |
   | protected BufferedReader | input     | 作为输入流对象，读取文本文件得到一个个三元组TermTuple |
   | public int               | curPos    | 单词出现位置，初始为0                                 |
   | private List\<String>    | tempParts | 存储文本文件中经分词得到的所有单词                    |

   * 说明：`AbstractTermTupleScanner`是`AbstractTermTupleStream`的抽象子类，即一个具体的`TermTupleScanner`对象就是一个`AbstractTermTupleStream`流对象，它利用`java.io.BufferedReader`去读取文本文件得到一个个三元组`TermTuple`。

   | 限定符与方法名  | 接收参数 | 返回类型          | 功能说明         |
   | --------------- | -------- | ----------------- | ---------------- |
   | **public next** | void     | AbstractTermTuple | 获取下一个三元组 |

3. `AbstractTermTupleFilter`

   ```java
   public abstract class AbstractTermTupleFilter extends AbstractTermTupleStream
   ```

   * 实现类：`LengthTermTupleFilter`、`PatternTermTupleFilter`、`StopWordTermTupleFilter`
   * 成员
   
   | 类型                               | 字段  | 说明                           |
   | ---------------------------------- | ----- | ------------------------------ |
   | protected  AbstractTermTupleStream | input | 从TermTupleScanner得到的输入流 |
   
   * 说明：抽象类`AbstractTermTupleFilter`类型是`AbstractTermTupleStream`的子类，里面包含另一个`AbstractTermTupleStream`对象作为输入，并对输入的`AbstractTermTupleStream`进行过滤，例如过滤掉所有停用词（the, is, are...）对应的三元组，其具体子类需要重新实现`next`方法以过滤掉不需要的单词对应的三元组。同时可以实现多个不同的过滤器，完成不同的过滤功能，多个过滤器可以形成过滤管道。
   * 方法
   
   | 限定符与方法名 | 接收参数            | 返回类型 | 功能说明                           |
   | -------------- | ------------------- | -------- | ---------------------------------- |
   | public sort    | List\<AbstractHit\> | void     | 对命中的集合根据分数进行排序       |
   | public score   | AbstractHit         | double   | 给命中的文章根据检索词出现频率打分 |
   

**三、`search.query`**

1. `AbstractHit`

   ```java
   public abstract class AbstractHit implements Comparable<AbstractHit>
   ```

   * 实现类：`Hit`
   * 成员

   | 类型                                          | 字段               | 说明                                                         |
   | --------------------------------------------- | ------------------ | ------------------------------------------------------------ |
   | protected String                              | content            | 文档原文内容                                                 |
   | protected int                                 | docId              | 文档ID                                                       |
   | protected String                              | docPath            | 文档绝对路径                                                 |
   | protected double                              | score              | 该文档的命中得分，文档的得分通过Sort接口计算.每个文档得分默认值为1.0 |
   | protected  Map<AbstractTerm, AbstractPosting> | termPostingMapping | 命中的单词和对应Posting的键值对                              |

   * 说明：`AbstractHit`是一个搜索命中结果的抽象类。

     该类子类要实现`Comparable`接口，因为需要必须比较大小，用于命中结果的排序。

   * 方法

   | 限定符与方法名                  | 接收参数    | 返回类型                           | 功能说明                                        |
   | ------------------------------- | ----------- | ---------------------------------- | ----------------------------------------------- |
   | public getDocId                 | void        | int                                | 获取文档id                                      |
   | public getContent               | void        | String                             | 获取文章内容                                    |
   | public getDocPath               | void        | String                             | 获取文档绝对路径                                |
   | public setContent               | String      | void                               | 设置文档内容                                    |
   | public getScore                 | void        | double                             | 获取文章得分                                    |
   | public setScore                 | double      | void                               | 设置文章得分                                    |
   | public getTermPositingMapping   | void        | Map<AbstractTerm, AbstractPosting> | 获取命中的单词对应的Posting键值对               |
   | public compareTo                | AbstractHit | int                                | 根据得分比较两个Hit，如果得分相同根据文章id比较 |
   | **private getHighlightedWords** | void        | String                             | 获取需要高亮显示的单词列表，用于构建正则表达式  |
   | **public toString**             | void        | String                             | 将Hit转换为字符串                               |

2. `AbstractIndexSearcher`

   ```java
   public abstract class AbstractIndexSearcher
   ```

   * 实现类：`IndexSearcher`
   * 成员

   | 类型                    | 字段  | 说明                               |
   | ----------------------- | ----- | ---------------------------------- |
   | protected AbstractIndex | index | 内存的索引，子类对象被初始化时为空 |

   * 说明：`AbstractIndexSearcher`是检索具体实现的抽象类
   * 方法

   | 限定符与方法名    | 接收参数                                                   | 返回类型      | 功能说明                                       |
   | ----------------- | ---------------------------------------------------------- | ------------- | ---------------------------------------------- |
   | public open       | String                                                     | void          | 从指定索引文件打开索引并加载到index对象中      |
   | public search     | AbstractTerm，Sort                                         | AbstractHit[] | 根据单个检索词进行搜索                         |
   | **public search** | AbstractTerm，  AbstractTerm，  Sort，  LogicalCombination | AbstractHit[] | 根据两个检索词以及两个检索词的逻辑关系进行搜索 |

3. `Sort`接口

   ```java
   public interface Sort
   ```

   * 实现类：`SimpleSorter`、`FilenameSorter`、`LengthSorter`、`TimeSorter`

   * 说明：`Sort`定义了对搜索结果排序的接口
   * 方法

   | 限定符和方法名 | 接收参数           | 返回类型 | 功能说明                           |
   | -------------- | ------------------ | -------- | ---------------------------------- |
   | public sort    | List\<AbstractHit> | void     | 对命中的集合根据分数进行排序       |
   | public score   | AbstractHit        | double   | 给命中的文章根据检索词出现频率打分 |

**四、`search.index`**

1. `EngineStart`类：该类为命令行交互程序测试类，用于在命令行测试搜索引擎，其中唯一的`main`方法作为交互程序入口。

**五、`search.util`**

1. `Config`类：保存搜索引擎的配置信息

   ```java
   public class Config {
       
       // Java工程HOME目录
       // System.getProperty("user.dir")返回当前JAVA工程目录
       public static String PROJECT_HOME_DIR = System.getProperty("user.dir");
       
       // 索引文件的目录，以相对路径指定索引文件目录
       // 将索引文件保存在当前工程目录下的index子目录中，索引文件目录是相对路径
       // 无论工程在什么位置，程序都可以正常运行
       public static String INDEX_DIR = PROJECT_HOME_DIR + "\\index\\";
       
       // 文本文件的目录，以相对路径指定文本文件目录
       // 将文本文件保存在当前工程目录下的text子目录中，文本文件目录是相对路径
       // 无论你把整个工程放在什么位置，程序都可以正常运行
       public static String DOC_DIR = PROJECT_HOME_DIR + "\\text\\";
   
       // 构建索引和检索时是否忽略单词大小写
       public static boolean IGNORE_CASE = true;
   
       // 将字符串切分成单词时所需的正则表达式
       // 例如根据中英文的逗号、分号、句号、问号、冒号、感叹号、中文顿号、空白分割符进行切分
       public static String STRING_SPLITTER_REGEX = "[,|，|;|；|.|。|?|？|:|：|!|！|、|《|》|(|（|)|）|*|'|‘|’|\"|”|“|\\s]+";
   
       // 单词过滤的正则表达式
       // 例如正则表达式指定只保留由字母组成的term，其他的term全部过滤掉，不写入倒排索引
       public static String TERM_FILTER_PATTERN = "[a-zA-Z]+";
   
       // 基于单词的最小长度过滤单词.
       // 例如指定最短单词长度为3，长度小于3的单词过滤掉，不写入倒排索引
       public static int TERM_FILTER_MINLENGTH = 1;
   
       // 基于单词的最大长度过滤单词.
       //例如指定最长单词长度为20，长度大于20的单词过滤掉，不写入倒排索引
       public static int TERM_FILTER_MAXLENGTH = 20;
   }
   ```

2. `FileUtil`类

   * 说明：文件操作的工具类
   * 方法

   | 限定符和方法名      | 接收参数                                     | 返回类型      | 功能说明                                                   |
   | ------------------- | -------------------------------------------- | ------------- | ---------------------------------------------------------- |
   | public static read  | String 指定文本文件的绝对路径                | String        | 读取指定文本文件的所有内容                                 |
   | public static write | String 写入的内容；String 指定的文本文件路径 | void          | 将字符串写入指定的文本文件                                 |
   | public static list  | String 指定目录                              | List\<String> | 列出指定目录下所有文件的绝对路径，不递归                   |
   | public static list  | String 指定目录；String 指定后缀名           | List\<String> | 列出指定目录下的匹配指定后缀名的所有文件的绝对路径，不递归 |

   

3. `StopWords`类：存储中英文停用词，停用词用于过滤无意义的`term`

   ```java
   public class StopWords {
       // 停用词字符串数组
       public static List<String> STOP_WORDS;
   }
   ```

   

4. `StringSplitter`类

   * 说明：字符串分割操作工具类
   * 成员

   | 类型            | 字段       | 说明                             |
   | --------------- | ---------- | -------------------------------- |
   | private String  | splitRegex | 分词所用正则表达式               |
   | private Pattern | pattern    | 分词所用正则表达式的编译表示     |
   | private Matcher | match      | 用于根据正则表达式进行字符串匹配 |

   * 方法

   | 限定符和方法名       | 接收参数 | 返回类型      | 功能说明                   |
   | -------------------- | -------- | ------------- | -------------------------- |
   | public setSplitRegex | String   | void          | 设置分词所用正则表达式     |
   | public splitByRegex  | String   | List\<String> | 将输入字符串分割成单词列表 |

   