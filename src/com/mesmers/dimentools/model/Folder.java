package com.mesmers.dimentools.model;

import com.intellij.psi.PsiFile;

import java.util.regex.Pattern;

public class Folder {

    private static final Pattern sValidityPattern = Pattern.compile("-?[0-9]+(\\.[0-9]+)?");

    public String name;
    public String ratio;
    public boolean used;
    public PsiFile file;
    public boolean isDefault;

    public Folder(PsiFile file, String directoryName) {
        this.file = file;
        this.name = directoryName;
        initData();
    }

    private void initData() {
        String ratio = "3";
        switch (name) {
            case "values-nxhdpi":
                ratio = "2.75";
                break;
        }
        this.ratio = ratio;
    }

    public float getRatio() {
        return Float.parseFloat(ratio);
    }

    public boolean checkValidity() {
        return sValidityPattern.matcher(ratio).find() && Float.parseFloat(ratio) != 0;
    }
}
