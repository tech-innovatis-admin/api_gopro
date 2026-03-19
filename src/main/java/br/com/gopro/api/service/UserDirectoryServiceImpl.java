package br.com.gopro.api.service;

import br.com.gopro.api.dtos.UserLookupResponseDTO;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.model.AppUser;
import br.com.gopro.api.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDirectoryServiceImpl implements UserDirectoryService {

    private static final int MAX_LOOKUP_IDS = 200;

    private final AppUserRepository appUserRepository;

    @Override
    public List<UserLookupResponseDTO> lookupByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        List<Long> sanitizedIds = ids.stream()
                .filter(Objects::nonNull)
                .filter(id -> id > 0)
                .distinct()
                .limit(MAX_LOOKUP_IDS + 1L)
                .toList();

        if (sanitizedIds.size() > MAX_LOOKUP_IDS) {
            throw new BusinessException("Quantidade maxima de IDs para lookup e 200");
        }

        Map<Long, AppUser> usersById = appUserRepository.findAllById(sanitizedIds).stream()
                .collect(Collectors.toMap(AppUser::getId, Function.identity()));

        return sanitizedIds.stream()
                .map(usersById::get)
                .filter(Objects::nonNull)
                .map(this::toDto)
                .toList();
    }

    private UserLookupResponseDTO toDto(AppUser user) {
        return new UserLookupResponseDTO(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getUsername()
        );
    }
}

