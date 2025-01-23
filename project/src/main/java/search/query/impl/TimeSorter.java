package search.query.impl;

import search.query.AbstractHit;
import search.query.Sort;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.List;

public class TimeSorter implements Sort {
    @Override
    public void sort(List<AbstractHit> hits) {
//        for (AbstractHit hit : hits) {
//            hit.setScore(score(hit));
//        }
        hits.sort(new Comparator<>() {
            @Override
            public int compare(AbstractHit o1, AbstractHit o2) {
                try {
                    return Files.readAttributes(Paths.get(o1.getDocPath()), BasicFileAttributes.class).lastModifiedTime().compareTo(Files.readAttributes(Paths.get(o2.getDocPath()), BasicFileAttributes.class).lastModifiedTime());
                } catch (Exception e) {
                    return 0;
                }
            }
        });
    }

    @Override
    public double score(AbstractHit hit) {
        return 0;
    }
}
