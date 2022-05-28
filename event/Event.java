package dev.rabies.twinsight.hcaptcha.event;

import com.jme3.math.Vector2f;
import lombok.Data;

@Data
public class Event {

    private final Vector2f point;
    private final String type;
    private final long timestamp;

}
