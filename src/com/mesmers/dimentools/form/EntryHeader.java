package com.mesmers.dimentools.form;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mesmers.dimentools.iface.OnCheckBoxStateChangeListener;

public class EntryHeader extends JPanel {

    protected JCheckBox mAllCheck;
    protected JLabel mType;
    protected JLabel mID;
    protected JLabel mValue;
    protected JLabel mAttribute;
    protected JLabel mName;
    protected OnCheckBoxStateChangeListener mAllListener;

    public void setCheckBoxStateChangeListener(OnCheckBoxStateChangeListener onStateChangedListener) {
        this.mAllListener = onStateChangedListener;
    }

    public EntryHeader() {
        mAllCheck = new JCheckBox();
        mAllCheck.setPreferredSize(new Dimension(40, 26));
        mAllCheck.setSelected(false);
        mAllCheck.addItemListener(new AllCheckListener());

        mType = new JLabel("Element");
        mType.setPreferredSize(new Dimension(100, 26));
        mType.setFont(new Font(mType.getFont().getFontName(), Font.BOLD, mType.getFont().getSize()));

        mID = new JLabel("ID");
        mID.setPreferredSize(new Dimension(100, 26));
        mID.setFont(new Font(mType.getFont().getFontName(), Font.BOLD, mType.getFont().getSize()));

        mValue = new JLabel("Value");
        mValue.setPreferredSize(new Dimension(100, 26));
        mValue.setFont(new Font(mType.getFont().getFontName(), Font.BOLD, mType.getFont().getSize()));

        mAttribute = new JLabel("Attribute");
        mAttribute.setPreferredSize(new Dimension(100, 26));
        mAttribute.setFont(new Font(mType.getFont().getFontName(), Font.BOLD, mType.getFont().getSize()));

        mName = new JLabel("Variable Name");
        mName.setPreferredSize(new Dimension(100, 26));
        mName.setFont(new Font(mType.getFont().getFontName(), Font.BOLD, mType.getFont().getSize()));

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        add(Box.createRigidArea(new Dimension(1, 0)));
        add(mAllCheck);
        add(Box.createRigidArea(new Dimension(11, 0)));
        add(mType);
        add(Box.createRigidArea(new Dimension(12, 0)));
        add(mID);
        add(Box.createRigidArea(new Dimension(12, 0)));
        add(mValue);
        add(Box.createRigidArea(new Dimension(12, 0)));
        add(mAttribute);
        add(Box.createRigidArea(new Dimension(12, 0)));
        add(mName);
        add(Box.createRigidArea(new Dimension(22, 0)));
        add(Box.createHorizontalGlue());
    }

    public JCheckBox getAllCheck() {
        return mAllCheck;
    }

    private class AllCheckListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent itemEvent) {
            if (mAllListener != null) {
                mAllListener.changeState(itemEvent.getStateChange() == ItemEvent.SELECTED);
            }
        }
    }
}
