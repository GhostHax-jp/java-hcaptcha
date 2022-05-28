package dev.rabies.twinsight.hcaptcha;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import java.io.IOException;
import java.util.Random;

public class HCaptchaUtils {

    private static String version;
    private static String assetsVersion;

    public static String getVersion() {
        if (version == null)
            version = HCaptchaVersion.fetchVersion();
        return version;
    }

    public static String getAssetVersion() {
        if (assetsVersion == null)
            assetsVersion = HCaptchaVersion.fetchAssetsVersion();
        return assetsVersion;
    }

    public static int[] getFrameSize() {
        return new int[]{400, 600};
    }

    public static int[] getTileImageSize() {
        return new int[]{123, 123};
    }

    public static int[] getTileImageStartPos() {
        return new int[]{11, 130};
    }

    public static int[] getTileImagePadding() {
        return new int[]{5, 6};
    }

    public static int[] getVerifyButtonPos() {
        return new int[]{314, 559};
    }

    public static int getTilesPerPage() {
        return 9;
    }

    public static int getTilesPerRow() {
        return 3;
    }

    public static String getWidgetId() {
        return RandomStringUtils.randomAlphanumeric(RandomUtils.nextInt(10, 12)).toLowerCase();
    }

    public static int between(int min, int max) {
        return new Random().nextInt(max - min) + min;
    }

    public static boolean chance(float chance) {
        return new Random().nextFloat() < chance;
    }
}
