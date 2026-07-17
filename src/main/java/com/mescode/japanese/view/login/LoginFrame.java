package com.mescode.japanese.view.login;

import com.mescode.japanese.controller.LoginController;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame implements LoginFrame_Interface{

    private final com.mescode.japanese.app.context.AppContext appContext;

    private JTabbedPane tabbedPane = new JTabbedPane();
    private LoginLandingPage landingPage;
    private Password_LoginPanel pwdPanel;
    private OTP_LoginPanel otpPanel;
    private GitHubLoginPanel githubPanel;
    private GoogleLoginPanel googlePanel;

    public LoginFrame(com.mescode.japanese.app.navigation.MenuNavigator navigator) {
        this.appContext = navigator.getAppContext();
        appContext.addThemeListener(isDark -> javax.swing.SwingUtilities.invokeLater(this::applyTheme));
        setupFrame();
        applyTheme();
        setupUI();
        this.setVisible(true);
    }

    private void applyTheme() {
        Color bg = appContext.isDarkMode() ? new Color(0x0F172A) : Color.WHITE;
        getContentPane().setBackground(bg);
    }

    private void setupFrame(){
        this.setSize(500, 500);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
    }

    private void setupUI(){
        landingPage = new LoginLandingPage();

        // Setup landing page buttons
        landingPage.getUsernameButton().addActionListener(e -> showUsernameLogin());
        landingPage.getGitHubButton().addActionListener(e -> showGitHubLogin());
        landingPage.getGoogleButton().addActionListener(e -> showGoogleLogin());

        // Setup tabs for later use
        pwdPanel = new Password_LoginPanel();
        otpPanel = new OTP_LoginPanel();
        githubPanel = new GitHubLoginPanel();
        googlePanel = new GoogleLoginPanel();

        // Show landing page initially
        this.add(landingPage, BorderLayout.CENTER);
    }

    private void showUsernameLogin() {
        getContentPane().removeAll();

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial Emoji", Font.PLAIN, 12));
        tabbedPane.addTab("Phone / OTP", otpPanel);
        tabbedPane.addTab("Username / Password", pwdPanel);

        this.add(tabbedPane, BorderLayout.CENTER);
        this.revalidate();
        this.repaint();
    }

    private void showGitHubLogin() {
        getContentPane().removeAll();
        this.add(githubPanel, BorderLayout.CENTER);
        this.revalidate();
        this.repaint();
    }

    private void showGoogleLogin() {
        getContentPane().removeAll();
        this.add(googlePanel, BorderLayout.CENTER);
        this.revalidate();
        this.repaint();
    }

    public void goBackToLanding() {
        getContentPane().removeAll();
        this.add(landingPage, BorderLayout.CENTER);
        this.revalidate();
        this.repaint();
    }

    // ===== expose cho Controller =====
    public Password_LoginPanel getPwdPanel() {
        return pwdPanel;
    }

    public OTP_LoginPanel getOtpPanel() {
        return otpPanel;
    }

    public GitHubLoginPanel getGitHubPanel() {
        return githubPanel;
    }

    @Override
    public String getUsername() {
        return "";
    }

    @Override
    public String getPassword() {
        return "";
    }
}
