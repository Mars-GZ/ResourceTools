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

import com.mesmers.dimentools.iface.OnCheckBoxStateChangeListener;
import com.mesmers.dimentools.model.Element;

public class ElementEntry extends JPanel {

    protected Element mElement;
    protected OnCheckBoxStateChangeListener mListener;

    protected JCheckBox mCheck;
    protected JLabel mType;
    protected JLabel mID;
    protected JLabel mValue;
    protected JLabel mAttribute;
    protected JTextField mName;
    protected Color mNameDefaultColor;
    protected Color mNameErrorColor = new Color(0x880000);

    public void setListener(OnCheckBoxStateChangeListener stateChangeListener) {
        this.mListener = stateChangeListener;
    }

    public ElementEntry(Element element) {
        mElement = element;

        mCheck = new JCheckBox();
        mCheck.setPreferredSize(new Dimension(40, 26));
        mCheck.setSelected(false);
        mCheck.addChangeListener(new CheckListener());

        mType = new JLabel(element.name);
        mType.setPreferredSize(new Dimension(100, 26));

        mID = new JLabel(element.id);
        mID.setPreferredSize(new Dimension(100, 26));

        mAttribute = new JLabel(element.getAttribute());
        mAttribute.setPreferredSize(new Dimension(100, 26));

        mValue = new JLabel(element.originValue);
        mValue.setPreferredSize(new Dimension(100, 26));

        mName = new JTextField(element.fieldName, 10);
        mNameDefaultColor = mName.getBackground();
        mName.setPreferredSize(new Dimension(100, 26));
        mName.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent focusEvent) {

            }

            @Override
            public void focusLost(FocusEvent focusEvent) {
                syncElement();
            }
        });

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setMaximumSize(new Dimension(Short.MAX_VALUE, 54));
        add(mCheck);
        add(Box.createRigidArea(new Dimension(10, 0)));
        add(mType);
        add(Box.createRigidArea(new Dimension(10, 0)));
        add(mID);
        add(Box.createRigidArea(new Dimension(10, 0)));
        add(mAttribute);
        add(Box.createRigidArea(new Dimension(10, 0)));
        add(mValue);
        add(Box.createRigidArea(new Dimension(10, 0)));
        add(mName);
        add(Box.createHorizontalGlue());

        changeState();
    }

    public void syncFiledName() {
        mName.setText(mElement.fieldName);
        mName.setBackground(mNameDefaultColor);
    }

    public void syncElement() {
        mElement.used = mCheck.isSelected();
        mElement.fieldName = mName.getText();
        if (mElement.checkValidity()) {
            mName.setBackground(mNameDefaultColor);
        } else {
            mName.setBackground(mNameErrorColor);
        }
    }

    private void changeState() {
        boolean isSelected = mCheck.isSelected();
        mType.setEnabled(isSelected);
        mID.setEnabled(isSelected);
        mValue.setEnabled(isSelected);
        mName.setEnabled(isSelected);
        if (mListener != null) {
            mListener.changeState(isSelected);
        }
        syncElement();
    }

    public boolean isChecked() {
        return mCheck.isSelected();
    }

    public JCheckBox getCheck() {
        return mCheck;
    }

    public class CheckListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent changeEvent) {
            changeState();
        }
    }
}
