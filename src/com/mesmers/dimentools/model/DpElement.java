package com.mesmers.dimentools.model;

import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;

public class DpElement extends Element {

    public DpElement(XmlTag tag, XmlAttribute attribute) {
        super(tag, attribute);
    }

    @Override
    String getType() {
        return "dp";
    }
}
