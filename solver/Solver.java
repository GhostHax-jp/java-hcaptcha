package dev.rabies.twinsight.hcaptcha.solver;

import dev.rabies.twinsight.hcaptcha.challenge.Task;

import java.util.List;

public interface Solver {

    String getName();

    List<Task> solve(String category, String object, List<Task> tasks);

}
