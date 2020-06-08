package com.alvin.computeraccessoriesstore.EventBus;

public class SweetAlertDialogLogin {
    private boolean successLogin, successAutoLogin;

    public SweetAlertDialogLogin(boolean successLogin, boolean successAutoLogin) {
        this.successLogin = successLogin;
        this.successAutoLogin = successAutoLogin;
    }

    public boolean isSuccessLogin() {
        return successLogin;
    }

    public void setSuccessLogin(boolean successLogin) {
        this.successLogin = successLogin;
    }

    public boolean isSuccessAutoLogin() {
        return successAutoLogin;
    }

    public void setSuccessAutoLogin(boolean successAutoLogin) {
        this.successAutoLogin = successAutoLogin;
    }
}
