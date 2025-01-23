package search.query.impl;

import search.query.AbstractHit;
import search.query.Sort;

import java.util.Comparator;
import java.util.List;

public class LengthSorter implements Sort {
    @Override
    public void sort(List<AbstractHit> hits) {
        for (AbstractHit hit : hits) {
            hit.setScore(score(hit));
        }
        hits.sort(new Comparator<>() {
            @Override
            public int compare(AbstractHit o1, AbstractHit o2) {
                return o1.compareTo(o2);
            }
        });
    }

    @Override
    public double score(AbstractHit hit) {
        double score;
        score = hit.getContent().length();
        return -score;
    }
}
