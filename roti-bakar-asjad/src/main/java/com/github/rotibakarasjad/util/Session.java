package com.github.rotibakarasjad.util;

import com.github.rotibakarasjad.entity.User;
import com.github.rotibakarasjad.exception.BusinessException;

public class Session {

    private static User currentUser;

    public static void login(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /** Id user yang sedang login. Melempar BusinessException jika belum login. */
    public static int requireUserId() {
        if (currentUser == null) {
            throw new BusinessException("Anda belum login. Silakan login ulang.");
        }
        return currentUser.getId();
    }
}