package com.mescode.japanese.app.launch;

import com.mescode.japanese.app.context.AppContext;
import com.mescode.japanese.app.navigation.AppNavigator;
import com.mescode.japanese.database.DatabaseInitializer;
import com.mescode.japanese.view.theme.AppIcon;

import javax.swing.*;

public class AppLauncher {
    public void start(){
        // Initialize the database before launching the UI
        DatabaseInitializer.initialize();

        SwingUtilities.invokeLater(() -> {
            // Khai báo AppIcon - nằm trong /view/theme
            AppIcon.install();
            AppContext context = new AppContext();
            AppNavigator nav = new AppNavigator(context);
            nav.firstStart(); // hiện ra menu frame
        });
    }
}

