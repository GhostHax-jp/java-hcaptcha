package dev.rabies.twinsight.hcaptcha;

import dev.rabies.twinsight.http.response.CustomHttpResponse;
import dev.rabies.twinsight.http.DefaultHttpClient;
import dev.rabies.twinsight.proxy.NoProxy;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Base64;

public class HCaptchaVersion {

    private static final DefaultHttpClient httpClient;

    public static String fetchVersion() {
        try {
            String body = httpClient.get("https://hcaptcha.com/1/api.js", it -> {
                it.addHeader("Accept", "*/*");
                it.addHeader("User-Agent", httpClient.getAgent().userAgent());
            }).getResponseBody();
            return body.split("v1/")[1].split("/static")[0];
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String fetchAssetsVersion() {
        try {
            CustomHttpResponse response = httpClient.get("https://hcaptcha.com/checksiteconfig", it -> {
                it.addParameter("v", HCaptchaUtils.getVersion());
                it.addParameter("host", "dashboard.hcaptcha.com");
                it.addParameter("sitekey", "13257c82-e129-4f09-a733-2a7cb3102832");
                it.addParameter("sc", "1");
                it.addParameter("swa", "1");

                it.addHeader("Accept", "*/*");
                it.addHeader("User-Agent", httpClient.getAgent().userAgent());
                it.addHeader("Content-Type", "application/json");
            });
            if (response.getResponseCode() != 200) return "";
            JSONObject responseObject = new JSONObject(response.getResponseBody());
            String jwt = responseObject.getJSONObject("c").getString("req");
            String decode = new String(Base64.getDecoder().decode(jwt.split("\\.")[1]));
            return decode.split("/c/")[1].split("\"")[0];
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    static {
        httpClient = DefaultHttpClient
                .newHttpClient(new NoProxy())
                .build();
    }
}
