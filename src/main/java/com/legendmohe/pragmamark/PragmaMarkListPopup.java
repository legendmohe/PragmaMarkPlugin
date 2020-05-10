package com.legendmohe.pragmamark;/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.ex.IdeDocumentHistory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.IPopupChooserBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.components.JBList;
import com.intellij.util.Function;
import com.intellij.util.Processor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.DefaultListModel;
import javax.swing.ListSelectionModel;

/**
 * @author Rustam Vishnyakov
 */
public class PragmaMarkListPopup {
    private final @NotNull
    JBPopup myPopup;
    private final @NotNull
    Editor myEditor;

    PragmaMarkListPopup(@NotNull Collection<PragmaMarkData> descriptors,
                        @NotNull final Editor editor,
                        @NotNull final Project project) {
        myEditor = editor;

        final IPopupChooserBuilder<PragmaMarkData> popupBuilder = JBPopupFactory.getInstance().createPopupChooserBuilder(new ArrayList<>(descriptors));
        myPopup = popupBuilder
                .setTitle("Goto Pargma Marks")
                .setResizable(false)
                .setMovable(false)
                .setNamerForFiltering(pragmaMarkData -> pragmaMarkData.title)
                .setCloseOnEnter(true)
                .setAutoSelectIfEmpty(false)
                .setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
                .setItemChosenCallback( pragmaMarkData -> {
                    if (pragmaMarkData != null) {
                        navigateTo(editor, pragmaMarkData);
                        IdeDocumentHistory.getInstance(project).includeCurrentCommandAsNavigation();
                    }
                })
                .createPopup();
    }

    void show() {
        myPopup.showInBestPositionFor(myEditor);
    }

    private static void navigateTo(@NotNull Editor editor, PragmaMarkData element) {
        int lineNum = element.lineNum;
        int offset = editor.getDocument().getLineStartOffset(lineNum);
        if (offset >= 0 && offset < editor.getDocument().getTextLength()) {
            editor.getCaretModel().removeSecondaryCarets();
            editor.getCaretModel().moveToOffset(offset);
            editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
            editor.getSelectionModel().removeSelection();
        }
    }
}
