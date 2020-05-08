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
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;

/**
 * @author Rustam Vishnyakov
 */
public class PragmaMarkListPopup {
    private final @NotNull
    JBList<MyFoldingDescriptorWrapper> myRegionsList;
    private final @NotNull
    JBPopup myPopup;
    private final @NotNull
    Editor myEditor;

    PragmaMarkListPopup(@NotNull Collection<PragmaMarkData> descriptors,
                        @NotNull final Editor editor,
                        @NotNull final Project project) {
        myEditor = editor;
        myRegionsList = new JBList<>();
        myRegionsList.setModel(new MyListModel(descriptors));
        myRegionsList.setSelectedIndex(0);

        final PopupChooserBuilder popupBuilder = JBPopupFactory.getInstance().createListPopupBuilder(myRegionsList);
        myPopup = popupBuilder
                .setTitle("Goto Pargma Marks")
                .setResizable(false)
                .setMovable(false)
                .setItemChoosenCallback(() -> {
                    PragmaMarkData pragmaMarkData = getNavigationElement();
                    if (pragmaMarkData != null) {
                        navigateTo(editor, pragmaMarkData);
                        IdeDocumentHistory.getInstance(project).includeCurrentCommandAsNavigation();
                    }
                }).createPopup();
    }

    void show() {
        myPopup.showInBestPositionFor(myEditor);
    }

    private static class MyListModel extends DefaultListModel<MyFoldingDescriptorWrapper> {
        private MyListModel(Collection<PragmaMarkData> descriptors) {
            for (PragmaMarkData pragmaMarkData : descriptors) {
                super.addElement(new MyFoldingDescriptorWrapper(pragmaMarkData));
            }
        }
    }

    private static class MyFoldingDescriptorWrapper {
        private final @NotNull
        PragmaMarkData pragmaMarkData;

        private MyFoldingDescriptorWrapper(@NotNull PragmaMarkData descriptor) {
            pragmaMarkData = descriptor;
        }

        @NotNull
        public PragmaMarkData getDescriptor() {
            return pragmaMarkData;
        }

        @Nullable
        @Override
        public String toString() {
            return " - " + pragmaMarkData.title;
        }
    }

    @Nullable
    public PragmaMarkData getNavigationElement() {
        Object selection = myRegionsList.getSelectedValue();
        if (selection != null) {
            return ((MyFoldingDescriptorWrapper) selection).getDescriptor();
        }
        return null;
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
