package dev.rabies.twinsight.hcaptcha.screen;

import dev.rabies.twinsight.hcaptcha.HCaptchaUtils;
import lombok.Data;

@Data
public class CurveOptions {

    private int offsetBoundaryX;
    private int offsetBoundaryY;

    private int leftBoundary;
    private int rightBoundary;

    private int upBoundary;
    private int downBoundary;

    private int knotsCount;
    private float distortionMean;
    private float distortionStdDev;
    private float distortionFrequency;
    private int targetPoints;

    public void defaultCurveOpts(Curve curve, CurveOptions opts) {
        int defaultOffsetBoundaryX = 100;
        if (opts.offsetBoundaryX == 0) {
            opts.offsetBoundaryX = defaultOffsetBoundaryX;
        }

        int defaultOffsetBoundaryY = 100;
        if (opts.offsetBoundaryY == 0) {
            opts.offsetBoundaryY = defaultOffsetBoundaryY;
        }

        int defaultLeftBoundary = (int) Math.min(curve.getFromPoint().x, curve.getToPoint().x);
        if (opts.leftBoundary == 0) {
            opts.leftBoundary = defaultLeftBoundary;
        }

        int defaultRightBoundary = (int) Math.max(curve.getFromPoint().x, curve.getToPoint().x);
        if (opts.rightBoundary == 0) {
            opts.rightBoundary = defaultRightBoundary;
        }

        int defaultUpBoundary = (int) Math.max(curve.getFromPoint().y, curve.getToPoint().y);
        if (opts.upBoundary == 0) {
            opts.upBoundary = defaultUpBoundary;
        }

        int defaultDownBoundary = (int) Math.min(curve.getFromPoint().y, curve.getToPoint().y);
        if (opts.downBoundary == 0) {
            opts.downBoundary = defaultDownBoundary;
        }

        int defaultKnotsCount = 2;
        if (opts.knotsCount == 0) {
            opts.knotsCount = defaultKnotsCount;
        }

        float defaultDistortionMean = 1.0F;
        if (opts.distortionMean == 0.0F) {
            opts.distortionMean = defaultDistortionMean;
        }

        float defaultDistortionStdDev = 0.6F;
        if (opts.distortionStdDev == 0.0F) {
            opts.distortionStdDev = defaultDistortionStdDev;
        }

        float defaultDistortionFrequency = 0.5F;
        if (opts.distortionFrequency == 0.0F) {
            opts.distortionFrequency = defaultDistortionFrequency;
        }

        int defaultTargetPoints = HCaptchaUtils.between(266, 424);
        if (opts.targetPoints == 0) {
            opts.targetPoints = defaultTargetPoints;
        }
    }
}
