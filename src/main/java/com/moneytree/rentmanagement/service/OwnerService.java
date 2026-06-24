package com.moneytree.rentmanagement.service;

import com.moneytree.rentmanagement.model.Owner;
import com.moneytree.rentmanagement.repository.OwnerRepository;
import com.moneytree.rentmanagement.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OwnerService {

    private final OwnerRepository ownerRepository;
    private final UserRepository userRepository;

    public OwnerService(OwnerRepository ownerRepository, UserRepository userRepository) {
        this.ownerRepository = ownerRepository;
        this.userRepository = userRepository;
    }

    public List<Owner> findAll() {
        return ownerRepository.findAll();
    }

    public Owner findById(Long id) {
        return ownerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid owner id: " + id));
    }

    public Owner save(Owner owner) {
        return ownerRepository.save(owner);
    }

    /**
     * Delete an owner, first removing its linked login account. Refuses to delete an owner that
     * still has properties, since cascading would silently remove their tenants and payments —
     * those must be reassigned or removed first.
     *
     * @throws IllegalStateException if the owner still has properties.
     */
    @Transactional
    public void deleteById(Long id) {
        Owner owner = ownerRepository.findById(id).orElse(null);
        if (owner == null) {
            return; // already gone — nothing to do
        }
        if (owner.getProperties() != null && !owner.getProperties().isEmpty()) {
            throw new IllegalStateException(
                    "Cannot delete " + owner.getName() + " — they still have "
                            + owner.getProperties().size() + " propert"
                            + (owner.getProperties().size() == 1 ? "y" : "ies")
                            + ". Reassign or delete those properties first.");
        }
        // Remove the owner's login account (FK users.owner_id) before deleting the owner.
        userRepository.findByOwnerId(id).ifPresent(userRepository::delete);
        ownerRepository.delete(owner);
    }

    public long count() {
        return ownerRepository.count();
    }
}
