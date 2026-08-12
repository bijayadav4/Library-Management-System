package com.library;

import com.library.menu.MainMenu;

/**
 * Application entry point. Kept to a single line on purpose — all
 * real logic lives in the layered packages (model/repository/service/menu).
 */
public class Main {
    public static void main(String[] args) {
        new MainMenu().start();
    }
}
