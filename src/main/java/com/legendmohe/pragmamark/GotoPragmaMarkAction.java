package com.legendmohe.pragmamark;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.PopupAction;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.UnfairTextRange;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiSubstitutor;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * asdasdasdasd
 */
public class GotoPragmaMarkAction extends AnAction implements DumbAware, PopupAction {

    public static final String PRAGMA_MARK_PREFIX = "////////";

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getProject();
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (Boolean.TRUE.equals(e.getData(PlatformDataKeys.IS_MODAL_CONTEXT))) {
            return;
        }
        if (project != null && editor != null) {
            if (DumbService.getInstance(project).isDumb()) {
                DumbService.getInstance(project).showDumbModeNotification("line desc navigation is not available until indices are built");
                return;
            }
            CommandProcessor processor = CommandProcessor.getInstance();
            processor.executeCommand(
                    project,
                    () -> {
                        process(e, project, editor);
                    },
                    "goto pragma marks",
                    null);
        }
    }

    private void process(AnActionEvent e, Project project, Editor editor) {
        PsiFile data = e.getData(LangDataKeys.PSI_FILE);
        @NotNull PsiElement[] children = data.getChildren();
        for (PsiElement child : children) {
            if (child instanceof PsiClass) {
                PsiClass psiClass = (PsiClass) child;
                printPsiClass(psiClass, editor);
            }
            System.out.println(child.toString());
        }
        Collection<PragmaMarkData> pragmaMarks = getCustomFoldingDescriptors(editor, project);
        if (pragmaMarks.size() > 0) {
            PragmaMarkListPopup regionsPopup = new PragmaMarkListPopup(pragmaMarks, editor, project);
            regionsPopup.show();
        } else {
            notifyCustomRegionsUnavailable(editor, project);
        }
    }

    private void printPsiClass(PsiClass psiClass, Editor editor) {
        @NotNull PsiMethod[] allMethods = psiClass.getMethods();
        @NotNull PsiField[] allFields = psiClass.getFields();
        @NotNull PsiClass[] allInnerClasses = psiClass.getInnerClasses();
        System.out.println("allMethods=" + Arrays.toString(allMethods));
        System.out.println("allFields=" + Arrays.toString(allFields));
        System.out.println("allInnerClasses=" + Arrays.toString(allInnerClasses));

        for (PsiMethod allMethod : allMethods) {
            TextRange textRange = allMethod.getTextRange();
            String lineContent = editor.getDocument().getText(textRange).trim();
            System.out.println(">>  " + lineContent);
        }
    }

    ///////////////////////////////////function///////////////////////////////////

    @NotNull
    private static Collection<PragmaMarkData> getCustomFoldingDescriptors(@NotNull Editor editor, @NotNull Project project) {
        List<PragmaMarkData> descDataList = new ArrayList<>();
        final Document document = editor.getDocument();
        for (int curLine = 0; curLine < document.getLineCount(); curLine++) {
            TextRange textRange = new UnfairTextRange(
                    document.getLineStartOffset(curLine),
                    document.getLineEndOffset(curLine)
            );
            String lineContent = document.getText(textRange).trim();
            if (lineContent.startsWith(PRAGMA_MARK_PREFIX)) {
                PragmaMarkData data = createPragmaMarkData(lineContent, curLine);
                if (data != null) {
                    descDataList.add(data);
                }
            }
        }
        return descDataList;
    }

    ///////////////////////////////////public///////////////////////////////////

    private static PragmaMarkData createPragmaMarkData(String lineContent, int curLine) {
        // 将/替换成空格再trim即可实现提取中间部分，但要注意中间部分里面的/也会被替换掉
        String title = lineContent.replace('/', ' ').trim();
        if (title.length() > 0) {
            PragmaMarkData newData = new PragmaMarkData();
            newData.title = title;
            newData.lineNum = curLine;
            return newData;
        }
        return null;
    }

    private static void notifyCustomRegionsUnavailable(@NotNull Editor editor, @NotNull Project project) {
        final JBPopupFactory popupFactory = JBPopupFactory.getInstance();
        Balloon balloon = popupFactory
                .createHtmlTextBalloonBuilder("There are no line desc in the current file.", MessageType.INFO, null)
                .setFadeoutTime(2000)
                .setHideOnClickOutside(true)
                .setHideOnKeyOutside(true)
                .createBalloon();
        Disposer.register(project, balloon);
        balloon.show(popupFactory.guessBestPopupLocation(editor), Balloon.Position.below);
    }
}
