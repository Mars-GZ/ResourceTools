package com.mesmers.dimentools.iface;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;

import java.util.ArrayList;

import com.mesmers.dimentools.model.Element;
import com.mesmers.dimentools.model.Folder;

public interface IConfirmListener {

    void onConfirm(Project project, Editor editor, ArrayList<Element> elements, ArrayList<Folder> folders);

}
