package dev.rabies.twinsight.hcaptcha.event;

import dev.rabies.twinsight.hcaptcha.HCaptcha;
import dev.rabies.twinsight.agents.Agent;
import lombok.Data;
import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.collections4.map.ListOrderedMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Data
public class EventRecorder {

    private final Agent agent;
    private final OrderedMap<String, Object> manifest;
    private final Map<String, EventContainer> timeBuffers;
    private boolean recording;

    public EventRecorder(Agent agent) {
        this.agent = agent;
        this.manifest = new ListOrderedMap<>();
        this.timeBuffers = new HashMap<>();
    }

    public void record(int flag) {
        manifest.put("st", agent.unix());
//        if (flag == 1) {
//            manifest.put("dct", agent.unix());
//        } else if (flag == 0) {
//            manifest.put("wn", new ArrayList<>());
//            manifest.put("xy", new ArrayList<>());
//        }
        recording = true;
    }

    public void recordEvent(Event event) {
        if (!recording) return;
        if (timeBuffers.get(event.getType()) == null) {
            timeBuffers.put(event.getType(), new EventContainer(agent, 16, 15000)); // 15E3
        }
        timeBuffers.get(event.getType()).push(event);
    }

    public OrderedMap<String, Object> getData() {
        for (Map.Entry<String, EventContainer> entry : timeBuffers.entrySet()) {
            String event = entry.getKey();
            EventContainer container = entry.getValue();
            manifest.put(event, container.getData());
            manifest.put(event + "-mp", container.getMeanPeriod());
            HCaptcha.getLogger().debug("MP-" + container.getMeanPeriod());
        }
        return manifest;
    }

    public void setData(String name, Object value) {
        manifest.put(name, value);
    }
}
