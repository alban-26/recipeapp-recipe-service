package com.myapp.recipe.adapter;

/**
 * Generic Converter interface that defines methods for converting between
 * a Domain Model, a Data Transfer Object (DTO), and a Persistence Entity.
 *
 * This interface is intended to be implemented by specific converters
 * for different domain models, DTOs, and entities.
 *
 * @param <DOMAIN> the type of the Domain Model
 * @param <DTO> the type of the Data Transfer Object (DTO)
 * @param <ENTITY> the type of the Persistence Entity
 */
public interface Converter<DOMAIN, DTO, ENTITY> {

    /**
     * Converts a Data Transfer Object (DTO) to the corresponding Domain Model.
     *
     * @param dto the Data Transfer Object that needs to be converted to a Domain Model.
     * @return the Domain Model that represents the business logic, constructed from the DTO.
     */
    DOMAIN dtoToDomain(DTO dto);

    /**
     * Converts a Persistence Entity to its corresponding Domain Model.
     *
     * @param entity the Persistence Entity that needs to be converted to a Domain Model.
     * @return the Domain Model that represents the business logic, constructed from the Persistence Entity.
     */
    DOMAIN entityToDomain(ENTITY entity);

    /**
     * Converts a Domain Model to its corresponding Data Transfer Object (DTO).
     *
     * @param domain the Domain Model that needs to be converted to a Data Transfer Object.
     * @return the Data Transfer Object that can be used for data exchange, constructed from the Domain Model.
     */
    DTO domainToDto(DOMAIN domain);

    /**
     * Converts a Domain Model to its corresponding Persistence Entity.
     *
     * @param domain the Domain Model that needs to be converted to a Persistence Entity.
     * @return the Persistence Entity that represents the database structure, constructed from the Domain Model.
     */
    ENTITY domainToEntity(DOMAIN domain);


    default long createId(Long id) {
        if (id == null) {
            return 0L;
        }
        return id;
    }

}
