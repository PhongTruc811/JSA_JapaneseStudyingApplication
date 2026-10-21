package com.mescode.japanese.view.vocabulary;

import com.mescode.japanese.model.vocab.Vocabulary;
import com.mescode.japanese.util.theme.UITheme;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VocabReviewPanelTest {
    @Test void completesRoundRetriesMissedAndResetsOnChapterChange() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            VocabReviewPanel panel = new VocabReviewPanel(List.of(
                    new Vocabulary("ねこ", "猫", "mèo", "neko", null, 1),
                    new Vocabulary("いぬ", "犬", "chó", "inu", null, 2)), true, () -> {});
            JButton card = (JButton) find(panel, "reviewCard");
            JButton known = (JButton) find(panel, "reviewKnown");
            JButton missed = (JButton) find(panel, "reviewMissed");
            JButton retry = (JButton) find(panel, "reviewRetry");
            assertTrue(card.getText().contains("猫"));
            assertFalse(known.isEnabled());
            assertFalse(known.isVisible());
            assertFalse(missed.isVisible());
            card.doClick(0);
            assertTrue(card.getText().contains("mèo"));
            assertTrue(known.isEnabled());
            assertTrue(known.isVisible());
            missed.doClick(0);
            assertTrue(card.getText().contains("Hoàn thành"));
            assertTrue(retry.isVisible());
            retry.doClick(0);
            assertTrue(card.isEnabled());
            assertFalse(known.isEnabled());
            card.doClick(0);
            known.doClick(0);
            assertFalse(retry.isVisible());
            ((JComboBox<?>) find(panel, "reviewChapter")).setSelectedItem(2);
            assertTrue(card.getText().contains("犬"));
            assertTrue(card.isEnabled());
            panel.applyTheme(false);
            assertEquals(UITheme.getCardBackground(false), card.getBackground());
            assertEquals(UITheme.getBackground(false), panel.getBackground());
            assertTrue(card.getText().contains("犬"));
        });
    }

    @Test void emptyContentDisablesReviewActions() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            VocabReviewPanel panel = new VocabReviewPanel(List.of(), false, () -> {});
            assertFalse(find(panel, "reviewCard").isEnabled());
            assertFalse(find(panel, "reviewKnown").isEnabled());
            assertFalse(find(panel, "reviewChapter").isEnabled());
        });
    }

    @Test void reverseDirectionAndBackActionWorkWithoutLosingTheChapter() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            boolean[] returned = {false};
            VocabReviewPanel panel = new VocabReviewPanel(List.of(
                    new Vocabulary("ねこ", "猫", "mèo", "neko", null, 1)), true, () -> returned[0] = true);
            JButton card = (JButton) find(panel, "reviewCard");
            JButton options = (JButton) find(panel, "reviewOptions");
            findByType(options.getComponentPopupMenu(), JCheckBoxMenuItem.class).doClick(0);
            assertTrue(card.getText().contains("mèo"));
            assertFalse(card.getText().contains("猫"));
            card.doClick(0);
            assertTrue(card.getText().contains("猫"));
            assertTrue(card.getText().contains("ねこ"));
            assertTrue(card.getText().contains("neko"));
            panel.applyTheme(false);
            assertTrue(card.getText().contains("猫"));
            findButton(panel, "←  Vocabulary").doClick(0);
            assertTrue(returned[0]);
        });
    }

    private JButton findButton(Container parent, String text) {
        for (Component child : parent.getComponents()) {
            if (child instanceof JButton button && text.equals(button.getText())) return button;
            if (child instanceof Container nested) {
                JButton found = findButton(nested, text);
                if (found != null) return found;
            }
        }
        return null;
    }

    private <T extends Component> T findByType(Container parent, Class<T> type) {
        for (Component child : parent.getComponents()) {
            if (type.isInstance(child)) return type.cast(child);
            if (child instanceof Container nested) {
                T found = findByType(nested, type);
                if (found != null) return found;
            }
        }
        return null;
    }

    private Component find(Container parent, String name) {
        for (Component child : parent.getComponents()) {
            if (name.equals(child.getName())) return child;
            if (child instanceof Container nested) {
                Component found = find(nested, name);
                if (found != null) return found;
            }
        }
        return null;
    }
}
