package com.mesmers.dimentools.model;

import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Element {

    public static float sDefaultRatio = 3;

    private static final Pattern sIdPattern = Pattern.compile("@\\+?(android:)?id/([^$]+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern sValidityPattern = Pattern.compile("^([a-zA-Z_\\$][\\w\\$]*)$");

    public boolean used;
    public String id;
    public String name;
    public String originValue;
    public String fieldName;
    public XmlAttribute attribute;
    public boolean isValid;

    public Element(XmlTag tag, XmlAttribute attribute) {
        String idValue = tag.getAttributeValue("android:id");
        if (idValue == null) {
            this.id = "N/A";
        } else {
            Matcher matcher = sIdPattern.matcher(idValue);
            if (matcher.find() && matcher.groupCount() > 0) {
                this.id = matcher.group(2);
            }
        }
        String name = tag.getName();
        String[] packages = name.split("\\.");
        if (packages.length > 1) {
            this.name = packages[packages.length - 1];
        } else {
            this.name = name;
        }
        this.originValue = attribute.getValue();
        this.attribute = attribute;
        syncElement();
    }

    public void syncElement() {
        String value = originValue;
        StringBuilder fieldBuilder = new StringBuilder();
        fieldBuilder.append("dimen_");
        float pxValue = Float.parseFloat(value.substring(0, value.length() - 2));
        if (value.endsWith("px")) {
            pxValue /= sDefaultRatio;
        }
        DecimalFormat df = new DecimalFormat("#.##");
        fieldBuilder.append(df.format(pxValue).replaceAll("\\.", "_"));
        fieldBuilder.append(getType());
        this.fieldName = fieldBuilder.toString();
    }

    abstract String getType();

    public String getAttribute() {
        return attribute.getName();
    }

    public String getValue(Folder folder) {
        String value = originValue;
        float ratio = folder.getRatio();
        float pxValue = Float.parseFloat(value.substring(0, value.length() - 2));
        if (value.endsWith("px")) {
            pxValue /= ratio;
        } else if (ratio != sDefaultRatio) {
            pxValue = pxValue * sDefaultRatio / ratio;
        }
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(pxValue) + getType();
    }

    public boolean checkValidity() {
        Matcher matcher = sValidityPattern.matcher(fieldName);
        return isValid = matcher.find();
    }
}
