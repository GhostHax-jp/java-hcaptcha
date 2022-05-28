package dev.rabies.twinsight.hcaptcha.algorithm;

import dev.rabies.twinsight.hcaptcha.challenge.Challenge;

import java.io.IOException;

public interface Algorithm {

    String encode();

    String prove(Challenge challenge, String request) throws IOException;

}
