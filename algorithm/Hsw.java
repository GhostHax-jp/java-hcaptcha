package dev.rabies.twinsight.hcaptcha.algorithm;

import dev.rabies.twinsight.hcaptcha.challenge.Challenge;
import dev.rabies.twinsight.utils.HttpUtils;

import java.io.IOException;

public class Hsw implements Algorithm {

    @Override
    public String encode() {
        return "hsw";
    }

    @Override
    public String prove(Challenge challenge, String request) throws IOException {
        AlgorithmHelper helper = challenge.getAlgorithmHelper();
        if (HttpUtils.isBlank(helper.getHswScriptCache()))
            helper.updateHswScript();
        return helper.getHswSolver().solve(helper.getHswScriptCache(), request);
    }
}
