
package com.uib.onlinepeptideshaker.model.beans;

/**
 *This class represents index point used as start point  for 
 * @author Yehia Farag
 */
public class IndexPoint implements Comparable<IndexPoint>{
    private Long startPoint;
    private int length;

    public long getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(long startPoint) {
        this.startPoint = startPoint;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    @Override
    public int compareTo(IndexPoint o) {
        return this.startPoint.compareTo(o.startPoint);
    }
    
    
}
