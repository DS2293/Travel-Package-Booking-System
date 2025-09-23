package com.tpbs.packageservice.mapper;

import com.tpbs.packageservice.dto.TravelPackageDto;
import com.tpbs.packageservice.model.TravelPackage;
import org.springframework.stereotype.Component;

@Component
public class TravelPackageMapper {

    public TravelPackageDto toDto(TravelPackage entity) {
        if (entity == null) {
            return null;
        }
        return new TravelPackageDto(
                entity.getPackageId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getDuration(),
                entity.getPrice(),
                entity.getIncludedServices(),
                entity.getAgentId(),
                entity.getImage()
        );
    }

    public TravelPackage toEntity(TravelPackageDto dto) {
        if (dto == null) {
            return null;
        }
        return new TravelPackage(
                dto.getPackageId(),
                dto.getTitle(),
                dto.getDescription(),
                dto.getDuration(),
                dto.getPrice(),
                dto.getIncludedServices(),
                dto.getAgentId(),
                dto.getImage()
        );
    }

    public void updateEntityFromDto(TravelPackageDto dto, TravelPackage entity) {
        if (dto == null || entity == null) {
            return;
        }
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setDuration(dto.getDuration());
        entity.setPrice(dto.getPrice());
        entity.setIncludedServices(dto.getIncludedServices());
        entity.setAgentId(dto.getAgentId());
        entity.setImage(dto.getImage());
    }
}