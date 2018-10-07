package com.google.zxing;

import com.google.zxing.common.detector.MathUtils;

public class ResultPoint {
    /* renamed from: x */
    private final float f74x;
    /* renamed from: y */
    private final float f75y;

    public ResultPoint(float x, float y) {
        this.f74x = x;
        this.f75y = y;
    }

    public final float getX() {
        return this.f74x;
    }

    public final float getY() {
        return this.f75y;
    }

    public final boolean equals(Object other) {
        if (!(other instanceof ResultPoint)) {
            return false;
        }
        ResultPoint otherPoint = (ResultPoint) other;
        if (this.f74x == otherPoint.f74x && this.f75y == otherPoint.f75y) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return (Float.floatToIntBits(this.f74x) * 31) + Float.floatToIntBits(this.f75y);
    }

    public final String toString() {
        return "(" + this.f74x + ',' + this.f75y + ')';
    }

    public static void orderBestPatterns(ResultPoint[] patterns) {
        ResultPoint pointB;
        ResultPoint pointA;
        ResultPoint pointC;
        float zeroOneDistance = distance(patterns[0], patterns[1]);
        float oneTwoDistance = distance(patterns[1], patterns[2]);
        float zeroTwoDistance = distance(patterns[0], patterns[2]);
        if (oneTwoDistance >= zeroOneDistance && oneTwoDistance >= zeroTwoDistance) {
            pointB = patterns[0];
            pointA = patterns[1];
            pointC = patterns[2];
        } else if (zeroTwoDistance < oneTwoDistance || zeroTwoDistance < zeroOneDistance) {
            pointB = patterns[2];
            pointA = patterns[0];
            pointC = patterns[1];
        } else {
            pointB = patterns[1];
            pointA = patterns[0];
            pointC = patterns[2];
        }
        if (crossProductZ(pointA, pointB, pointC) < 0.0f) {
            ResultPoint temp = pointA;
            pointA = pointC;
            pointC = temp;
        }
        patterns[0] = pointA;
        patterns[1] = pointB;
        patterns[2] = pointC;
    }

    public static float distance(ResultPoint pattern1, ResultPoint pattern2) {
        return MathUtils.distance(pattern1.f74x, pattern1.f75y, pattern2.f74x, pattern2.f75y);
    }

    private static float crossProductZ(ResultPoint pointA, ResultPoint pointB, ResultPoint pointC) {
        float bX = pointB.f74x;
        float bY = pointB.f75y;
        return ((pointC.f74x - bX) * (pointA.f75y - bY)) - ((pointC.f75y - bY) * (pointA.f74x - bX));
    }
}
