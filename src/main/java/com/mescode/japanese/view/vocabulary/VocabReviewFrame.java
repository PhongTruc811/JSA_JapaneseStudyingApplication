package com.mescode.japanese.view.vocabulary;

import com.mescode.japanese.app.context.AppContext;
import com.mescode.japanese.app.navigation.AppNavigator;
import com.mescode.japanese.app.navigation.AppRoute;

import javax.swing.*;
import java.awt.Dimension;
import java.util.function.Consumer;

public final class VocabReviewFrame extends JFrame {
    private final AppContext context;
    private final Consumer<Boolean> themeListener;

    public VocabReviewFrame(AppNavigator navigator) {
        super("Ôn tập từ vựng theo chapter");
        context = navigator.getAppContext();
        VocabReviewPanel panel = new VocabReviewPanel(context.getVocabs(), context.isDarkMode(),
                () -> navigator.navigateTo(AppRoute.Vocab));
        setContentPane(panel);
        themeListener = dark -> SwingUtilities.invokeLater(() -> panel.applyTheme(dark));
        context.addThemeListener(themeListener);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1080, 820);
        setMinimumSize(new Dimension(800, 720));
        setLocationRelativeTo(null);
    }

    @Override public void dispose() {
        context.removeThemeListener(themeListener);
        super.dispose();
    }
}
