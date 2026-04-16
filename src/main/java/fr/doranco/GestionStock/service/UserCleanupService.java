package fr.doranco.GestionStock.service;


import fr.doranco.GestionStock.model.entity.User;
import fr.doranco.GestionStock.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCleanupService {
    private final IUserRepository userRepository;

    // S'exécute chaque jour à minuit : "0 0 0 * * ?"
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void anonymizeDeletedUsersJob() {

        LocalDateTime limitDate = LocalDateTime.now().minusDays(30);

        List<User> usersToAnonymize = userRepository.findUsersReadyForAnonymization(limitDate);
        if (usersToAnonymize.isEmpty()) {
            log.info("No users to anonymize at the moment...");
            return;
        }

        for (User user : usersToAnonymize) {
            log.info("Anonymizing user : {}", user.getUsername());
            user.anonymize();
            userRepository.save(user);
            log.info("User ID {} was anonymized after 30 days of retention", user.getId());
        }
    }

   /* @Scheduled(fixedDelay = 100000) // S'exécute toutes les 60 secondes (1 min)
    @Transactional
    public void anonymizeDeletedUsersJob() {

        // On définit la limite à 1 minute en arrière pour le test
        LocalDateTime limitDate = LocalDateTime.now().plusHours(3).minusMinutes(1);

        List<User> usersToAnonymize = userRepository.findUsersReadyForAnonymization(limitDate);
        log.info("Anonymisation1 de : {}", usersToAnonymize);

        if (usersToAnonymize.isEmpty()) {
            log.info("Aucun utilisateur à anonymiser pour le moment...");
        }

        for (User user : usersToAnonymize) {
            log.info("Anonymisation de : {}", user.getUsername());
            user.anonymize();
            userRepository.save(user);
            log.info("TEST : L'utilisateur ID {} a été anonymisé après 1 minute.", user.getId());
        }
    }*/
}
