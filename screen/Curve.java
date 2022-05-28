package dev.rabies.twinsight.hcaptcha.screen;

import com.jme3.math.Vector2f;
import dev.rabies.twinsight.hcaptcha.HCaptchaUtils;
import lombok.Getter;
import org.apache.commons.lang3.RandomUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Curve {

    @Getter
    private final Vector2f fromPoint;
    @Getter
    private final Vector2f toPoint;
    @Getter
    private final List<Vector2f> points;

    public Curve(Vector2f fromPoint, Vector2f toPoint, CurveOptions opts) {
        this.fromPoint = fromPoint;
        this.toPoint = toPoint;
        this.points = generateCurve(opts);
    }

    private List<Vector2f> generateCurve(CurveOptions opts) {
        opts.defaultCurveOpts(this, opts);

        int offsetBoundaryX = opts.getOffsetBoundaryX();
        int offsetBoundaryY = opts.getOffsetBoundaryY();

        int leftBoundary = opts.getLeftBoundary() - offsetBoundaryY;
        int rightBoundary = opts.getRightBoundary() + offsetBoundaryX;
        int upBoundary = opts.getUpBoundary() - offsetBoundaryY;
        int downBoundary = opts.getDownBoundary() + offsetBoundaryY;
        int count = opts.getKnotsCount();
        float distortionMean = opts.getDistortionMean();
        float distortionStdDev = opts.getDistortionStdDev();
        float distortionFrequency = opts.getDistortionFrequency();
        int targetPoints = opts.getTargetPoints();

        List<Vector2f> internalKnots = generateInternalKnots(leftBoundary, rightBoundary, upBoundary, downBoundary, count);
        List<Vector2f> points = generatePoints(internalKnots);
        points = distortPoints(points, distortionMean, distortionStdDev, distortionFrequency);
        points = tweenPoints(points, targetPoints);
        return points;
    }

    private List<Vector2f> generateInternalKnots(int leftBoundary, int rightBoundary,
                                              int upBoundary,int downBoundary, int knotsCount) {
        if (knotsCount < 0) {
            throw new RuntimeException("knotsCount can't be negative");
        }
        if (leftBoundary > rightBoundary) {
            throw new RuntimeException("leftBoundary must be less than or equal to rightBoundary");
        }
        if (downBoundary > upBoundary) {
            throw new RuntimeException("downBoundary must be less than or equal to upBoundary");
        }

        List<Integer> knotsX = CurveUtil.knots(leftBoundary, rightBoundary, knotsCount);
        List<Integer> knotsY = CurveUtil.knots(downBoundary, upBoundary, knotsCount);
        return CurveUtil.merge(knotsX, knotsY);
    }

    private List<Vector2f> generatePoints(List<Vector2f> knots) {
        int midPointsCount = (int) Math.max(Math.max(Math.abs(fromPoint.x - toPoint.x), Math.abs(fromPoint.y - toPoint.y)), 2);
        List<Vector2f> ret = new ArrayList<>();
        ret.add(fromPoint);
        knots.add(toPoint);
        ret.addAll(knots);
        return CurveUtil.makeBezierCurve2d(midPointsCount, ret);
    }

    private List<Vector2f> distortPoints(List<Vector2f> points, float distortionMean, float distortionStdDev, float distortionFrequency) {
        if (distortionFrequency < 0 || distortionFrequency > 1) {
            throw new RuntimeException("distortionFrequency must be between 0 and 1");
        }

        List<Vector2f> distortedPoints = new ArrayList<>(points.size());
        for (int i = 1; i < points.size() - 1; i++) {
            Vector2f point = points.get(i);
            if (HCaptchaUtils.chance(distortionFrequency)) {
                float delta = RandomUtils.nextFloat() * distortionStdDev + distortionMean;
                distortedPoints.add(new Vector2f(point.x, point.y + delta));
            } else {
                distortedPoints.add(point);
            }
        }

        List<Vector2f> ret = new ArrayList<>();
        ret.add(points.get(0));
        distortedPoints.add(points.get(points.size() - 1));
        ret.addAll(distortedPoints);
        return ret;
    }

    private List<Vector2f> tweenPoints(List<Vector2f> points, int targetPoints) {
        if (targetPoints < 2) {
            throw new RuntimeException("targetPoints must be at least 2");
        }

        List<Vector2f> tweenPoints = new ArrayList<>();
        for (int i = 0; i < targetPoints; i++) {
            int index = (int) (CurveUtil.tween(i / (targetPoints - 1.0F)) * (points.size() - 1));
            tweenPoints.add(points.get(index));
        }
        return tweenPoints;
    }
}
