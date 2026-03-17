package br.com.gopro.api.service;

import br.com.gopro.api.config.AuthenticatedUserPrincipal;
import br.com.gopro.api.dtos.BudgetTransferRequestDTO;
import br.com.gopro.api.enums.BudgetTransferStatusEnum;
import br.com.gopro.api.enums.UserRoleEnum;
import br.com.gopro.api.mapper.BudgetTransferMapper;
import br.com.gopro.api.model.BudgetItem;
import br.com.gopro.api.model.BudgetTransfer;
import br.com.gopro.api.model.Project;
import br.com.gopro.api.repository.BudgetItemRepository;
import br.com.gopro.api.repository.BudgetTransferRepository;
import br.com.gopro.api.repository.DocumentRepository;
import br.com.gopro.api.repository.ProjectRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetTransferServiceImplTest {

    @Mock
    private BudgetTransferRepository budgetTransferRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private BudgetItemRepository budgetItemRepository;

    @Mock
    private DocumentRepository documentRepository;

    private final BudgetTransferMapper budgetTransferMapper = Mappers.getMapper(BudgetTransferMapper.class);

    private BudgetTransferServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new BudgetTransferServiceImpl(
                budgetTransferRepository,
                budgetTransferMapper,
                projectRepository,
                budgetItemRepository,
                documentRepository
        );
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createBudgetTransfer_shouldUseAuthenticatedUserAsCreator() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        new AuthenticatedUserPrincipal(77L, "admin@gopro.com", UserRoleEnum.ADMIN),
                        null,
                        List.of()
                )
        );

        BudgetTransferRequestDTO dto = new BudgetTransferRequestDTO(
                10L,
                11L,
                12L,
                new BigDecimal("150.00"),
                LocalDate.of(2026, 3, 17),
                BudgetTransferStatusEnum.APROVADO,
                "Comeback do remanejamento #44",
                null,
                15L
        );

        mockCreateDependencies(dto);

        ArgumentCaptor<BudgetTransfer> captor = ArgumentCaptor.forClass(BudgetTransfer.class);
        when(budgetTransferRepository.save(captor.capture())).thenAnswer(invocation -> {
            BudgetTransfer transfer = invocation.getArgument(0);
            transfer.setId(99L);
            return transfer;
        });

        var response = service.createBudgetTransfer(dto);

        assertThat(captor.getValue().getCreatedBy()).isEqualTo(77L);
        assertThat(response.createdBy()).isEqualTo(77L);
    }

    @Test
    void createBudgetTransfer_shouldFallbackToPayloadCreatorWithoutAuthenticatedUser() {
        BudgetTransferRequestDTO dto = new BudgetTransferRequestDTO(
                10L,
                11L,
                12L,
                new BigDecimal("150.00"),
                LocalDate.of(2026, 3, 17),
                BudgetTransferStatusEnum.APROVADO,
                "Comeback do remanejamento #44",
                null,
                15L
        );

        mockCreateDependencies(dto);

        ArgumentCaptor<BudgetTransfer> captor = ArgumentCaptor.forClass(BudgetTransfer.class);
        when(budgetTransferRepository.save(captor.capture())).thenAnswer(invocation -> {
            BudgetTransfer transfer = invocation.getArgument(0);
            transfer.setId(100L);
            return transfer;
        });

        var response = service.createBudgetTransfer(dto);

        assertThat(captor.getValue().getCreatedBy()).isEqualTo(15L);
        assertThat(response.createdBy()).isEqualTo(15L);
    }

    private void mockCreateDependencies(BudgetTransferRequestDTO dto) {
        Project project = new Project();
        project.setId(dto.projectId());

        BudgetItem fromItem = new BudgetItem();
        fromItem.setId(dto.fromItemId());

        BudgetItem toItem = new BudgetItem();
        toItem.setId(dto.toItemId());

        when(projectRepository.existsById(dto.projectId())).thenReturn(true);
        when(budgetItemRepository.existsById(dto.fromItemId())).thenReturn(true);
        when(budgetItemRepository.existsById(dto.toItemId())).thenReturn(true);
        when(projectRepository.getReferenceById(dto.projectId())).thenReturn(project);
        when(budgetItemRepository.getReferenceById(dto.fromItemId())).thenReturn(fromItem);
        when(budgetItemRepository.getReferenceById(dto.toItemId())).thenReturn(toItem);
    }
}
