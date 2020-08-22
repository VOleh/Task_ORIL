package com.vanivskyi.test.dto.mapper;

public interface BaseMapper<Model, DTO> {
    DTO toDTO(Model model);

    Model toModel(DTO dto);
}