package com.mesmers.dimentools;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilBase;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import com.mesmers.dimentools.common.Utils;
import com.mesmers.dimentools.form.EntryList;
import com.mesmers.dimentools.iface.ICanelListener;
import com.mesmers.dimentools.iface.IConfirmListener;
import com.mesmers.dimentools.model.Element;
import com.mesmers.dimentools.model.Folder;

public class DimenAction extends BaseGenerateAction implements IConfirmListener, ICanelListener {

    Logger log = Logger.getInstance(DimenAction.class);

    protected JFrame mDialog;

    public DimenAction() {
        super(null);
    }

    public DimenAction(CodeInsightActionHandler handler) {
        super(handler);
    }

    @Override
    protected boolean isValidForFile(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        ArrayList<Element> elements = Utils.getDimensFromLayout(file);
        ArrayList<Folder> folders = Utils.getFoldersFromProject(project, file);
        return !elements.isEmpty() && !folders.isEmpty();
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        Editor editor = event.getData(PlatformDataKeys.EDITOR);
        if (project == null) {
            Utils.showErrorNotificationDialog(project, " No project found");
            return;
        }
        actionPerformedImpl(project, editor);
    }

    public void actionPerformedImpl(@NotNull Project project, Editor editor) {
        PsiFile file = PsiUtilBase.getPsiFileInEditor(editor, project);
        if (file == null) {
            Utils.showErrorNotificationDialog(project, "No layout found");
            return;
        }
        log.info("Layout file is " + file.getVirtualFile());

        ArrayList<Element> elements = Utils.getDimensFromLayout(file);
        ArrayList<Folder> folders = Utils.getFoldersFromProject(project, file);
        if (elements.isEmpty() || folders.isEmpty()) {
            Utils.showErrorNotificationDialog(project, "No dimen found in layout");
        } else {
            showDialog(project, editor, elements, folders);
        }
    }

    private void showDialog(Project project, Editor editor, ArrayList<Element> elements, ArrayList<Folder> folders) {
        EntryList container = new EntryList(project, editor, elements, folders, this, this);

        mDialog = new JFrame();
        mDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        mDialog.getRootPane().setDefaultButton(container.getConfirmButton());
        mDialog.getContentPane().add(container);
        mDialog.pack();
        mDialog.setLocationRelativeTo(null);
        mDialog.setVisible(true);
    }

    @Override
    public void onCancel() {
        closeDialog();
    }

    @Override
    public void onConfirm(Project project, Editor editor, ArrayList<Element> elements, ArrayList<Folder> folders) {
        PsiFile file = PsiUtilBase.getPsiFileInEditor(editor, project);
        if (file == null) {
            return;
        }
        closeDialog();
        if (Utils.isCheckedElement(elements) && Utils.isCheckedFolder(folders)) {
            new DimenWriter(project, "Generate Dimens", file, elements, folders).execute();
        } else {
            Utils.showErrorNotificationDialog(project, "No data selected");
        }
    }

    private void closeDialog() {
        if (mDialog == null) {
            return;
        }
        mDialog.setVisible(false);
        mDialog.dispose();
    }
}
