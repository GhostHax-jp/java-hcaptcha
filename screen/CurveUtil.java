package dev.rabies.twinsight.hcaptcha.screen;

import com.jme3.math.Vector2f;
import dev.rabies.twinsight.hcaptcha.HCaptcha;
import dev.rabies.twinsight.hcaptcha.HCaptchaUtils;

import java.util.ArrayList;
import java.util.List;

public class CurveUtil {

    public static List<Vector2f> merge(List<Integer> a, List<Integer> b) {
        if (a.size() != b.size()) {
            throw new RuntimeException("arguments must be of same length");
        }

        List<Vector2f> r = new ArrayList<>();
        for (int i = 0; i < a.size(); i++) {
            r.add(new Vector2f(a.get(i), b.get(i)));
        }
        return r;
    }

    public static List<Integer> knots(int firstBoundary, int secondBoundary, int size) {
        List<Integer> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(HCaptchaUtils.between(firstBoundary, secondBoundary));
        }
        return result;
    }

    public static float tween(float n) {
        if (n < 0 || n > 1) {
            throw new RuntimeException("parameter must be between 0.0 and 1.0");
        }
        return -n * (n - 2);
    }

    public static List<Vector2f> makeBezierCurve2d(int points, List<Vector2f> cPoints) {
        List<Vector2f> line = new ArrayList<>(points);
        if (points == 0) return null;
        if (points == 1) {
            line.add(cPoints.get(0));
            return line;
        } else if (points == 2) {
            line.add(cPoints.get(0));
            line.add(cPoints.get(cPoints.size() - 1));
            return line;
        }

        line.add(cPoints.get(0));
        for (int i = 1; i < points - 1; i++) {
            line.add(bezierCurve2d(clamp(i / (points - 1.0F), 0.0F, 1.0F), cPoints));
        }
        return line;
    }

    private static Vector2f bezierCurve2d(float t, List<Vector2f> cPoints) {
        if (t < 0.0 || t > 1.0) {
            throw new RuntimeException("Input to bezier has t not in range [0,1]. If you think this is a precision error, use mathgl.Clamp[f|d] before calling this function");
        }

        int n = cPoints.size() - 1;
        Vector2f point = cPoints.get(0).mult((float) Math.pow(1.0 - t, n));
        for (int i = 1; i < n; i++) {
            point = point.add(cPoints.get(i).mult((float) (choose(n, i) * Math.pow(1 - t, n - i) * Math.pow(t, i))));
        }
        return point;
    }

    private static int choose(int n, int k) {
        if (k == 0) {
            return 1;
        } else if (n == 0) {
            return 0;
        }
        float result = n - (k - 1);
        for (int i = 2; i < k; i++) {
            result *= (double) (n - (k - i)) / i;
        }
        return (int) result;
    }

    public static float clamp(float num, float min, float max) {
        if (num < min) {
            return min;
        } else {
            return Math.min(num, max);
        }
    }
}
