package com.mesmers.dimentools.form;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import com.mesmers.dimentools.common.Utils;
import com.mesmers.dimentools.iface.ICanelListener;
import com.mesmers.dimentools.iface.IConfirmListener;
import com.mesmers.dimentools.iface.ISyncListener;
import com.mesmers.dimentools.iface.OnCheckBoxStateChangeListener;
import com.mesmers.dimentools.model.Element;
import com.mesmers.dimentools.model.Folder;

public class EntryList extends JPanel implements ISyncListener {

    protected IConfirmListener mConfirmListener;
    protected ICanelListener mCancelListener;
    protected Project mProject;
    protected Editor mEditor;
    protected ArrayList<Element> mElements = new ArrayList<>();
    protected ArrayList<Folder> mFolders = new ArrayList<>();

    protected EntryHeader mEntryHeader;
    protected ArrayList<ElementEntry> mElementEntries = new ArrayList<>();
    protected ArrayList<FolderEntry> mFolderEntries = new ArrayList<>();
    protected JButton mConfirm;
    protected JButton mCancel;

    public EntryList(Project project, Editor editor, ArrayList<Element> elements, ArrayList<Folder> folders,
                     IConfirmListener confirmListener, ICanelListener cancelListener) {
        this.mConfirmListener = confirmListener;
        this.mCancelListener = cancelListener;
        this.mProject = project;
        this.mEditor = editor;
        if (elements != null && !elements.isEmpty()) {
            mElements.addAll(elements);
        }
        if (folders != null && !folders.isEmpty()) {
            mFolders.addAll(folders);
        }
        setPreferredSize(new Dimension(740, 360));
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        addContentView();
        addFolders();
        addButtons();
    }

    private void addContentView() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mEntryHeader = new EntryHeader();
        contentPanel.add(mEntryHeader);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        JPanel injectionPanel = new JPanel();
        injectionPanel.setLayout(new BoxLayout(injectionPanel, BoxLayout.PAGE_AXIS));
        injectionPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        boolean selectAllCheck = true;
        int index = 0;
        for (Element element : mElements) {
            ElementEntry elementEntry = new ElementEntry(element);
            elementEntry.setListener(singleCheckListener);
            if (index > 0) {
                injectionPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
            injectionPanel.add(elementEntry);
            index++;

            mElementEntries.add(elementEntry);
            selectAllCheck &= elementEntry.isChecked();
        }
        mEntryHeader.getAllCheck().setSelected(selectAllCheck);
        mEntryHeader.setCheckBoxStateChangeListener(allCheckListener);
        injectionPanel.add(Box.createVerticalGlue());
        injectionPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        JBScrollPane scrollPanel = new JBScrollPane(injectionPanel);
        contentPanel.add(scrollPanel);

        add(contentPanel, BorderLayout.CENTER);
    }

    private void addFolders() {
        JPanel folderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (Folder folder : mFolders) {
            FolderEntry entry = new FolderEntry(folder);
            entry.setSyncListener(this);
            folderPanel.add(entry);
            mFolderEntries.add(entry);
        }
        add(folderPanel, BorderLayout.PAGE_END);
    }

    private void addButtons() {
        mCancel = new JButton();
        mCancel.setAction(new CancelAction());
        mCancel.setPreferredSize(new Dimension(120, 26));
        mCancel.setText("Cancel");
        mCancel.setVisible(true);

        mConfirm = new JButton();
        mConfirm.setAction(new ConfirmAction());
        mConfirm.setPreferredSize(new Dimension(120, 26));
        mConfirm.setText("Confirm");
        mConfirm.setVisible(true);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(mCancel);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(mConfirm);

        add(buttonPanel, BorderLayout.PAGE_END);
    }

    public JButton getConfirmButton() {
        return mConfirm;
    }

    private OnCheckBoxStateChangeListener allCheckListener = new OnCheckBoxStateChangeListener() {
        @Override
        public void changeState(boolean checked) {
            for (final ElementEntry entry : mElementEntries) {
                entry.setListener(null);
                entry.getCheck().setSelected(checked);
                entry.setListener(singleCheckListener);
            }
        }
    };

    private OnCheckBoxStateChangeListener singleCheckListener = checked -> {
        boolean result = true;
        for (ElementEntry entry : mElementEntries) {
            result &= entry.getCheck().isSelected();
        }

        mEntryHeader.setCheckBoxStateChangeListener(null);
        mEntryHeader.getAllCheck().setSelected(result);
        mEntryHeader.setCheckBoxStateChangeListener(allCheckListener);
    };

    private boolean checkValidity() {
        for (Element element : mElements) {
            if (element.used && !element.checkValidity()) {
                return false;
            }
        }
        for (Folder folder : mFolders) {
            if (folder.used && !folder.checkValidity()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void sync() {
        for (Element element : mElements) {
            element.syncElement();
        }
        for (ElementEntry entry : mElementEntries) {
            entry.syncFiledName();
        }
    }

    protected class ConfirmAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            for (ElementEntry entry : mElementEntries) {
                entry.syncElement();
            }
            for (FolderEntry entry : mFolderEntries) {
                entry.syncFolder();
            }
            boolean valid = checkValidity();
            if (valid) {
                if (mConfirmListener != null) {
                    mConfirmListener.onConfirm(mProject, mEditor, mElements, mFolders);
                }
            } else {
                Utils.showErrorNotificationDialog(mProject, "parameters error");
            }
        }
    }

    protected class CancelAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (mCancelListener != null) {
                mCancelListener.onCancel();
            }
        }
    }
}
