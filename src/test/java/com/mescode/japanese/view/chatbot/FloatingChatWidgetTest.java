package com.mescode.japanese.view.chatbot;

import com.mescode.japanese.service.chatbot.MizukiReply;
import com.mescode.japanese.service.chatbot.ChatService;
import org.junit.jupiter.api.Test;

import javax.swing.Action;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class FloatingChatWidgetTest {

    @Test
    void immediateCollapseCancelsAnInFlightOpeningTransition() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            FloatingChatWidget widget = createWidget();
            try {
                widget.setExpanded(true);
                assertTrue(widget.isTransitioning());

                widget.collapseImmediately();

                assertFalse(widget.isExpanded());
                assertFalse(widget.isTransitioning());
                assertEquals(new Dimension(310, 96), widget.getPreferredSize());
            } finally {
                widget.close();
            }
        });
    }

    @Test
    void launcherGreetingExpandedSizeComposerScrollbarsAndPopInAreConfigured() throws Exception {
        AtomicReference<FloatingChatWidget> widgetReference = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            FloatingChatWidget widget = createWidget();
            widgetReference.set(widget);
            try {
                Dimension launcherSize = widget.getPreferredSize();
                assertTrue(launcherSize.width > 200, "Launcher needs room for its greeting bubble");

                widget.setExpanded(true);
                assertTrue(widget.isExpanded());
                assertTrue(widget.isTransitioning());
                widget.setExpanded(true);
                assertTrue(widget.isTransitioning(), "A second click must not restart the opening animation");
            } catch (RuntimeException exception) {
                widget.close();
                throw exception;
            }
        });

        FloatingChatWidget widget = widgetReference.get();
        try {
            waitForTransition(widget);
            SwingUtilities.invokeAndWait(() -> {
                assertTrue(widget.isExpanded());
                assertFalse(widget.isTransitioning());
                widget.setExpandedSize(new Dimension(520, 680));
                assertEquals(new Dimension(520, 680), widget.getPreferredSize());

                JTextArea input = descendants(widget, JTextArea.class).stream()
                        .filter(JTextArea::isEditable)
                        .findFirst()
                        .orElseThrow();
                KeyStroke shiftEnter = KeyStroke.getKeyStroke(
                        KeyEvent.VK_ENTER,
                        KeyEvent.SHIFT_DOWN_MASK
                );
                Object actionKey = input.getInputMap().get(shiftEnter);
                assertEquals("insert-line-break", actionKey);
                Action action = input.getActionMap().get(actionKey);
                assertNotNull(action);

                input.setText("Dòng đầu");
                input.setCaretPosition(input.getDocument().getLength());
                action.actionPerformed(new ActionEvent(input, ActionEvent.ACTION_PERFORMED, ""));
                assertEquals("Dòng đầu\n", input.getText());

                List<JScrollPane> scrollPanes = descendants(widget, JScrollPane.class);
                assertTrue(scrollPanes.size() >= 2);
                assertTrue(scrollPanes.stream().anyMatch(scrollPane ->
                        scrollPane.getVerticalScrollBar().getUI()
                                .getClass().getSimpleName().equals("ModernScrollBarUI")
                ));

                widget.setExpanded(false);
                assertTrue(widget.isTransitioning());
                widget.setExpanded(false);
                assertTrue(widget.isTransitioning(), "A second close request must be ignored");
            });
            waitForTransition(widget);
            SwingUtilities.invokeAndWait(() -> {
                assertFalse(widget.isExpanded());
                assertFalse(widget.isTransitioning());
            });
        } finally {
            SwingUtilities.invokeAndWait(widget::close);
        }
    }

    private FloatingChatWidget createWidget() {
        ChatService chatService = message -> CompletableFuture.completedFuture(
                new MizukiReply("Phản hồi thử", "MIZUKI", List.of())
        );
        return new FloatingChatWidget(
                () -> false,
                chatService,
                () -> { },
                point -> { },
                bounds -> { }
        );
    }

    private <T extends Component> List<T> descendants(Container root, Class<T> type) {
        List<T> matches = new ArrayList<>();
        for (Component component : root.getComponents()) {
            if (type.isInstance(component)) {
                matches.add(type.cast(component));
            }
            if (component instanceof Container child) {
                matches.addAll(descendants(child, type));
            }
        }
        return matches;
    }

    private void waitForTransition(FloatingChatWidget widget) throws Exception {
        for (int attempt = 0; attempt < 20; attempt++) {
            Thread.sleep(35);
            AtomicReference<Boolean> transitioning = new AtomicReference<>();
            SwingUtilities.invokeAndWait(() -> transitioning.set(widget.isTransitioning()));
            if (!transitioning.get()) {
                return;
            }
        }
        fail("Mizuki pop-in transition did not complete in time");
    }
}
