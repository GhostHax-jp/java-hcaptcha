package dev.rabies.twinsight.hcaptcha.event;

import dev.rabies.twinsight.agents.Agent;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EventContainer {

    private final Agent agent;
    private final int period;
    private final int interval;
    private List<Long> date;
    private List<Object[]> data;
    private long previousTimestamp;
    private long meanPeriod;
    private int meanCounter;

    public EventContainer(Agent agent, int period, int interval) {
        this.agent = agent;
        this.period = period;
        this.interval = interval;

        this.date = new ArrayList<>();
        this.data = new ArrayList<>();
    }

    public List<Object[]> getData() {
        cleanStaleData();
        return data;
    }

    public void push(Event event) {
        cleanStaleData();

        long timestamp = 0;
        boolean notFirst = date.size() > 0;
        if (notFirst) {
            timestamp = date.get(date.size() - 1);
        }

        if (event.getTimestamp() - timestamp >= period) {
            date.add(event.getTimestamp());
            data.add(new Object[]{(int) event.getPoint().getX(), (int) event.getPoint().getY(), event.getTimestamp()});

            if (notFirst) {
                long delta = event.getTimestamp() - previousTimestamp;
                meanPeriod = (meanPeriod * meanCounter + delta) / (meanCounter + 1);
                meanCounter++;
            }
        }

        previousTimestamp = event.getTimestamp();
    }

    public void cleanStaleData() {
        long unix = agent.unix();
        int t = date.size() - 1;
        while (t >= 0) {
            if (unix - date.get(t) >= interval) {
                date = date.subList(0, t + 1);
                date = date.subList(0, t + 1);
                break;
            }
            t--;
        }
    }
}
