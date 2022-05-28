package dev.rabies.twinsight.hcaptcha.algorithm;

import dev.rabies.twinsight.hcaptcha.HCaptcha;
import dev.rabies.twinsight.hcaptcha.HCaptchaUtils;
import dev.rabies.twinsight.agents.Agent;
import dev.rabies.twinsight.hcaptcha.challenge.Challenge;
import dev.rabies.twinsight.http.response.CustomHttpResponse;
import dev.rabies.twinsight.solver.ChromeHswSolver;
import dev.rabies.twinsight.solver.HswSolver;
import dev.rabies.twinsight.solver.OrbitaHswSolver;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AlgorithmHelper {
    private final Map<String, Algorithm> algorithms = new HashMap<>();
    @Getter private final Challenge challenge;
    @Getter private Agent agent;

    @Getter @Setter
    private HswSolver hswSolver;
    @Getter private String hswScriptCache;

    public AlgorithmHelper(Challenge challenge) {
        this.challenge = challenge;
        this.hswSolver = new OrbitaHswSolver(1548, challenge.getHswMode());
//        this.hswSolver = new ChromeHswSolver(challenge.getAgent(), challenge.getHswMode());
        registerAlgorithm(new Hsw());
        registerAlgorithm(new Hsl());
//        clearHswScriptCache();
    }

    public Proof solve(Agent agent, String algorithm, String request) throws IOException {
        this.agent = agent;
        Algorithm algo = findAlgorithm(algorithm);
        String proof = algo.prove(challenge, request);
        HCaptcha.getLogger().info("%s %s...".formatted(algo.encode(), proof.substring(0, 30)));
        return new Proof(algo, new JSONObject().put("type", algo.encode()).put("req", request).toString(), proof);
    }

    public void updateHswScript() throws IOException {
        HCaptcha.getLogger().info("Update hsw cache");
        hswScriptCache = script("hsw.js");
    }

    public void clearHswScriptCache() {
        HCaptcha.getLogger().info("Clear hsw cache");
        hswScriptCache = null;
    }

    public String script(String script) throws IOException {
        CustomHttpResponse response = HCaptcha.getHttp().get(
                "https://newassets.hcaptcha.com/c/%s/%s".formatted(HCaptchaUtils.getAssetVersion(), script), it -> {
            it.addHeader("Authority", "hcaptcha.com");
            it.addHeader("Accept", "*/*");
            it.addHeader("User-Agent", agent.userAgent());
        });
        return response.getResponseBody();
    }

    public Algorithm findAlgorithm(String name) {
        return algorithms.getOrDefault(name, new Hsw());
    }

    private void registerAlgorithm(Algorithm algorithm) {
        if (algorithms.get(algorithm.encode()) != null) return;
        algorithms.put(algorithm.encode(), algorithm);
    }
}
