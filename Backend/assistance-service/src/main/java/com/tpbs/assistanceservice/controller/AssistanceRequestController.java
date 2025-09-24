package com.tpbs.assistanceservice.controller;

import com.tpbs.assistanceservice.dto.AssistanceRequestDto;
import com.tpbs.assistanceservice.dto.UpdateStatusDto;
import com.tpbs.assistanceservice.dto.ResolveDto;
import com.tpbs.assistanceservice.service.AssistanceRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assistance")
@RequiredArgsConstructor
public class AssistanceRequestController {

    private final AssistanceRequestService service;

    @GetMapping
    public List<AssistanceRequestDto> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssistanceRequestDto> getById(@PathVariable("id") Long id) {
        AssistanceRequestDto dto = service.getById(id);
        return dto == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(dto);
    }

    @GetMapping("/user/{userId}")
    public List<AssistanceRequestDto> getByUser(@PathVariable("userId") Long userId) {
        return service.getByUserId(userId);
    }

    @PostMapping
    public AssistanceRequestDto create(@Valid @RequestBody AssistanceRequestDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssistanceRequestDto> update(@PathVariable("id") Long id, @RequestBody AssistanceRequestDto dto) {
        AssistanceRequestDto updated = service.update(id, dto);
        return updated == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<AssistanceRequestDto> updateStatus(@PathVariable("id") Long id, @Valid @RequestBody UpdateStatusDto statusDto) {
        AssistanceRequestDto updated = service.updateStatus(id, statusDto.getStatus());
        return updated == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<AssistanceRequestDto> resolve(@PathVariable("id") Long id, @RequestBody ResolveDto resolveDto) {
        AssistanceRequestDto updated = service.resolve(id, resolveDto.getResolutionNote());
        return updated == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        AssistanceRequestDto existing = service.getById(id);
        if (existing == null) return ResponseEntity.notFound().build();
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
