package sid.service_admin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envoi d'email pour l'inscription autonome d'une compagnie (voir
 * CompagnieService.inscriptionAutonome) - le seul usage d'email
 * transactionnel du systeme pour l'instant. SMTP configure via
 * spring.mail.* (voir cloud-config-gescom/microservice-admin*.properties) -
 * identifiants fournis par variables d'environnement, jamais en clair.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void envoyerEmailVerification(String destinataire, String nomCompagnie, String lienVerification) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(destinataire);
        message.setSubject("Confirmez votre adresse email - EasyCom-Pro");
        message.setText(
                "Bonjour,\n\n"
                + "Votre compagnie \"" + nomCompagnie + "\" a bien ete creee sur EasyCom-Pro.\n\n"
                + "Cliquez sur le lien ci-dessous pour confirmer votre adresse email et activer votre compte "
                + "(valable 24 heures) :\n\n"
                + lienVerification + "\n\n"
                + "Si vous n'etes pas a l'origine de cette inscription, ignorez cet email.\n\n"
                + "L'equipe EasyCom-Pro"
        );
        log.info("Envoi email de verification a {}", destinataire);
        mailSender.send(message);
    }
}
