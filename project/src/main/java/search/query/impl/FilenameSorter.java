package search.query.impl;

import search.index.AbstractPosting;
import search.query.AbstractHit;
import search.query.Sort;

import java.util.Comparator;
import java.util.List;

public class FilenameSorter implements Sort {
    @Override
    public void sort(List<AbstractHit> hits) {
//        for (AbstractHit hit : hits) {
//            hit.setScore(score(hit));
//        }
        hits.sort(new Comparator<>() {
            @Override
            public int compare(AbstractHit o1, AbstractHit o2) {
                return o1.getDocPath().compareTo(o2.getDocPath());
            }
        });
    }

    @Override
    public double score(AbstractHit hit) {
        double score = 0;
        for (AbstractPosting posting : hit.getTermPostingMapping().values())
            score += posting.getFreq();
        return score;
    }
}
