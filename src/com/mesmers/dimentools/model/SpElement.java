package com.mesmers.dimentools.model;

import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;

public class SpElement extends Element {

    public SpElement(XmlTag tag, XmlAttribute attribute) {
        super(tag, attribute);
    }

    @Override
    String getType() {
        return "sp";
    }

}
