package com.mescode.japanese.view.chatbot;

import com.mescode.japanese.app.context.AppContext;
import com.mescode.japanese.model.Kanji;
import com.mescode.japanese.repo.KanjiRepository;
import com.mescode.japanese.service.chatbot.MizukiChatService;
import com.mescode.japanese.service.music.MusicPlayer;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Owns the single application-wide tutor session and moves its floating view
 * between module frames as navigation replaces one JFrame with another.
 */
public final class MizukiChatAssistant implements AutoCloseable {
    private static final int EDGE_MARGIN = 24;
    private static final int DRAG_EDGE_MARGIN = 8;

    private final AppContext appContext;
    private final FloatingChatWidget widget;
    private final Consumer<Boolean> themeListener;
    private final ComponentAdapter resizeListener;

    private JLayeredPane hostLayer;
    private double launcherAnchorX = 1d;
    private double launcherAnchorY = 1d;
    private boolean customLauncherPosition;
    private volatile boolean closed;

    public MizukiChatAssistant(AppContext appContext, MusicPlayer musicPlayer) {
        this.appContext = Objects.requireNonNull(appContext, "appContext");

        MizukiChatService tutorService = new MizukiChatService(
                appContext.getHiraganaList(),
                appContext.getKatakanaList(),
                appContext::getVocabs,
                loadKanjis(),
                appContext::getLearnedVocabCount,
                appContext::getFavoriteVocabCount
        );
        this.widget = new FloatingChatWidget(
                appContext::isDarkMode,
                tutorService,
                musicPlayer,
                this::positionWidget,
                this::moveWidget,
                this::resizeWidget
        );
        this.resizeListener = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                positionWidget();
            }
        };
        this.themeListener = darkMode -> SwingUtilities.invokeLater(() -> {
            if (!closed) {
                widget.updateTheme();
            }
        });
        appContext.addThemeListener(themeListener);
    }

    public void attachTo(JFrame frame) {
        if (frame == null || closed) {
            return;
        }
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> attachTo(frame));
            return;
        }

        JLayeredPane nextHost = frame.getLayeredPane();
        if (hostLayer == nextHost) {
            positionWidget();
            return;
        }

        boolean movingBetweenModules = hostLayer != null;
        detachFromHost();
        hostLayer = nextHost;
        hostLayer.addComponentListener(resizeListener);
        hostLayer.add(widget, JLayeredPane.PALETTE_LAYER);
        hostLayer.moveToFront(widget);
        if (movingBetweenModules) {
            widget.collapseImmediately();
        }
        widget.updateTheme();
        widget.setVisible(true);
        positionWidget();
        SwingUtilities.invokeLater(this::positionWidget);
    }

    public void detach() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::detach);
            return;
        }
        widget.collapseImmediately();
        detachFromHost();
    }

    private void detachFromHost() {
        if (hostLayer != null) {
            hostLayer.removeComponentListener(resizeListener);
        }
        Container parent = widget.getParent();
        if (parent != null) {
            parent.remove(widget);
            parent.revalidate();
            parent.repaint();
        }
        hostLayer = null;
    }

    private void positionWidget() {
        if (hostLayer == null) {
            return;
        }

        int margin = customLauncherPosition ? DRAG_EDGE_MARGIN : EDGE_MARGIN;
        int availableWidth = Math.max(1, hostLayer.getWidth() - margin * 2);
        int availableHeight = Math.max(1, hostLayer.getHeight() - margin * 2);
        Dimension preferred = widget.getPreferredSize();
        int width = Math.min(preferred.width, availableWidth);
        int height = Math.min(preferred.height, availableHeight);
        int x;
        int y;
        if (customLauncherPosition) {
            int anchoredX = (int) Math.round(launcherAnchorX * hostLayer.getWidth() - width / 2d);
            int anchoredY = (int) Math.round(launcherAnchorY * hostLayer.getHeight() - height / 2d);
            x = clampCoordinate(anchoredX, hostLayer.getWidth(), width, margin);
            y = clampCoordinate(anchoredY, hostLayer.getHeight(), height, margin);
        } else {
            x = Math.max(0, hostLayer.getWidth() - width - EDGE_MARGIN);
            y = Math.max(0, hostLayer.getHeight() - height - EDGE_MARGIN);
        }

        widget.setBounds(x, y, width, height);
        widget.revalidate();
        widget.repaint();
    }

    private void moveWidget(Point requestedLocation) {
        if (requestedLocation == null || hostLayer == null || widget.isExpanded()) {
            return;
        }
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> moveWidget(requestedLocation));
            return;
        }

        Dimension preferred = widget.getPreferredSize();
        int availableWidth = Math.max(1, hostLayer.getWidth() - DRAG_EDGE_MARGIN * 2);
        int availableHeight = Math.max(1, hostLayer.getHeight() - DRAG_EDGE_MARGIN * 2);
        int width = Math.min(preferred.width, availableWidth);
        int height = Math.min(preferred.height, availableHeight);
        int x = clampCoordinate(
                requestedLocation.x, hostLayer.getWidth(), width, DRAG_EDGE_MARGIN
        );
        int y = clampCoordinate(
                requestedLocation.y, hostLayer.getHeight(), height, DRAG_EDGE_MARGIN
        );

        customLauncherPosition = true;
        launcherAnchorX = (x + width / 2d) / Math.max(1, hostLayer.getWidth());
        launcherAnchorY = (y + height / 2d) / Math.max(1, hostLayer.getHeight());
        widget.setBounds(x, y, width, height);
        hostLayer.moveToFront(widget);
        widget.revalidate();
        widget.repaint();
    }

    private void resizeWidget(Rectangle requestedBounds) {
        if (requestedBounds == null || hostLayer == null || !widget.isExpanded()) {
            return;
        }
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> resizeWidget(requestedBounds));
            return;
        }

        int availableWidth = Math.max(1, hostLayer.getWidth() - DRAG_EDGE_MARGIN * 2);
        int availableHeight = Math.max(1, hostLayer.getHeight() - DRAG_EDGE_MARGIN * 2);
        int width = Math.min(Math.max(1, requestedBounds.width), availableWidth);
        int height = Math.min(Math.max(1, requestedBounds.height), availableHeight);
        int x = clampCoordinate(
                requestedBounds.x, hostLayer.getWidth(), width, DRAG_EDGE_MARGIN
        );
        int y = clampCoordinate(
                requestedBounds.y, hostLayer.getHeight(), height, DRAG_EDGE_MARGIN
        );

        widget.setExpandedSize(new Dimension(width, height));
        widget.setBounds(x, y, width, height);
        hostLayer.moveToFront(widget);
        widget.revalidate();
        widget.repaint();
    }

    private int clampCoordinate(int requested, int hostExtent, int widgetExtent, int margin) {
        int minimum = hostExtent >= widgetExtent + margin * 2 ? margin : 0;
        int maximum = Math.max(minimum, hostExtent - widgetExtent - minimum);
        return Math.max(minimum, Math.min(requested, maximum));
    }

    private List<Kanji> loadKanjis() {
        try {
            return new KanjiRepository().getKanjis();
        } catch (RuntimeException exception) {
            return List.of();
        }
    }

    @Override
    public void close() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::close);
            return;
        }
        if (closed) {
            return;
        }
        closed = true;
        appContext.removeThemeListener(themeListener);
        detach();
        widget.close();
    }
}
