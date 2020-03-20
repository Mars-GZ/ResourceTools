package com.mesmers.dimentools.common;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.XmlRecursiveElementVisitor;
import com.intellij.psi.search.EverythingGlobalScope;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.awt.RelativePoint;

import java.util.ArrayList;

import com.mesmers.dimentools.model.Element;
import com.mesmers.dimentools.model.ElementFactory;
import com.mesmers.dimentools.model.Folder;
import com.mesmers.dimentools.model.FolderFactory;

public class Utils {

    private static final Logger log = Logger.getInstance(Utils.class);

    public static void showErrorNotificationDialog(Project project, String text) {
        showNotificationDialog(project, MessageType.ERROR, text);
    }

    public static void showNotificationDialog(Project project, MessageType type, String text) {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(text, type, null)
                .setFadeoutTime(7500)
                .createBalloon()
                .show(RelativePoint.getCenterOf(statusBar.getComponent()), Balloon.Position.atRight);
    }

    public static ArrayList<Folder> getFoldersFromProject(Project project, PsiFile file) {
        ArrayList<Folder> folders = new ArrayList<>();
        return getFoldersFromModule(project, file, folders);
    }

    //Module module = ModuleUtil.findModuleForPsiElement(file);
    //ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
    //List<VirtualFile> sourceRoots = rootManager.getSourceRoots(JavaModuleSourceRootTypes.RESOURCES);
    public static ArrayList<Folder> getFoldersFromModule(Project project, PsiFile file, ArrayList<Folder> folders) {
        Module module = ModuleUtil.findModuleForPsiElement(file);
        if (module != null) {
            GlobalSearchScope moduleScope = module.getModuleContentScope();
            PsiFile[] dimenFiles = FilenameIndex.getFilesByName(project, "dimens.xml", moduleScope);
            if (dimenFiles.length <= 0) {
                return folders;
            }
            Folder defaultFolder = null;
            for (PsiFile dimenFile : dimenFiles) {
                PsiDirectory directory = dimenFile.getParent();
                if (directory == null) {
                    continue;
                }
                Folder folder = FolderFactory.createFolderFromDirectoryName(dimenFile);
                if (folder != null) {
                    if (folder.name.equalsIgnoreCase("values")) {
                        defaultFolder = folder;
                    } else if (defaultFolder == null && folder.ratio.equalsIgnoreCase("3")) {
                        defaultFolder = folder;
                    }
                    folders.add(folder);
                }
            }
        }
        return folders;
    }

    public static ArrayList<Element> getDimensFromLayout(PsiFile file) {
        ArrayList<Element> elements = new ArrayList<>();
        return getDimensFromLayout(file, elements);
    }

    public static ArrayList<Element> getDimensFromLayout(final PsiFile file, final ArrayList<Element> elements) {
        file.accept(new XmlRecursiveElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                super.visitElement(element);
                if (element instanceof XmlTag) {
                    XmlTag tag = (XmlTag) element;
                    if (tag.getName().equalsIgnoreCase("include")) {
                        XmlAttribute layout = tag.getAttribute("layout");
                        if (layout != null) {
                            Project project = file.getProject();
                            PsiFile include = findLayoutResource(file, project, getLayoutName(layout.getValue()));
                            if (include != null) {
                                getDimensFromLayout(include, elements);
                                return;
                            }
                        }
                    }
                    XmlAttribute[] attributes = tag.getAttributes();
                    if (attributes.length <= 0) {
                        return;
                    }
                    for (XmlAttribute attribute : attributes) {
                        Element dimenElement = ElementFactory.createElementFromValue(tag, attribute);
                        if (dimenElement != null) {
                            elements.add(dimenElement);
                        }
                    }
                }
            }
        });
        return elements;
    }

    public static String getLayoutName(String layout) {
        if (layout == null || !layout.startsWith("@") || !layout.contains("/")) {
            return null;
        }
        String[] parts = layout.split("/");
        if (parts.length != 2) {
            return null;
        }
        return parts[1];
    }

    public static PsiFile findLayoutResource(PsiFile file, Project project, String fileName) {
        String name = String.format("%s.xml", fileName);
        return resolveLayoutResourceFile(file, project, name);
    }

    public static PsiFile resolveLayoutResourceFile(PsiElement element, Project project, String name) {
        Module module = ModuleUtil.findModuleForPsiElement(element);
        PsiFile[] files = null;
        if (module != null) {
            GlobalSearchScope moduleScope = module.getModuleWithDependenciesScope();
            files = FilenameIndex.getFilesByName(project, name, moduleScope);
            if (files.length <= 0) {
                moduleScope = module.getModuleWithDependenciesAndLibrariesScope(false);
                files = FilenameIndex.getFilesByName(project, name, moduleScope);
            }
        }
        if (files == null || files.length <= 0) {
            files = FilenameIndex.getFilesByName(project, name, new EverythingGlobalScope());
            if (files.length <= 0) {
                return null;
            }
        }
        for (PsiFile file : files) {
            log.info("Resolved layout resource file for name [" + name + "]: " + file.getVirtualFile());
        }
        return files[0];
    }

    public static boolean isDigitsOnly(CharSequence str) {
        final int len = str.length();
        for (int cp, i = 0; i < len; i += Character.charCount(cp)) {
            cp = Character.codePointAt(str, i);
            if (!Character.isDigit(cp)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isCheckedElement(ArrayList<Element> elements) {
        if (elements.isEmpty()) {
            return false;
        }
        for (Element element : elements) {
            if (element.used) {
                return true;
            }
        }
        return false;
    }

    public static boolean isCheckedFolder(ArrayList<Folder> folders) {
        if (folders.isEmpty()) {
            return false;
        }
        for (Folder folder : folders) {
            if (folder.used) {
                return true;
            }
        }
        return false;
    }
}
