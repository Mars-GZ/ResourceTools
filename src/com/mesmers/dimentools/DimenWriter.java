package com.mesmers.dimentools;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.XmlElementFactory;
import com.intellij.psi.XmlRecursiveElementVisitor;
import com.intellij.psi.impl.source.codeStyle.CodeStyleManagerImpl;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.mesmers.dimentools.model.Element;
import com.mesmers.dimentools.model.Folder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DimenWriter {

    private ArrayList<Folder> mFolders;
    private ArrayList<Element> mElements;
    protected Project mProject;

    public void setParameters(Project project, ArrayList<Element> elements, ArrayList<Folder> folders) {
        this.mElements = elements;
        this.mFolders = folders;
        this.mProject = project;
    }

    private void generateDimenCode(Folder folder, XmlTag tag, Element element) {
        if (mElements.isEmpty()) {
            return;
        }
        List<XmlTag> tagList = new ArrayList<>();
        Map<String, XmlTag> tagMap = new HashMap<>();
        XmlTag[] tags = tag.findSubTags("dimen");
        for (XmlTag xmlTag : tags) {
            String name = getNameValue(xmlTag);
            if (name != null && name.startsWith("dimen_")) {
                tagList.add(xmlTag);
            }
        }
        for (XmlTag dimenTag : tags) {
            String value = getNameValue(dimenTag);
            if (value == null) {
                continue;
            }
            tagMap.put(value, dimenTag);
        }
        if (!element.used) {
            return;
        }
        boolean update = tags.length == 0 || !tagMap.containsKey(element.fieldName);
        if (update) {
            StringBuilder builder = new StringBuilder();
            builder.append("<dimen name=\"");
            builder.append(element.fieldName);
            builder.append("\">");
            builder.append(element.getValue(folder));
            builder.append("</dimen>");
            XmlTag subTag = XmlElementFactory.getInstance(mProject).createTagFromText(builder.toString());
            String currentValue = getNameValue(subTag);
            float cdv = getDimenTagValue(subTag);
            XmlTag afterTag = null;
            if (currentValue != null && currentValue.startsWith("dimen_")) {
                for (int i = 0; i < tagList.size(); i++) {
                    XmlTag xmlTag = tagList.get(i);
                    String xmlValue = getNameValue(xmlTag);
                    if (xmlValue == null || !xmlValue.startsWith("dimen_")) {
                        continue;
                    }
                    float odv = afterTag == null ? 0 : getDimenTagValue(afterTag);
                    float xdv = getDimenTagValue(xmlTag);
                    if (cdv > xdv && xdv > odv) {
                        afterTag = xmlTag;
                    }
                }
            }
            if (afterTag != null) {
                tag.addAfter(subTag, afterTag);
            } else {
                tag.addSubTag(subTag, false);
            }
            tagMap.put(currentValue, subTag);
            if (currentValue != null && currentValue.startsWith("dimen_")) {
                tagList.add(subTag);
            }
        }
        element.attribute.setValue(String.format("@dimen/%s", element.fieldName));
    }

    private String getNameValue(XmlTag tag) {
        XmlAttribute attribute = tag.getAttribute("name");
        if (attribute != null) {
            return attribute.getValue();
        }
        return null;
    }

    private float getDimenTagValue(XmlTag tag) {
        String text = tag.getValue().getText();
        float result = 0;
        try {
            result = Float.parseFloat(text.substring(0, text.length() - 2));
        } catch (NumberFormatException e) {
            if (text.endsWith("dip")) {
                try {
                    result = Float.parseFloat(text.substring(0, text.length() - 2));
                } catch (NumberFormatException e2) {
                    return result;
                }
            }
            return result;
        }
        return result;
    }

    public void execute() {
        WriteCommandAction.runWriteCommandAction(mProject, new Runnable() {
            @Override
            public void run() {
                DimenWriter.this.run();
            }
        });
    }

    protected void run() {
        for (Folder folder : mFolders) {
            if (!folder.used) {
                continue;
            }
            PsiFile file = folder.file;
            for (Element e : mElements) {
                file.accept(new XmlRecursiveElementVisitor() {
                    @Override
                    public void visitElement(PsiElement element) {
                        super.visitElement(element);
                        if (element instanceof XmlTag) {
                            XmlTag tag = (XmlTag) element;
                            if (tag.getName().equalsIgnoreCase("resources")) {
                                generateDimenCode(folder, tag, e);
                            }
                        }
                    }
                });
            }
            CodeStyleManagerImpl codeStyleManager = (CodeStyleManagerImpl) CodeStyleManagerImpl.getInstance(mProject);
            codeStyleManager.reformat(file);
        }
    }
}
