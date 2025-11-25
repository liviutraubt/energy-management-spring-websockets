package org.example.authenticationservice.security.annotations;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAuthority(T(org.example.authenticationservice.entity.Roles).ADMIN)")
@Target(ElementType.METHOD)
public @interface AllowAdmin {
}
