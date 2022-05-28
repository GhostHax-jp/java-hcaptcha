package dev.rabies.twinsight.hcaptcha.solver;

import ai.djl.Device;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.YoloV5Translator;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.Pipeline;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import dev.rabies.twinsight.hcaptcha.HCaptcha;
import dev.rabies.twinsight.hcaptcha.HCaptchaUtils;
import dev.rabies.twinsight.hcaptcha.challenge.Task;
import dev.rabies.twinsight.utils.OpenCVUtils;
import org.apache.commons.lang3.RandomUtils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class YoloV5Solver implements Solver {

    @Override
    public String getName() {
        return "YoloV5";
    }

    @Override
    public List<Task> solve(String category, String object, List<Task> tasks) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        HCaptcha.getLogger().debug("Object: " + object);

        List<Task> answers = new ArrayList<>();
        Translator<Image, DetectedObjects> translator = YoloV5Translator.builder()
                .setPipeline(new Pipeline()
                        .add(new Resize(128, 128))
                        .add(new ToTensor()))
                .optSynsetArtifactName("coco.txt")
                .optThreshold(0.27F)
                .optNmsThreshold(0.32F)
                .build();
        Criteria<Image, DetectedObjects> criteria = Criteria.builder()
                .setTypes(Image.class, DetectedObjects.class)
                .optDevice(Device.cpu())
                .optModelUrls("opencv")
                .optModelName("yolov5n6_128.torchscript")
                .optTranslator(translator)
                .optEngine("PyTorch")
                .build();

        for (Task task : tasks) {
            try {
                if (!task.getImage().exists()) {
                    HCaptcha.getLogger().error("Image not found");
                    if (HCaptchaUtils.chance(RandomUtils.nextFloat(0.5F, 0.575F))) {
                        answers.add(task);
                    }
                    continue;
                }

                Mat src = Imgcodecs.imread(task.getImagePath(), Imgcodecs.IMREAD_COLOR);
                if (src.empty()) continue;
                Mat src_clean = OpenCVUtils.denoise1(src, 1);

                try (ZooModel<Image, DetectedObjects> model = ModelZoo.loadModel(criteria)) {
                    Image img = OpenCVUtils.mat2Image(src_clean);
                    try (Predictor<Image, DetectedObjects> predictor = model.newPredictor()) {
                        HCaptcha.getLogger().debug("Detecting " + task.getKey());
                        long startTime = System.currentTimeMillis();

                        DetectedObjects results = predictor.predict(img);
                        for (DetectedObjects.DetectedObject obj : results.<DetectedObjects.DetectedObject>items()) {
                            String classId = obj.getClassName();
                            double score = obj.getProbability();
                            HCaptcha.getLogger().debug(String.format(
                                    "Class: %s, Score: %.2f, Time: %sms",
                                    classId, score, System.currentTimeMillis() - startTime));
                            if (testAnswerData(object, classId, score)) {
                                HCaptcha.getLogger().debug(String.format("Class: %s, Score: %.2f", classId, score));
                                String newName = classId + "-" + task.getKey();
                                task.setNewPath(new File("temp/yolo/" + newName + ".jpg"));
                                task.setNewName(newName);
                            } else {
                                task.setNewName(null);
                            }

                            task.setAnswer(isAnswer(object, classId, score));
                            answers.add(task);
                        }
                    }
                } catch (RuntimeException | ModelException | TranslateException | IOException ex) {
                    throw new RuntimeException(ex);
                }
            } finally {
//                task.getImage().delete();
            }
        }

        return answers;
    }

    private boolean isAnswer(String object, String classId, double score) {
        if (object.equals("seaplane")) {
            return classId.contains("aeroplane") && HCaptchaUtils.chance(0.57F);
        } else {
            return classId.contains(object)/* && score >= 0.32*/;
        }
    }

    private boolean testAnswerData(String object, String classId, double score) {
//        if (object.equals("truck") && classId.equals("bus") && score < 0.7) return true;
        if (classId.equals("bus") && score > 0.4) return true;
        if (classId.equals("truck") && score > 0.4) return true;
        if (classId.equals("bicycle") && score > 0.4) return true;
        if (classId.equals("truck") && score > 0.35) return true;
        if (classId.equals("aeroplane") && score > 0.4) return true;
        return classId.contains(object) && score > 0.3;
    }
}
