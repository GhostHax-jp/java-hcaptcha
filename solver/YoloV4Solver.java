package dev.rabies.twinsight.hcaptcha.solver;

import dev.rabies.twinsight.hcaptcha.HCaptcha;
import dev.rabies.twinsight.hcaptcha.HCaptchaUtils;
import dev.rabies.twinsight.hcaptcha.challenge.Task;
import dev.rabies.twinsight.utils.OpenCVUtils;
import org.opencv.core.*;
import org.opencv.dnn.DetectionModel;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class YoloV4Solver implements Solver {

    @Override
    public String getName() {
        return "YoloV4";
    }

    @Override
    public List<Task> solve(String category, String object, List<Task> tasks) {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            HCaptcha.getLogger().debug("Object: " + object);

            List<Task> answers = new ArrayList<>();
            List<String> classes = Files.readAllLines(Paths.get("opencv/coco.txt"));
            Net net = Dnn.readNetFromDarknet("opencv/yolov4_new.cfg", "opencv/yolov4_new.weights");

            for (Task task : tasks) {
                try {
                    Mat src = Imgcodecs.imread(task.getImagePath(), Imgcodecs.IMREAD_COLOR);
                    if (src.empty()) continue;
                    Mat src_clean = OpenCVUtils.denoise1(src, 1);

                    DetectionModel model = new DetectionModel(net);
                    model.setInputParams(1 / 255.0, new Size(416, 416), new Scalar(0), true, false);

                    MatOfInt classIds = new MatOfInt();
                    MatOfFloat scores = new MatOfFloat();
                    MatOfRect boxes = new MatOfRect();
                    HCaptcha.getLogger().debug("Detecting " + task.getKey());
                    long startTime = System.currentTimeMillis();
                    model.detect(src_clean, classIds, scores, boxes, 0.25F, 0.32F);

                    for (int i = 0; i < classIds.rows(); i++) {
                        String classId = classes.get((int) classIds.get(i, 0)[0]);
                        double score = scores.get(i, 0)[0];
                        if (isAnswer(object, classId)) {
                            HCaptcha.getLogger().debug(String.format(
                                    "Class: %s, Score: %.2f, Time: %sms",
                                    classId,
                                    score,
                                    (System.currentTimeMillis() - startTime)));
                            answers.add(task);
                        }
                    }
                } finally {
                    task.getImage().delete();
                }
            }

            return answers;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private boolean isAnswer(String object, String classId) {
        if (object.equals("seaplane")) {
            return classId.contains("aeroplane") && HCaptchaUtils.chance(0.57F);
        } else {
            return classId.contains(object);
        }
    }
}
