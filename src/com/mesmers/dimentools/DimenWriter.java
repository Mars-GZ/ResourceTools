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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DimenWriter extends WriteCommandAction.Simple {

    private ArrayList<Folder> mFolders;
    private ArrayList<Element> mElements;
    protected Project mProject;

    protected DimenWriter(Project project, String commandName, PsiFile file,
                          ArrayList<Element> elements, ArrayList<Folder> folders) {
        super(project, commandName);
        this.mElements = elements;
        this.mFolders = folders;
        this.mProject = project;
    }

    @Override
    protected void run() throws Throwable {
        for (Folder folder : mFolders) {
            if (!folder.used) {
                continue;
            }
            PsiFile file = folder.file;
            file.accept(new XmlRecursiveElementVisitor() {
                @Override
                public void visitElement(PsiElement element) {
                    super.visitElement(element);
                    if (element instanceof XmlTag) {
                        XmlTag tag = (XmlTag) element;
                        if (tag.getName().equalsIgnoreCase("resources")) {
                            generateDimenCode(folder, tag);
                        }
                    }
                }
            });
            CodeStyleManagerImpl codeStyleManager = (CodeStyleManagerImpl) CodeStyleManagerImpl.getInstance(mProject);
            codeStyleManager.reformat(file);
        }
    }

    private void generateDimenCode(Folder folder, XmlTag tag) {
        if (mElements.isEmpty()) {
            return;
        }
        List<XmlTag> tagList = new ArrayList<>();
        Map<String, XmlTag> tagMap = new HashMap<>();
        XmlTag[] tags = tag.findSubTags("dimen");
        Collections.addAll(tagList, tags);
        for (XmlTag dimenTag : tags) {
            String value = getNameValue(dimenTag);
            if (value == null) {
                continue;
            }
            tagMap.put(value, dimenTag);
        }
        tagList.sort((t1, t2) -> {
            String v1 = getNameValue(t1);
            String v2 = getNameValue(t2);
            if (v1 == null && v2 == null) {
                return 0;
            }
            if (v1 == null) {
                return 1;
            }
            if (v2 == null) {
                return -1;
            }
            return v1.compareTo(v2);
        });
        for (Element element : mElements) {
            if (!element.used) {
                continue;
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
                XmlTag afterTag = null;
                if (currentValue != null && currentValue.startsWith("dimen_")) {
                    for (int i = 0; i < tagList.size(); i++) {
                        XmlTag xmlTag = tagList.get(i);
                        String xmlValue = getNameValue(xmlTag);
                        if (xmlValue == null || !currentValue.startsWith("dimen_")) {
                            continue;
                        }
                        float xdv = getDimenTagValue(xmlTag);
                        float cdv = getDimenTagValue(subTag);
                        if (cdv > xdv) {
                            afterTag = xmlTag;
                        }
                    }
                }
                if (afterTag != null) {
                    tag.addAfter(subTag, afterTag);
                } else {
                    tag.addSubTag(subTag, true);
                }
                tagMap.put(currentValue, subTag);
            }
            element.attribute.setValue(String.format("@dimen/%s", element.fieldName));
        }
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
        return Float.parseFloat(text.substring(0, text.length() - 2));
    }
}
