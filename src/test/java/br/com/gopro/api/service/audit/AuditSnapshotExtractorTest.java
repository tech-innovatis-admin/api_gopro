package br.com.gopro.api.service.audit;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AuditSnapshotExtractorTest {

    private final AuditSnapshotExtractor extractor = new AuditSnapshotExtractor();

    @Test
    void extract_shouldIncludeInheritedBusinessFieldsAndIgnoreProxyInternals() {
        Map<String, Object> snapshot = extractor.extract(new EnhancedBudgetCategory());

        assertThat(snapshot)
                .containsEntry("id", 9L)
                .containsEntry("name", "AWS Console")
                .containsEntry("isActive", true)
                .containsEntry("project", 7L)
                .doesNotContainKeys("handler", "hibernateLazyInitializer", "$$_hibernate_interceptor");
    }

    private static class BaseBudgetCategory {
        private final Long id = 9L;
        private final String name = "AWS Console";
        private final Boolean isActive = true;
        private final RelatedProject project = new RelatedProject();
    }

    private static final class EnhancedBudgetCategory extends BaseBudgetCategory {
        private final Object handler = new Object();
        private final Object hibernateLazyInitializer = new Object();
        private final Object $$_hibernate_interceptor = new Object();
    }

    private static final class RelatedProject {
        private final Long id = 7L;
    }
}
