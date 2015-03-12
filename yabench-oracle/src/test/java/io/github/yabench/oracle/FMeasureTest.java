package io.github.yabench.oracle;

import org.junit.Test;
import static org.junit.Assert.*;

public class FMeasureTest {
    
    private static final double DELTA = 0.0;

    @Test
    public void equalEmptyArrays() {
        FMeasure fm = new FMeasure();
        fm.updateScores(new String[]{}, new String[]{});

        assertEquals(0.0, fm.getRecallScore(), DELTA);
        assertEquals(0.0, fm.getPrecisionScore(), DELTA);
        System.out.println(fm);
    }

    @Test
    public void equalArraysWithOneItem() {
        FMeasure fm = new FMeasure();
        fm.updateScores(new String[]{"1"}, new String[]{"1"});

        assertEquals(1.0, fm.getRecallScore(), DELTA);
        assertEquals(1.0, fm.getPrecisionScore(), DELTA);
        System.out.println(fm);

        fm = new FMeasure();
        fm.updateScores(new Integer[]{1}, new Integer[]{1});

        assertEquals(1.0, fm.getRecallScore(), DELTA);
        assertEquals(1.0, fm.getPrecisionScore(), DELTA);
        System.out.println(fm);
    }

    @Test
    public void equalArraysWithMoreItems() {
        FMeasure fm = new FMeasure();
        fm.updateScores(new String[]{"1", "1", "1", "1", "1", "1", "1", "1", "1"},
                new String[]{"1", "1", "1", "1", "1", "1", "1", "1", "1"});

        assertEquals(1.0, fm.getRecallScore(), DELTA);
        assertEquals(1.0, fm.getPrecisionScore(), DELTA);
        System.out.println(fm);

        fm = new FMeasure();
        fm.updateScores(new Integer[]{1, 1, 1, 1, 1, 1, 1, 1, 1},
                new Integer[]{1, 1, 1, 1, 1, 1, 1, 1, 1});

        assertEquals(1.0, fm.getRecallScore(), DELTA);
        assertEquals(1.0, fm.getPrecisionScore(), DELTA);
        System.out.println(fm);
    }

    @Test
    public void equalArraysWithLotsOfItems() {
        FMeasure fm = new FMeasure();
        Integer[] a = fillByRange(new Integer[1000], 0, 1000);
        Integer[] b = fillByRange(new Integer[1000], 0, 1000);
        fm.updateScores(a, b);

        assertEquals(1.0, fm.getRecallScore(), DELTA);
        assertEquals(1.0, fm.getPrecisionScore(), DELTA);
        System.out.println(fm);
    }

    @Test
    public void nonequalArraysWithLotsOfItems99() {
        FMeasure fm = new FMeasure();
        Integer[] a = fillByRange(new Integer[1000], 1, 1001);
        Integer[] b = fillByRange(new Integer[1000], 0, 1000);
        fm.updateScores(a, b);

        assertEquals(0.999, fm.getRecallScore(), DELTA);
        assertEquals(0.999, fm.getPrecisionScore(), DELTA);
        System.out.println(fm);
    }

    @Test
    public void nonequalArraysWithDifferentSizes() {
        FMeasure fm = new FMeasure();
        Integer[] a = fillByRange(new Integer[900], 0, 1000);
        Integer[] b = fillByRange(new Integer[1000], 0, 1000);
        fm.updateScores(a, b);

        assertEquals(1.0, fm.getRecallScore(), DELTA);
        assertEquals(0.9, fm.getPrecisionScore(), DELTA);
        System.out.println(fm);
    }

    @Test
    public void nonequalArraysWithLotsOfItems() {
        FMeasure fm = new FMeasure();
        Integer[] a = fillByRange(new Integer[1000], 500, 1500);
        Integer[] b = fillByRange(new Integer[1000], 0, 1000);
        fm.updateScores(a, b);

        assertEquals(0.5, fm.getRecallScore(), DELTA);
        assertEquals(0.5, fm.getPrecisionScore(), DELTA);
        System.out.println(fm);
    }

    private Integer[] fillByRange(Integer[] array, int begin, int end) {
        for (int i = 0; i < array.length || i > end; i++) {
            array[i] = begin + i;
        }
        return array;
    }

}
