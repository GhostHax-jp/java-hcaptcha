package dev.rabies.twinsight.hcaptcha.challenge;

import lombok.Data;

import java.io.File;

@Data
public class Task {

    private final File image;
    private final String imagePath;
    private final String key;
    private final int index;

    private boolean answer;
    private File newPath;
    private String newName;

}
