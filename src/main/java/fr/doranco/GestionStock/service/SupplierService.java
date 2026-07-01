package fr.doranco.GestionStock.service;

import fr.doranco.GestionStock.enums.UserRole;
import fr.doranco.GestionStock.model.dto.SupplierRequestDTO;
import fr.doranco.GestionStock.model.dto.SupplierResponseDTO;
import fr.doranco.GestionStock.model.entity.Supplier;
import fr.doranco.GestionStock.model.entity.User;
import fr.doranco.GestionStock.repository.ISupplierRepository;
import fr.doranco.GestionStock.repository.IUserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SupplierService implements ISupplierService {
    private final ISupplierRepository supplierRepository;
    private final IUserRepository userRepository;


    @Override
    public SupplierResponseDTO create(SupplierRequestDTO request) throws Exception {
        if (request == null) {
            log.error("[SUPPLIER] Creation attempt with a null SupplierRequestDTO");
            throw new IllegalArgumentException("SupplierRequestDTO cannot be null");
        }
        if (request.name() == null || request.email().isBlank()
                || request.phone() == null || request.phone().isBlank() || request.number() == null || request.number().isBlank()) {
            log.error("[SUPPLIER] Creation attempt with invalid data: {}", request);
            throw new IllegalArgumentException("Invalid SupplierRequestDTO data: " + request);
        }

        Optional<Supplier> existingSupplier = supplierRepository.findByNumberOrSiretIncludingDeleted(request.number(), request.siret());
        if(existingSupplier.isPresent()) {
            Supplier supplier = existingSupplier.get();
            if(!supplier.isDeleted()) {
                log.error("[SUPPLIER] Creation attempt with duplicate number or siret: {}", request);
                throw new IllegalArgumentException("Supplier with the same number or siret already exists");
            } else {
                log.warn("[SUPPLIER] Creation attempt with number or siret of a deleted supplier. Restoring supplier with ID {}: {}", supplier.getId(), supplier);
                supplier.setDeleted(false);
                supplier.setName(request.name());
                supplier.setCompanyName(request.companyName());
                supplier.setWebsite(request.website());
                supplier.setSiret(request.siret());
                supplier.setEmail(request.email());
                supplier.setPhoneNumber(request.phone());
                supplier.setAddress(request.address());
                supplier.setNumber(request.number());
                return mapToResponse(supplierRepository.save(supplier));
            }

        }
        Supplier supp  = Supplier.builder()
                .name(request.name())
                .companyName(request.companyName())
                .website(request.website())
                .siret(request.siret())
                .email(request.email())
                .phoneNumber(request.phone())
                .address(request.address())
                .number(request.number())
                .build();
        supplierRepository.save(supp);
        log.info("[SUPPLIER] Created new supplier with ID {}: {}", supp.getId(), supp);
        return mapToResponse(supp);

    }

    @Override
    public List<SupplierResponseDTO> getAll() throws Exception {
        return supplierRepository.findAllByDeletedFalse().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public SupplierResponseDTO getById(Long id) throws Exception {
        return supplierRepository.findByIdAndDeletedFalse(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found with ID: " + id));
    }

    @Override
    public SupplierResponseDTO update(Long id, SupplierRequestDTO request) throws Exception {
        if(request == null) {
            log.error("[SUPPLIER] Update attempt with a null SupplierRequestDTO for supplier ID {}", id);
            throw new IllegalArgumentException("SupplierRequestDTO cannot be null");
        }
        Supplier supplier = supplierRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> {
                    log.error("[SUPPLIER] Supplier not found with ID {} for update", id);
                    return new EntityNotFoundException("Supplier not found with ID: " + id);
                });
        if(request.number()!= null && !request.number().equals(supplier.getNumber())){
            if(supplierRepository.existsByNumberAndDeletedFalse(request.number())) {
                log.error("[SUPPLIER] Update attempt with duplicate number '{}' for supplier ID {}", request.number(), id);
                throw new IllegalArgumentException("Supplier with number " + request.number() + " already exists");
            }
        }
        if(request.siret() != null && !request.siret().equals(supplier.getSiret())){
            if(supplierRepository.existsBySiretAndDeletedFalse(request.siret())) {
                log.error("[SUPPLIER] Update attempt with duplicate siret '{}' for supplier ID {}", request.siret(), id);
                throw new IllegalArgumentException("Supplier with siret " + request.siret() + " already exists");
            }
        }
        supplier.setName(request.name());
        supplier.setCompanyName(request.companyName());
        supplier.setWebsite(request.website());
        supplier.setSiret(request.siret());
        supplier.setEmail(request.email());
        supplier.setPhoneNumber(request.phone());
        supplier.setAddress(request.address());
        supplier.setNumber(request.number());

        return mapToResponse(supplierRepository.save(supplier));
    }

    @Override
    public void delete(Long id) throws Exception {
        if(id == null || id <= 0) {
            throw new IllegalArgumentException("supplier ID must be greater than zero or can not be null.");
        }
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() ->{
                    log.error("[SUPPLIER] Supplier with ID {} not found for deletion", id);
                    return new EntityNotFoundException("Supplier not found with ID: " + id);
                });

        supplier.setDeleted(true);
        supplierRepository.save(supplier);
        log.info("[SUPPLIER] Supplier with ID {} marked as deleted", id);
    }


    private SupplierResponseDTO mapToResponse(Supplier supplier) {
        String createdByName = "—";
        if (supplier.getCreatedBy() != null) {
            createdByName = userRepository.findById(supplier.getCreatedBy())
                    .map(u -> u.getFirstname() + " " + u.getLastname())
                    .orElse("—");
        }

        return SupplierResponseDTO.builder()
                .id(supplier.getId())
                .number(supplier.getNumber())
                .name(supplier.getName())
                .companyName(supplier.getCompanyName())
                .email(supplier.getEmail())
                .phone(supplier.getPhoneNumber())
                .createdAt(supplier.getCreatedAt())
                .createdBy(supplier.getCreatedBy())
                .createdByName(createdByName)
                .build();
    }
}

