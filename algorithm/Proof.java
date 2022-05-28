package dev.rabies.twinsight.hcaptcha.algorithm;

import lombok.Data;

@Data
public class Proof {

    private final Algorithm algorithm;
    private final String request;
    private final String proof;

}
