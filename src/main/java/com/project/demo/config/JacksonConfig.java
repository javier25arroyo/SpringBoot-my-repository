package com.project.demo.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Configuration
public class JacksonConfig {

    @Bean
    public Module simpleGrantedAuthorityModule() {
        SimpleModule module = new SimpleModule();
        module.setMixInAnnotation(SimpleGrantedAuthority.class, SimpleGrantedAuthorityMixin.class);
        return module;
    }

    abstract static class SimpleGrantedAuthorityMixin implements GrantedAuthority {
        @JsonCreator
        SimpleGrantedAuthorityMixin(@JsonProperty("authority") String role) {
        }

        @Override
        public abstract String getAuthority();
    }
}
