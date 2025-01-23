package search.query.impl;

import search.query.AbstractHit;
import search.query.AbstractIndexSearcher;
import search.query.Sort;
import search.index.AbstractPosting;
import search.index.AbstractPostingList;
import search.index.AbstractTerm;
import search.index.impl.Posting;
import search.index.impl.Term;

import java.io.File;
import java.util.*;

/**
 * AbstractIndexSearcher的具体实现类
 */
public class IndexSearcher extends AbstractIndexSearcher {
    /**
     * 从指定索引文件打开索引，加载到index对象里. 一定要先打开索引，才能执行search方法
     *
     * @param indexFile ：指定索引文件
     */
    @Override
    public void open(String indexFile) {
        index.load(new File(indexFile));
    }

    /**
     * 根据单个检索词进行搜索
     *
     * @param queryTerm ：检索词
     * @param sorter    ：排序器
     * @return ：命中结果数组
     */
    @Override
    public AbstractHit[] search(AbstractTerm queryTerm, Sort sorter) {
        List<AbstractHit> hits = new ArrayList<>();
        if (index.termToPostingListMapping.containsKey(queryTerm)) {
            AbstractPostingList list = index.termToPostingListMapping.get(queryTerm);
            for (int i = 0; i < list.size(); i++) {
                AbstractPosting posting = list.get(i);
                Map<AbstractTerm, AbstractPosting> tempMap = new TreeMap<>();
                tempMap.put(queryTerm, posting);
                AbstractHit hit = new Hit(posting.getDocId(), index.docIdToDocPathMapping.get(posting.getDocId()), tempMap);
                hit.setScore(posting.getFreq());
                hits.add(hit);
            }
        }
        sorter.sort(hits);
        return hits.toArray(new AbstractHit[0]);
    }

    @Override
    public AbstractHit[] search(AbstractTerm[] queryTerms, Sort sorter, boolean not) {
        List<AbstractHit> hits = new ArrayList<>();
        AbstractTerm firstTerm = queryTerms[0];
        Map<AbstractTerm, AbstractPosting> tempMap = new TreeMap<>();
        if (index.termToPostingListMapping.containsKey(firstTerm)) {
            AbstractPostingList list = index.termToPostingListMapping.get(firstTerm);
            // 遍历第一个单词的postingList，对其中每个posting进行判断，每个posting包含文件编号与出现位置
            for (int i = 0; i < list.size(); i++) {
                AbstractPosting posting = list.get(i);
                if (queryTerms.length == 1) {
                    tempMap.put(firstTerm, posting);
                    AbstractHit hit = new Hit(posting.getDocId(), index.docIdToDocPathMapping.get(posting.getDocId()), tempMap);
                    hit.setScore(posting.getFreq() * (not ? -1 : 1));
                    hits.add(hit);
                    continue;
                }
                List<Integer> positions = posting.getPositions();   // 第一个单词的位置
                List<Integer> newPositions = new ArrayList<>();     // 存短语的位置
                // 遍历每个posting的单词出现位置
                for (int position : positions) {

                    boolean isLegal = true; // 当前位置是否有合法短语
                    // 遍历短语中剩下的单词，判断其是否出现在同文件的下几个位置中
                    for (int j = 1; j < queryTerms.length; j++) {
                        AbstractTerm queryTerm = queryTerms[j];
                        if (index.termToPostingListMapping.containsKey(queryTerm)) {
                            AbstractPostingList list2 = index.termToPostingListMapping.get(queryTerm);
                            // 遍历后续单词的postingList
                            for (int k = 0; k < list2.size(); k++) {
                                AbstractPosting posting2 = list2.get(k);
//                                boolean hasFound = false;   // 是否找到满足位置要求的后续单词
                                // 首先需要出现在相同文件中
                                if (posting2.getDocId() == posting.getDocId()) {
                                    // 遍历当前单词在该文件中的出现位置
                                    List<Integer> positions2 = posting2.getPositions();
                                    for (int position2: positions2) {
                                        // 判断当前单词在该文件的当前位置是否满足要求，满足则置为true
                                        if (position2 == position + j) {
                                            isLegal = true;
                                            break;
                                        }
                                        isLegal = false;   // 对于未找到的，在退出循环前置为false，防止前面的单词置为true后，后续单词找不到却无法回到false
                                    }
                                    // 相同文件只会出现一次，因此该if最多进一次，可以直接break
                                    break;
                                }
                            }

                        } else {
                            break;
                        }
                        if(isLegal) {
                            newPositions.add(position);
                        }
                    }
                }
                if (newPositions.isEmpty()) {
                    continue;
                }
                AbstractPosting newPosting = new Posting(posting.getDocId(), newPositions.size(), newPositions);
                tempMap.put(firstTerm, newPosting);
                AbstractHit hit = new Hit(posting.getDocId(), index.docIdToDocPathMapping.get(posting.getDocId()), tempMap);
                hit.setScore(posting.getFreq() * (not ? -1 : 1));
                hits.add(hit);
            }
        }
        sorter.sort(hits);
        return hits.toArray(new AbstractHit[0]);
    }

