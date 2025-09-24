package com.tpbs.assistanceservice.service;

import com.tpbs.assistanceservice.dto.AssistanceRequestDto;

import java.util.List;

public interface AssistanceRequestService {
    List<AssistanceRequestDto> getAll();
    AssistanceRequestDto getById(Long id);
    List<AssistanceRequestDto> getByUserId(Long userId);
    AssistanceRequestDto create(AssistanceRequestDto dto);
    AssistanceRequestDto update(Long id, AssistanceRequestDto dto);
    AssistanceRequestDto updateStatus(Long id, String status);
    AssistanceRequestDto resolve(Long id, String resolutionNote);
    void delete(Long id);
}
