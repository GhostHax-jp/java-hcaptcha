package dev.rabies.twinsight.hcaptcha.challenge;

import com.jme3.math.Vector2f;
import lombok.Data;

@Data
public class Movement {

    private final Vector2f point;
    private final long timestamp;

}
