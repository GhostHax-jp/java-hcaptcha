package dev.rabies.twinsight.hcaptcha;

import dev.rabies.twinsight.agents.Agent;
import dev.rabies.twinsight.agents.ChromeAgent;
import dev.rabies.twinsight.hcaptcha.algorithm.AlgorithmHelper;
import dev.rabies.twinsight.hcaptcha.challenge.Challenge;
import dev.rabies.twinsight.http.DefaultHttpClient;
import dev.rabies.twinsight.http.HttpClientWrapper;
import dev.rabies.twinsight.proxy.RemoteProxy;
import dev.rabies.twinsight.solver.HswMode;
import dev.rabies.twinsight.utils.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

public class HCaptcha {

    private static final Logger logger = LoggerFactory.getLogger("[HCaptcha-Solver]");
    private static HttpClientWrapper http;
    private final Challenge challenge;

    public HCaptcha(RemoteProxy proxy) {
        DefaultHttpClient httpClient = DefaultHttpClient.newHttpClient(proxy)
                .useFirefoxTls().build();
        http = httpClient.getHttpClientWrapper();
        challenge = new Challenge();
        challenge.setWidgetId(HCaptchaUtils.getWidgetId());
        challenge.setUseAccessibility(false);
        setAgent(httpClient.getAgent());
        setDr("https://google.com/");
        setHswMode(HswMode.NORMAL);
    }

    public static HCaptcha newBuilder(RemoteProxy proxy) {
        return new HCaptcha(proxy);
    }

    public HCaptcha setHost(String host) {
        challenge.setHost(host.split("://")[1].split("/")[0].toLowerCase());
        challenge.setUrl(host);
        return this;
    }

    public HCaptcha setSiteKey(String siteKey) {
        challenge.setSiteKey(siteKey);
        return this;
    }

    public HCaptcha setUrl(String url) {
        challenge.setUrl(url);
        return this;
    }

    public HCaptcha setAgent(Agent agent) {
        challenge.setAgent(agent);
        challenge.getAgent().setUnixOffset(-10);
        return this;
    }

    public HCaptcha setDr(String dr) {
        if (!HttpUtils.isBlank(dr)) challenge.setDr(dr);
        return this;
    }

    public HCaptcha useAccessibility() {
        challenge.setUseAccessibility(true);
        return this;
    }

    public HCaptcha setHswMode(HswMode mode) {
        challenge.setHswMode(mode);
        return this;
    }

    public Challenge build() throws IOException {
        challenge.setTasks(new LinkedList<>());
        challenge.setupFrames();
        HCaptcha.getLogger().debug("Verifying site configuration.");
        challenge.siteConfig();
        HCaptcha.getLogger().debug("Requesting captcha.");
        challenge.requestCaptcha();
        return challenge;
    }

    public static HttpClientWrapper getHttp() {
        return http;
    }

    public static Logger getLogger() {
        return logger;
    }
}
