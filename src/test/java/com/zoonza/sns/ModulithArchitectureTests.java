package com.zoonza.sns;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

import static org.assertj.core.api.Assertions.assertThat;

class ModulithArchitectureTests {

    private final ApplicationModules modules = ApplicationModules.of(SnsApplication.class);

    @Test
    @DisplayName("애플리케이션 모듈 경계와 의존성 규칙을 준수한다")
    void verifiesModuleStructure() {
        modules.verify();
    }

    @Test
    @DisplayName("멤버 기능은 독립된 애플리케이션 모듈이다")
    void containsMemberModule() {
        assertThat(modules.getModuleByName("member")).isPresent();
    }
}
