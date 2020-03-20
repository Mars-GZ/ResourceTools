package com.mesmers.dimentools.form;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.mesmers.dimentools.iface.ISyncListener;
import com.mesmers.dimentools.model.Element;
import com.mesmers.dimentools.model.Folder;

public class FolderEntry extends JPanel {

    protected Folder mFolder;
    protected JCheckBox mCheckBox;
    protected JLabel mName;
    protected JTextField mRatio;

    protected Color mNameDefaultColor;
    protected Color mNameErrorColor = new Color(0x880000);

    protected ISyncListener mSyncListener;
    private String lastRatio;

    public FolderEntry(Folder folder) {
        mFolder = folder;
        lastRatio = mFolder.ratio;
        mCheckBox = new JCheckBox();
        mCheckBox.setPreferredSize(new Dimension(32, 26));
        mCheckBox.setSelected(mFolder.used);
        mCheckBox.addChangeListener(new CheckListener());

        mName = new JLabel(mFolder.name);
        mName.setPreferredSize(new Dimension(100, 26));

        mRatio = new JTextField(String.valueOf(mFolder.ratio), 10);
        mNameDefaultColor = mRatio.getBackground();
        mRatio.setPreferredSize(new Dimension(30, 26));
        mRatio.setMaximumSize(new Dimension(40, 26));
        mRatio.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent focusEvent) {

            }

            @Override
            public void focusLost(FocusEvent focusEvent) {
                syncFolder();
            }
        });

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setMaximumSize(new Dimension(Short.MAX_VALUE, 54));
        add(mCheckBox);
        add(Box.createHorizontalStrut(1));
        add(mName);
        add(Box.createHorizontalStrut(1));
        add(mRatio);
        add(Box.createHorizontalGlue());

        changeState();
    }

    private void changeState() {
        boolean isSelected = mCheckBox.isSelected();
        mName.setEnabled(isSelected);
        mRatio.setEnabled(isSelected);
        syncFolder();
    }

    public void syncFolder() {
        mFolder.used = mCheckBox.isSelected();
        if (!mFolder.used) {
            return;
        }
        String ratio = mRatio.getText();
        boolean updated = !ratio.equalsIgnoreCase(lastRatio);
        lastRatio = mFolder.ratio = ratio;
        if (mFolder.checkValidity()) {
            mRatio.setBackground(mNameDefaultColor);
            if (mFolder.isDefault) {
                Element.sDefaultRatio = mFolder.getRatio();
            }
            if (updated && mSyncListener != null) {
                mSyncListener.sync();
            }
        } else {
            mRatio.setBackground(mNameErrorColor);
        }
    }

    public void setSyncListener(ISyncListener syncListener) {
        mSyncListener = syncListener;
    }

    private class CheckListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent changeEvent) {
            changeState();
        }
    }
}
