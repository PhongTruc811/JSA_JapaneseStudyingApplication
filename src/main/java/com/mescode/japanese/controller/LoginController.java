package com.mescode.japanese.controller;

import com.mescode.japanese.service.UserService;
import com.mescode.japanese.view.login.LoginFrame_Interface;

public class LoginController {
    private UserService userService;
    private LoginFrame_Interface view;

    public LoginController(LoginFrame_Interface view, UserService userService) {
        this.view = view;
        this.userService = userService;
    }
}
