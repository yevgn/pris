package ru.antonov.securitytest.auth.role;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Permission {
    ADMIN_POST("admin::post"),
    ADMIN_GET("admin::get"),
    ADMIN_PUT("admin::put"),
    ADMIN_DELETE("admin::delete"),
    MANAGER_POST("manager::post"),
    MANAGER_GET("manager::get"),
    MANAGER_PUT("manager::put"),
    MANAGER_DELETE("manager::delete"),
    SUPERUSER_ALL("superuser::all")
    ;

    private final String permission;
}
