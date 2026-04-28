package br.com.gopro.api.controller;

import br.com.gopro.api.model.Project;
import br.com.gopro.api.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @GetMapping("/teste")
    public ResponseEntity<EmailService.EmailDispatchResult> testarEmail() {
        Project project = new Project();
        project.setId(1L);
        project.setName("Projeto Teste");
        project.setCode("PRJ-001");
        project.setEndDate(LocalDate.now().plusDays(2));

        EmailService.EmailDispatchResult result = emailService.sendProjectDeadlineNotification(
                "samuel.araujo@innovatismc.com",
                "Samuel",
                project,
                2
        );

        return ResponseEntity.status(result.statusCode()).body(result);
    }
}
