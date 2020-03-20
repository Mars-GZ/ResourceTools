package com.mesmers.dimentools.model;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;

public class FolderFactory {

    public static Folder createFolderFromDirectoryName(PsiFile file) {
        PsiDirectory directory = file.getParent();
        if (directory == null) {
            return null;
        }
        String name = directory.getName();
        if (!name.startsWith("values")) {
            return null;
        }
        return new Folder(file, name);
    }

}
