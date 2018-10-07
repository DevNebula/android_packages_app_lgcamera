package android.support.p000v4.view.animation;

import android.annotation.TargetApi;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.support.annotation.RequiresApi;
import android.view.animation.Interpolator;

@TargetApi(9)
@RequiresApi(9)
/* renamed from: android.support.v4.view.animation.PathInterpolatorGingerbread */
class PathInterpolatorGingerbread implements Interpolator {
    private static final float PRECISION = 0.002f;
    /* renamed from: mX */
    private final float[] f70mX;
    /* renamed from: mY */
    private final float[] f71mY;

    public PathInterpolatorGingerbread(Path path) {
        PathMeasure pathMeasure = new PathMeasure(path, false);
        float pathLength = pathMeasure.getLength();
        int numPoints = ((int) (pathLength / PRECISION)) + 1;
        this.f70mX = new float[numPoints];
        this.f71mY = new float[numPoints];
        float[] position = new float[2];
        for (int i = 0; i < numPoints; i++) {
            pathMeasure.getPosTan((((float) i) * pathLength) / ((float) (numPoints - 1)), position, null);
            this.f70mX[i] = position[0];
            this.f71mY[i] = position[1];
        }
    }

    public PathInterpolatorGingerbread(float controlX, float controlY) {
        this(PathInterpolatorGingerbread.createQuad(controlX, controlY));
    }

    public PathInterpolatorGingerbread(float controlX1, float controlY1, float controlX2, float controlY2) {
        this(PathInterpolatorGingerbread.createCubic(controlX1, controlY1, controlX2, controlY2));
    }

    public float getInterpolation(float t) {
        if (t <= 0.0f) {
            return 0.0f;
        }
        if (t >= 1.0f) {
            return 1.0f;
        }
        int startIndex = 0;
        int endIndex = this.f70mX.length - 1;
        while (endIndex - startIndex > 1) {
            int midIndex = (startIndex + endIndex) / 2;
            if (t < this.f70mX[midIndex]) {
                endIndex = midIndex;
            } else {
                startIndex = midIndex;
            }
        }
        float xRange = this.f70mX[endIndex] - this.f70mX[startIndex];
        if (xRange == 0.0f) {
            return this.f71mY[startIndex];
        }
        float fraction = (t - this.f70mX[startIndex]) / xRange;
        float startY = this.f71mY[startIndex];
        return ((this.f71mY[endIndex] - startY) * fraction) + startY;
    }

    private static Path createQuad(float controlX, float controlY) {
        Path path = new Path();
        path.moveTo(0.0f, 0.0f);
        path.quadTo(controlX, controlY, 1.0f, 1.0f);
        return path;
    }

    private static Path createCubic(float controlX1, float controlY1, float controlX2, float controlY2) {
        Path path = new Path();
        path.moveTo(0.0f, 0.0f);
        path.cubicTo(controlX1, controlY1, controlX2, controlY2, 1.0f, 1.0f);
        return path;
    }
}
