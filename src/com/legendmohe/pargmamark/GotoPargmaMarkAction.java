package com.legendmohe.pargmamark;

import com.intellij.openapi.actionSystem.*;
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
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.UnfairTextRange;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class GotoPargmaMarkAction extends AnAction implements DumbAware, PopupAction {

    public static final String PARGMA_MARK_PREFIX = "////////";

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
                        Collection<PargmaMarkData> pargmaMarks = getCustomFoldingDescriptors(editor, project);
                        if (pargmaMarks.size() > 0) {
                            PargmaMarkListPopup regionsPopup = new PargmaMarkListPopup(pargmaMarks, editor, project);
                            regionsPopup.show();
                        } else {
                            notifyCustomRegionsUnavailable(editor, project);
                        }
                    },
                    "goto pargma marks",
                    null);
        }
    }

    @NotNull
    private static Collection<PargmaMarkData> getCustomFoldingDescriptors(@NotNull Editor editor, @NotNull Project project) {
        List<PargmaMarkData> descDataList = new ArrayList<>();
        final Document document = editor.getDocument();
        for (int curLine = 0; curLine < document.getLineCount(); curLine++) {
            TextRange textRange = new UnfairTextRange(
                    document.getLineStartOffset(curLine),
                    document.getLineEndOffset(curLine)
            );
            String lineContent = document.getText(textRange).trim();
            if (lineContent.startsWith(PARGMA_MARK_PREFIX)) {
                descDataList.add(createPargmaMarkData(lineContent, curLine));
            }
        }
        return descDataList;
    }

    private static PargmaMarkData createPargmaMarkData(String lineContent, int curLine) {
        // 将/替换成空格再trim即可实现提取中间部分，但要注意中间部分里面的/也会被替换掉
        String title = lineContent.replace('/', ' ').trim();
        PargmaMarkData newData = new PargmaMarkData();
        newData.title = title;
        newData.lineNum = curLine;
        return newData;
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
