package com.mesmers.dimentools.model;

import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ElementFactory {

    private static final Pattern sValidityPattern = Pattern.compile("^([0-9]*\\.)?[0-9]*(px|dp|dip|sp)$");

    public static Element createElementFromValue(XmlTag tag, XmlAttribute attribute) {
        String attributeValue = attribute.getValue();
        if (attributeValue == null) {
            return null;
        }
        Matcher matcher = sValidityPattern.matcher(attributeValue);
        if (!matcher.find()) {
            return null;
        }
        if (attributeValue.endsWith("sp")) {
            return new SpElement(tag, attribute);
        } else {
            return new DpElement(tag, attribute);
        }
    }

}
