package com.codigo.ms_security.aggregates.constants;

public class Constants {
    public static final Boolean STATUS_ACTIVE = true;
    public static final String CLAVE_AccountNonExpired ="isAccountNonExpired";
    public static final String CLAVE_AccountNonLocked ="isAccountNonLocked";
    public static final String CLAVE_CredentialsNonExpired = "isCredentialsNonExpired";
    public static final String CLAVE_Enabled = "isEnabled";
    public static final String USER_ADMIN = "LAVILA";
    public static final String ACCESS = "access";
    public static final String ENDPOINTS_PERMIT = "api/v1/authentication/**";
    public static final String[] ENDPOINTS_USER = {"api/v1/users","api/v1/users/**"};
    public static final String ENDPOINTS_PERMIT_ACTUATOR = "/actuator/**";
}
