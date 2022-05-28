package dev.rabies.twinsight.hcaptcha.solver;

import dev.rabies.twinsight.hcaptcha.HCaptcha;
import dev.rabies.twinsight.hcaptcha.HCaptchaUtils;
import dev.rabies.twinsight.hcaptcha.challenge.Task;
import org.apache.commons.lang3.RandomUtils;

import java.util.ArrayList;
import java.util.List;

public class RandomSolver implements Solver {

    @Override
    public String getName() {
        return "Random";
    }

    @Override
    public List<Task> solve(String category, String object, List<Task> tasks) {
        List<Task> answers = new ArrayList<>();
        for (Task task : tasks) {
            if (HCaptchaUtils.chance(RandomUtils.nextFloat(0.5F, 0.575F))) {
                answers.add(task);
            }
        }

        HCaptcha.getLogger().info(
                "Answered: " + answers.size() + ", " +
                "TaskSize: " + tasks.size() + ", " +
                "Object: " + object);
        return answers;
    }
}
