package dev.rabies.twinsight.hcaptcha.challenge;

import com.jme3.math.Vector2f;
import dev.rabies.twinsight.hcaptcha.HCaptcha;
import dev.rabies.twinsight.hcaptcha.HCaptchaUtils;
import dev.rabies.twinsight.agents.Agent;
import dev.rabies.twinsight.hcaptcha.algorithm.AlgorithmHelper;
import dev.rabies.twinsight.hcaptcha.algorithm.Proof;
import dev.rabies.twinsight.hcaptcha.event.Event;
import dev.rabies.twinsight.hcaptcha.event.EventRecorder;
import dev.rabies.twinsight.hcaptcha.screen.Curve;
import dev.rabies.twinsight.hcaptcha.screen.CurveOptions;
import dev.rabies.twinsight.hcaptcha.solver.Solver;
import dev.rabies.twinsight.http.response.CustomHttpResponse;
import dev.rabies.twinsight.http.FormEntityBuilder;
import dev.rabies.twinsight.solver.HswMode;
import dev.rabies.twinsight.utils.HttpUtils;
import lombok.Data;
import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.apache.commons.lang3.RandomUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class Challenge {

    private String host;
    private String siteKey;
    private String url;
    private String widgetId;
    private String dr;
    private Agent agent;
    private AlgorithmHelper algorithmHelper;
    private boolean useAccessibility;
    private HswMode hswMode;

    private String id;
    private String token;
    private String category;
    private String question;
    private List<Task> tasks;
    private Proof proof;
    private JSONObject prevJson;

    private EventRecorder top;
    private EventRecorder frame;

    public void setupFrames() {
        prevJson = null;
        algorithmHelper = new AlgorithmHelper(this);

        top = new EventRecorder(agent);
        top.record(0);
        top.setData("dr", dr);
        top.setData("inv", false);
        top.setData("sc", agent.screenProperties());
        top.setData("nv", agent.navigatorProperties());
        top.setData("exec", false);
        agent.setUnixOffset(HCaptchaUtils.between(200, 400));
        frame = new EventRecorder(agent);
        frame.record(1);

        int rightBoundary = HCaptchaUtils.getFrameSize()[0];
        int upBoundary = HCaptchaUtils.getFrameSize()[1];
        CurveOptions curveOpts = new CurveOptions();
        curveOpts.setRightBoundary(rightBoundary);
        curveOpts.setUpBoundary(upBoundary);
        List<Movement> movements = getMouseMovements(
                new Vector2f(HCaptchaUtils.between(280, 300), HCaptchaUtils.between(20, 30)),
                new Vector2f(HCaptchaUtils.between(100, 150), HCaptchaUtils.between(30, 40)),
                curveOpts);
        Movement lastMovement = movements.get(movements.size() - 1);
        for (Movement movement : movements) {
            frame.recordEvent(new Event(movement.getPoint(), "mm", movement.getTimestamp()));
        }
        frame.recordEvent(new Event(lastMovement.getPoint(), "md", lastMovement.getTimestamp()));
        frame.recordEvent(new Event(lastMovement.getPoint(), "mu", lastMovement.getTimestamp()));
    }

    public void requestCaptcha() throws IOException {
        OrderedMap<String, Boolean> prev = new ListOrderedMap<>();
        prev.put("escaped", false);
        prev.put("passed", false);
        prev.put("expiredChallenge", false);
        prev.put("expiredResponse", false);

        OrderedMap<String, Object> motionData = new ListOrderedMap<>();
        motionData.put("v", 1);

        OrderedMap<String, Object> frameData = frame.getData();
        motionData.putAll(frameData);
        motionData.put("topLevel", top.getData());
        motionData.put("session", new JSONObject());
        motionData.put("widgetList", new String[]{widgetId});
        motionData.put("widgetId", widgetId);
        motionData.put("href", url);
        motionData.put("prev", prev);

        if (useAccessibility) {
//            HCaptcha.getHttp().getCookieStore().addCookie(new BasicClientCookie(
//                    "session",
//                    "49cdfb12-0eae-43cb-95cd-4f7fe7c77c23"
//            ));
            HCaptcha.getHttp().getCookieStore().addCookie(new BasicClientCookie(
                    "hc_accessibility",
                    "qnCgdKB+oH+PzCTwDB4abTGOKt6gmEROqEy6bKNMq4tXmSHs4lUTDNgzBMOWIL+9nGn9x+JSYxQkcmT3UplxHqcT7p70+jjzAMeRJyRg0P3W1ibRKt8lvxugVukxqZ4c6KhaQglznDE9K70CxmEyK1o+w02LX+3F25Z2PfNfK6cXPM5YYAikLvCVEtLrYj3JyJOrALy0FZOcdFsfjEqU82oY6qaoR9Wxn0iZ9KSzXoFVipw1O4dEs7fdq5WDGs4iabMUTtky4kDR350icA3e8lRNICR2shNp9uHnSuPNMSN77Kghgb5O3Kg2CLFMmE5vC8jiAxhuFpd1tkDqsq9xkfdILKY+rpbQZsAUHZa15mfRiTnfE8X7AEukOg6BDYniRxX6uTfbGAyhKjS512AvEh5uMcMONp1Dja+oXZdsVTKldmF71aH4AXjkiqf6p6OEcHD8zjn2DhEVWM5Ph1a3NXMAQ1HrJY3ZZVDGhUXdfQihn9HUa1v3SuU0srdOOSp5dPiN1/7zsmMpek9ciGOGykIKWnUdtqNOUVwndGvsaVLfQUtKZtSPGgnrpskdxBYYm49XAU/8FZZpKaj4azuI+BCHghdjrLsE0Rbh64YJpwvP9KcOWo0nM73n6AT9cdfikH/P9vQHotw6v1zl5eLrugYzT4ehrCfo+NMNITL7edNSKVLDFkBbGCMxRdFEO9p4AeiaEb8w07qgO1Dfdw1hO7dOXeuCF/oiZ+eGFGrLb+wheXmacN2D3Tuxh3y6mXVzppsoDGP7AOdDKxQpGT/vnyeuXUsD7iRThx/Upg4/W6gbh1Yjx0XklE5ERkZWUHZ7UP2ms7HT4pFXlu4nZR66QihGyOaWxf5khcPirAdGzbDhwGCgx0IcS6/dcCpssvbmf/FXoNdYoP7GNEK7uag3yplew15DPlS+zvw2OoIblpK3ybn0dhnQeweMNzwFP0JdjRJ+atqOmUiWK8oPdlw03ZWskurjelJdyHHcOkvlazQ=2gBIEHJmhfEZzrje"
            ));
        }

        JSONObject encodedMotionData = new JSONObject(motionData);
        CustomHttpResponse response = HCaptcha.getHttp().post("https://hcaptcha.com/getcaptcha", it -> {
            it.addParameter("s", siteKey);

            it.addHeader("Authority", "hcaptcha.com");
            it.addHeader("Accept", "application/json");
            it.addHeader("User-Agent", agent.userAgent());
            it.addHeader("Origin", "https://newassets.hcaptcha.com");
            it.addHeader("Sec-Fetch-Site", "same-site");
            it.addHeader("Sec-Fetch-Mode", "cors");
            it.addHeader("Sec-Fetch-Dest", "empty");
            it.addHeader("Accept-Language", "en-US,en;q=0.9");
            it.addHeader("Accept-Encoding", "gzip, deflate, br");

            FormEntityBuilder builder = new FormEntityBuilder()
                    .addItem("v", HCaptchaUtils.getVersion())
                    .addItem("sitekey", siteKey)
                    .addItem("host", host)
                    .addItem("hl", "en")
                    .addItem("motionData", encodedMotionData.toString())
                    .addItem("n", proof.getProof())
                    .addItem("c", proof.getRequest());
//            if (prevJson != null) {
//                builder.addItem("action", "challenge-refresh");
//                builder.addItem("extraData", prevJson.toString());
//                builder.addItem("old_ekey", id);
//            }
            it.setEntity(builder.build());
        });
        if (response.getResponseCode() != 200) return;
        JSONObject responseObject = new JSONObject(response.getResponseBody());
        if (!responseObject.isNull("pass")) {
            token = responseObject.getString("generated_pass_UUID");
            return;
        }

        if (!responseObject.isNull("success") && responseObject.getBoolean("success")) {
            HCaptcha.getLogger().error("challenge creation request was rejected");
            return;
        }

        JSONArray tasks = responseObject.getJSONArray("tasklist");
        HCaptcha.getLogger().info("Task size: " + tasks.length());
        if (tasks.length() < 2 || tasks.isEmpty()) { // ?
            HCaptcha.getLogger().error("no tasks in challenge, most likely ratelimited");
            return;
        }

        prevJson = responseObject;
        id = responseObject.getString("key");
        category = responseObject.getString("request_type");
        question = responseObject.getJSONObject("requester_question").getString("en");
        this.tasks.clear();
        HCaptcha.getLogger().info("Fetching Images...");

        for (int index = 0; index < tasks.length(); index++) {
            Object object = tasks.get(index);
            if (!(object instanceof JSONObject taskObject)) continue;
            String datapoint = taskObject.getString("datapoint_uri");
            String taskKey = taskObject.getString("task_key");

            String path = "temp/" + taskKey + ".jpg";
            File imageFile = new File(path);
            CustomHttpResponse imageResponse = HCaptcha.getHttp().download(datapoint, HttpGet.METHOD_NAME, it -> {
                it.addHeader("User-Agent", agent.userAgent());
                it.addHeader("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8");
                it.addHeader("Referer", "https://newassets.hcaptcha.com/");
            }, imageFile);
            if (imageResponse.getResponseCode() != 200) {
                imageFile.delete();
                continue;
            }

//            imageFile.deleteOnExit();
            this.tasks.add(new Task(imageFile, path, taskKey, index));
        }

        JSONObject configObject = responseObject.getJSONObject("c");
        proof = algorithmHelper.solve(agent, configObject.getString("type"), configObject.getString("req"));
    }

    public void refresh() throws IOException {
        siteConfig();
        requestCaptcha();
    }

    public boolean solve(Solver solver) throws IOException {
        HCaptcha.getLogger().debug("Solving challenge with %s...".formatted(solver.getName()));
        if (!HttpUtils.isBlank(token)) return true;
        if (HttpUtils.isBlank(question)) return false;
        String[] split = question.split(" ");
        String object = split[split.length - 1]
                .replace("motorbus", "bus")
                .replace("mοtorbus", "bus")
                .replace("airplane", "aeroplane")
                .replace("аirplane", "aeroplane")
                .replace("ѕeaplane", "seaplane")
                .replace("truсk", "truck")
                .replace("motorcycle", "motorbike")
                .replace("mοtorcycle", "motorbike")
                .replace("bіcycle", "bicycle")
                .replace("bοat", "boat")
                .replace("right", "aeroplane"); // cry about it!
        if (object.equals("seaplane")) {
            HCaptcha.getLogger().info("Skip seaplane");
            return false;
        }

        HCaptcha.getLogger().info("Question: " + question + ", Obj: " + object);
        List<Task> results = solver.solve(category, object, tasks);
        return checkAnswer(results);
    }

    public boolean checkAnswer(List<Task> results) throws IOException {
        List<Task> answers = results.stream().filter(Task::isAnswer).collect(Collectors.toList());
        HCaptcha.getLogger().info("Answer Result: " + answers.size() + "/" + tasks.size());
        HCaptcha.getLogger().debug("Simulating mouse movements on tiles.");
        simulateMouseMovements(answers);
        agent.resetUnix();
        prevJson = null;

        OrderedMap<String, String> answersMap = new ListOrderedMap<>();
        for (Task answer : tasks) {
            answersMap.put(answer.getKey(), String.valueOf(answered(answer, answers)));
        }

        OrderedMap<String, Object> motionData = new ListOrderedMap<>();
        OrderedMap<String, Object> frameData = frame.getData();
        motionData.putAll(frameData);
        motionData.put("topLevel", top.getData());
        motionData.put("v", 1);

        JSONObject encodedMotionData = new JSONObject(motionData);
        CustomHttpResponse response = HCaptcha.getHttp().post("https://hcaptcha.com/checkcaptcha/" + id, it -> {
            it.addParameter("s", siteKey);

            it.addHeader("Authority", "hcaptcha.com");
            it.addHeader("Accept", "*/*");
            it.addHeader("User-Agent", agent.userAgent());
            it.addHeader("Content-Type", "application/json");
            it.addHeader("Origin", "https://newassets.hcaptcha.com");
            it.addHeader("Sec-Fetch-Site", "same-site");
            it.addHeader("Sec-Fetch-Mode", "cors");
            it.addHeader("Sec-Fetch-Dest", "empty");
            it.addHeader("Accept-Language", "en-US,en;q=0.9");

            String payload = new JSONObject()
                    .put("v", HCaptchaUtils.getVersion())
                    .put("job_mode", category)
                    .put("answers", answersMap)
                    .put("serverdomain", host)
                    .put("sitekey", siteKey)
                    .put("motionData", encodedMotionData.toString())
                    .put("n", proof.getProof())
                    .put("c", proof.getRequest()).toString();
            it.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        });
        if (response.getResponseCode() != 200) {
            HCaptcha.getLogger().error("Failed captcha");
            return false;
        }

        saveYoloData(tasks);
        JSONObject resultObject = new JSONObject(response.getResponseBody());
        if (!resultObject.isNull("pass") && !resultObject.getBoolean("pass")) {
            HCaptcha.getLogger().error("incorrect answers");
            return false;
        }

        HCaptcha.getLogger().info("Successfully completed challenge!");
        token = resultObject.getString("generated_pass_UUID");
        return true;
    }

    private void simulateMouseMovements(List<Task> answers) {
        int totalPages = Math.max(1, tasks.size());
        Vector2f cursorPos = new Vector2f(RandomUtils.nextFloat(1, 5), RandomUtils.nextFloat(300, 350));

        int rightBoundary = HCaptchaUtils.getFrameSize()[0];
        int upBoundary = HCaptchaUtils.getFrameSize()[1];

        CurveOptions curveOpts = new CurveOptions();
        curveOpts.setRightBoundary(rightBoundary);
        curveOpts.setUpBoundary(upBoundary);

        for (int page = 0; page < totalPages; page++) {
            int fromIndex = page * tasks.size();
            int toIndex = (page + 1) * tasks.size() - 1;
//            toIndex = Math.min(tasks.size(), toIndex);
            List<Task> pageTiles = tasks.subList(fromIndex, toIndex);

            for (Task tile : pageTiles) {
                if (!answered(tile, answers)) continue;

                Vector2f tilePos = new Vector2f(
                        HCaptchaUtils.getTileImageSize()[0] * tile.getIndex() % HCaptchaUtils.getTilesPerRow() +
                                HCaptchaUtils.getTileImagePadding()[0] * tile.getIndex() % HCaptchaUtils.getTilesPerRow() +
                                HCaptchaUtils.between(10, HCaptchaUtils.getTileImageSize()[0]) +
                                HCaptchaUtils.getTileImageStartPos()[0],
                        HCaptchaUtils.getTileImageSize()[1] * tile.getIndex() % HCaptchaUtils.getTilesPerRow() +
                                HCaptchaUtils.getTileImagePadding()[1] * tile.getIndex() % HCaptchaUtils.getTilesPerRow() +
                                HCaptchaUtils.between(10, HCaptchaUtils.getTileImageSize()[1]) +
                                HCaptchaUtils.getTileImageStartPos()[1]
                );

                List<Movement> movements = getMouseMovements(cursorPos, tilePos, curveOpts);
                Movement lastMovement = movements.get(movements.size() - 1);
                for (Movement movement : movements) {
                    frame.recordEvent(new Event(movement.getPoint(), "mm", movement.getTimestamp()));
                }
                frame.recordEvent(new Event(lastMovement.getPoint(), "md", lastMovement.getTimestamp()));
                frame.recordEvent(new Event(lastMovement.getPoint(), "mu", lastMovement.getTimestamp()));
                cursorPos = tilePos;
            }

            Vector2f buttonPos = new Vector2f(
                    HCaptchaUtils.getVerifyButtonPos()[0] + HCaptchaUtils.between(5, 50),
                    HCaptchaUtils.getVerifyButtonPos()[1] + HCaptchaUtils.between(5, 15)
            );
            List<Movement> movements = getMouseMovements(cursorPos, buttonPos, curveOpts);
            Movement lastMovement = movements.get(movements.size() - 1);
            for (Movement movement : movements) {
                frame.recordEvent(new Event(movement.getPoint(), "mm", movement.getTimestamp()));
            }
            frame.recordEvent(new Event(lastMovement.getPoint(), "md", lastMovement.getTimestamp()));
            frame.recordEvent(new Event(lastMovement.getPoint(), "mu", lastMovement.getTimestamp()));

            for (Movement movement : movements) {
                top.recordEvent(new Event(movement.getPoint(), "mm", movement.getTimestamp()));
            }
            top.recordEvent(new Event(lastMovement.getPoint(), "md", lastMovement.getTimestamp()));
            top.recordEvent(new Event(lastMovement.getPoint(), "mu", lastMovement.getTimestamp()));
            cursorPos = buttonPos;
        }
    }

    public void siteConfig() throws IOException {
        CustomHttpResponse response = HCaptcha.getHttp().get("https://hcaptcha.com/checksiteconfig", it -> {
            it.addParameter("v", HCaptchaUtils.getVersion());
            it.addParameter("host", host);
            it.addParameter("sitekey", siteKey);
            it.addParameter("sc", "1");
            it.addParameter("swa", "1");

            it.addHeader("Accept", "*/*");
            it.addHeader("User-Agent", agent.userAgent());
            it.addHeader("Content-Type", "application/json");
            it.addHeader("Accept-Encoding", "gzip, deflate, br");
        });
        if (response.getResponseCode() != 200) return;
        JSONObject responseObject = new JSONObject(response.getResponseBody());
        if (!responseObject.getBoolean("pass")) return;
        JSONObject configObject = responseObject.getJSONObject("c");
        proof = algorithmHelper.solve(agent, configObject.getString("type"), configObject.getString("req"));
    }

    private List<Movement> getMouseMovements(Vector2f fromPoint, Vector2f toPoint, CurveOptions opts) {
        Curve curve = new Curve(fromPoint, toPoint, opts);
        List<Movement> resultMovements = new ArrayList<>(curve.getPoints().size());
        for (Vector2f move : curve.getPoints()) {
            agent.setUnixOffset(HCaptchaUtils.between(1, 7));
            resultMovements.add(new Movement(move, agent.unix()));
        }
        return resultMovements;
    }

    private List<Movement> getRandomMouseMovements() {
        List<Movement> resultMovements = new ArrayList<>();
        int points = HCaptchaUtils.between(1000, 10000);
        for (int point = 0; point < points; point++) {
            agent.setUnixOffset(HCaptchaUtils.between(2, 6));
            resultMovements.add(new Movement(new Vector2f(
                    HCaptchaUtils.between(5, 500),
                    HCaptchaUtils.between(5, 500)
            ), agent.unix()));
        }
        return resultMovements;
    }

    private void saveYoloData(List<Task> tasks) {
        File saveFolder = new File("temp/yolo");
        if (!saveFolder.exists()) saveFolder.mkdir();
        for (Task task : tasks) {
            if (HttpUtils.isBlank(task.getNewName())) {
                task.getImage().delete();
                continue;
            }

            if (task.getImage().renameTo(task.getNewPath())) {
                HCaptcha.getLogger().info("Moved " + task.getNewName());
            } else {
                HCaptcha.getLogger().error("Cannot move");
            }
        }
    }

    private boolean answered(Task task, List<Task> answers) {
        for (Task t : answers) {
            if (!t.getKey().equals(task.getKey())) continue;
            return true;
        }
        return false;
    }

    public AlgorithmHelper getAlgorithmHelper() {
        return algorithmHelper;
    }
}