    /**
     * 根据二个检索词进行搜索
     *
     * @param queryTerm1 ：第1个检索词
     * @param queryTerm2 ：第2个检索词
     * @param sorter     ：    排序器
     * @param combine    ：   多个检索词的逻辑组合方式
     * @return ：命中结果数组
     */
    @Override
    public AbstractHit[] search(AbstractTerm queryTerm1, AbstractTerm queryTerm2, Sort sorter, LogicalCombination combine) {
        AbstractHit[] hitsOfTerm1 = search(queryTerm1, sorter);
        AbstractHit[] hitsOfTerm2 = search(queryTerm2, sorter);
        return switch (combine) {
            case OR -> unionResultWithOR(queryTerm1, queryTerm2, hitsOfTerm1, hitsOfTerm2, sorter);
            case AND -> unionResultWithAND(queryTerm1, queryTerm2, hitsOfTerm1, hitsOfTerm2, sorter);
            case PHRASE -> unionResultWithAS_PHRASE(queryTerm1, queryTerm2, hitsOfTerm1, hitsOfTerm2, sorter);
            case NOT -> null;
        };
    }

    private String[][] readContents(String[] contents) {
        List<String[]> contentsList = new ArrayList<>();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i].equalsIgnoreCase("and")) {
                contentsList.add(new String[]{contents[i]});
            } else if(contents[i].equalsIgnoreCase("or")) {
                contentsList.add(new String[]{contents[i]});
            } else if(contents[i].equalsIgnoreCase("not")) {
                contentsList.add(new String[]{contents[i]});
            } else {
                List<String> strings = new ArrayList<>();
                strings.add(contents[i]);
                while(i + 1 < contents.length && !contents[i + 1].equalsIgnoreCase("and") && !contents[i + 1].equalsIgnoreCase("or") && !contents[i + 1].equalsIgnoreCase("not")) {
                    strings.add(contents[i + 1]);
                    i++;
                }
                contentsList.add(strings.toArray(new String[0]));
            }
        }
        return contentsList.toArray(new String[0][]);
    }
    @Override
    public AbstractHit[] search(String[] contents, Sort sorter) {
        boolean andFlag = false;
        boolean orFlag = false;
        boolean notFlag = false;
        Stack<AbstractTerm> termStack = new Stack<>();
        Stack<AbstractHit[]> hitsStack = new Stack<>();
        String[][] newContents = readContents(contents);
        for (int i = 0; i < newContents.length; i++) {
            if (getLogicalType(newContents[i]) == LogicalCombination.AND) {
                if(hitsStack.empty()) {
                    return null;
                }
                LogicalCombination nextType = getLogicalType(newContents[i + 1]);
                if(nextType == LogicalCombination.OR || nextType == LogicalCombination.AND) {
                    return null;
                }
                andFlag = true;
            } else if (getLogicalType(newContents[i]) == LogicalCombination.OR) {
                if(hitsStack.empty()) {
                    return null;
                }
                LogicalCombination nextType = getLogicalType(newContents[i + 1]);
                if(nextType == LogicalCombination.OR || nextType == LogicalCombination.AND) {
                    return null;
                }
                if(orFlag) {
                    AbstractTerm tmp = termStack.peek();
                    hitsStack.push(unionResultWithOR(termStack.pop(), termStack.pop(), hitsStack.pop(), hitsStack.pop(), sorter));
                    termStack.push(tmp);
                }
                orFlag = true;
            } else if (getLogicalType(newContents[i]) == LogicalCombination.NOT) {
                notFlag = true;
            } else {
                termStack.push(new Term(newContents[i][0]));
                hitsStack.push(search(getTerms(newContents[i]), sorter, notFlag));
                notFlag = false;
                if(andFlag) {
                    AbstractTerm tmp = termStack.peek();
                    hitsStack.push(unionResultWithAND(termStack.pop(), termStack.pop(), hitsStack.pop(), hitsStack.pop(), sorter));
                    termStack.push(tmp);
                    andFlag = false;
                }
            }
        }
        if(orFlag) {
            AbstractTerm tmp = termStack.peek();
            hitsStack.push(unionResultWithOR(termStack.pop(), termStack.pop(), hitsStack.pop(), hitsStack.pop(), sorter));
            termStack.push(tmp);
        }
        return hitsStack.pop();
    }

    private AbstractTerm[] getTerms(String[] contents) {
        List<AbstractTerm> terms = new ArrayList<>();
        for (String content : contents) {
            terms.add(new Term(content));
        }
        return terms.toArray(new AbstractTerm[0]);
    }

    private LogicalCombination getLogicalType(String[] contents) {
        if (contents.length == 1) {
            switch (contents[0]) {
                case "and":
                    return LogicalCombination.AND;
                case "or":
                    return LogicalCombination.OR;
                case "not":
                    return LogicalCombination.NOT;
            }
        }
        return LogicalCombination.PHRASE;
    }

    private AbstractHit[] unionResultWithOR(AbstractTerm queryTerm1, AbstractTerm queryTerm2, AbstractHit[] hitsOfTerm1, AbstractHit[] hitsOfTerm2, Sort sorter) {
        List<AbstractHit> hits = new ArrayList<>();
        int cnt1 = 0, cnt2 = 0;
        while (true) {
            AbstractHit hit1 = cnt1 < hitsOfTerm1.length ? hitsOfTerm1[cnt1] : null;
            AbstractHit hit2 = cnt2 < hitsOfTerm2.length ? hitsOfTerm2[cnt2] : null;
            if (hit1 == null && hit2 == null)
                break;
            else if (hit1 == null) {
                hits.add(hit2);
                cnt2++;
            } else if (hit2 == null) {
                hits.add(hit1);
                cnt1++;
            } else {
                if (hit1.getDocId() == hit2.getDocId()) {
                    Map<AbstractTerm, AbstractPosting> tempMap = new TreeMap<>();
                    tempMap.put(queryTerm1, hit1.getTermPostingMapping().get(queryTerm1));
                    tempMap.put(queryTerm2, hit2.getTermPostingMapping().get(queryTerm2));
                    AbstractHit newHit = new Hit(hit1.getDocId(), hit1.getDocPath(), tempMap, hit1.getScore() + hit2.getScore());
                    hits.add(newHit);
                    cnt1++;
                    cnt2++;
                } else if (hit1.getDocId() < hit2.getDocId()) {
                    hits.add(hit1);
                    cnt1++;
                } else {
                    hits.add(hit2);
                    cnt2++;
                }
            }
        }
        sorter.sort(hits);
        return hits.toArray(new AbstractHit[0]);
    }

    private AbstractHit[] unionResultWithAND(AbstractTerm queryTerm1, AbstractTerm queryTerm2, AbstractHit[] hitsOfTerm1, AbstractHit[] hitsOfTerm2, Sort sorter) {
        List<AbstractHit> hits = new ArrayList<>();
        int cnt1 = 0, cnt2 = 0;
        boolean not1 = false, not2 = false;
        while (true) {
            AbstractHit hit1 = cnt1 < hitsOfTerm1.length ? hitsOfTerm1[cnt1] : null;
            AbstractHit hit2 = cnt2 < hitsOfTerm2.length ? hitsOfTerm2[cnt2] : null;
            if (cnt1 == 0 && cnt2 == 0 && hit1 != null && hit2 != null) {
                not1 = !(hit1.getScore() > 0);
                not2 = !(hit2.getScore() > 0);
            }
            if (hit1 == null && hit2 != null) {
                if (not1) {
                    hits.add(hit2);
                    cnt2++;
                } else {
                    break;
                }
            }
            else if (hit1 != null && hit2 == null) {
                if (not2) {
                    hits.add(hit1);
                    cnt1++;
                } else {
                    break;
                }
            }
            else if (hit1 == null && hit2 == null) {
                break;
            }
            else {
                if (hit1.getDocId() == hit2.getDocId()) {
                    if(!not1 && !not2) {
                        Map<AbstractTerm, AbstractPosting> tempMap = new TreeMap<>();
                        tempMap.put(queryTerm1, hit1.getTermPostingMapping().get(queryTerm1));
                        tempMap.put(queryTerm2, hit2.getTermPostingMapping().get(queryTerm2));
                        AbstractHit newHit = new Hit(hit1.getDocId(), hit1.getDocPath(), tempMap, hit1.getScore() + hit2.getScore());
                        hits.add(newHit);
                    }
                    cnt1++;
                    cnt2++;
                } else if (hit1.getDocId() < hit2.getDocId()) {
                    if (!not2) {
                        hits.add(hit1);
                    }
                    cnt1++;
                }
                else {
                    if(!not1) {
                        hits.add(hit2);
                    }
                    cnt2++;
                }
            }
        }
        sorter.sort(hits);
        return hits.toArray(new AbstractHit[0]);
    }


    private AbstractHit[] unionResultWithAS_PHRASE(AbstractTerm queryTerm1, AbstractTerm queryTerm2, AbstractHit[] hitsOfTerm1, AbstractHit[] hitsOfTerm2, Sort sorter) {
        List<AbstractHit> hits = new ArrayList<>();
        int cnt1 = 0, cnt2 = 0;
        while (true) {
            AbstractHit hit1 = cnt1 < hitsOfTerm1.length ? hitsOfTerm1[cnt1] : null;
            AbstractHit hit2 = cnt2 < hitsOfTerm2.length ? hitsOfTerm2[cnt2] : null;
            if (hit1 == null || hit2 == null)
                break;
            else {
                if (hit1.getDocId() == hit2.getDocId()) {
                    if (hit1.getScore() > 0 && hit2.getScore() > 0) {
                        float newScore = 0;
                        List<Integer> positions1 = hit1.getTermPostingMapping().get(queryTerm1).getPositions();
                        List<Integer> positions2 = hit2.getTermPostingMapping().get(queryTerm2).getPositions();
                        for (Integer position : positions1)
                            if (positions2.contains(position + 1))
                                newScore += 1;
                        if (newScore > 0) {
                            Map<AbstractTerm, AbstractPosting> tempMap = new TreeMap<>();
                            tempMap.put(new Term(queryTerm1.getContent() + " " + queryTerm2.getContent()), hit1.getTermPostingMapping().get(queryTerm1));
                            AbstractHit newHit = new Hit(hit1.getDocId(), hit1.getDocPath(), tempMap, newScore);
                            hits.add(newHit);
                        }
                    }
                    cnt1++;
                    cnt2++;
                } else if (hit1.getDocId() < hit2.getDocId())
                    cnt1++;
                else
                    cnt2++;
            }
        }
        sorter.sort(hits);
        return hits.toArray(new AbstractHit[0]);
    }

    public static void main(String[] args) {
        IndexSearcher searcher = new IndexSearcher();
        System.out.println(Arrays.deepToString(searcher.readContents(new String[]{"word1", "and", "word2", "word3", "or", "not", "word4", "word5", "word6"})));
    }


}
