package com.moneytree.rentmanagement.notification;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.nio.charset.StandardCharsets;

/**
 * Sends account-related emails (e.g. an owner's initial login credentials).
 * Uses the configured {@link JavaMailSender} when available; if mail is not configured
 * it logs a warning instead of failing, so the app still runs without SMTP.
 */
@Service
public class AccountEmailService {

    private static final Logger log = LoggerFactory.getLogger(AccountEmailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String from;
    private final String loginUrl;

    public AccountEmailService(ObjectProvider<JavaMailSender> mailSenderProvider,
                               @Value("${app.reminders.email.from:no-reply@rentmanager.local}") String from,
                               @Value("${app.login.url:http://rentify.ddns.net/login}") String loginUrl) {
        this.mailSenderProvider = mailSenderProvider;
        this.from = from;
        this.loginUrl = loginUrl;
    }

    /** Returns true if the credentials email was actually sent. */
    public boolean sendOwnerCredentials(String toEmail, String username, String temporaryPassword) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("Mail is not configured; skipping owner credentials email to {}", toEmail);
            return false;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(toEmail);
            helper.setSubject("Your RentManager login");
            helper.setText(buildHtml(username, temporaryPassword), true);
            mailSender.send(message);
            log.info("Owner credentials email sent to {}", toEmail);
            return true;
        } catch (Exception ex) {
            log.error("Failed to send owner credentials email to {}", toEmail, ex);
            return false;
        }
    }

    /** Builds the light-themed, block-based HTML body for the credentials email. */
    private String buildHtml(String username, String temporaryPassword) {
        String safeUsername = HtmlUtils.htmlEscape(username);
        String safePassword = HtmlUtils.htmlEscape(temporaryPassword);
        String safeLoginUrl = HtmlUtils.htmlEscape(loginUrl);

        return "<!DOCTYPE html>"
                + "<html><head><meta charset=\"UTF-8\"/>"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/></head>"
                + "<body style=\"margin:0;padding:0;background-color:#f1f5f9;"
                + "font-family:Arial,Helvetica,sans-serif;color:#1e293b;\">"
                + "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" "
                + "style=\"background-color:#f1f5f9;padding:32px 0;\"><tr><td align=\"center\">"

                // Card
                + "<table role=\"presentation\" width=\"560\" cellpadding=\"0\" cellspacing=\"0\" "
                + "style=\"max-width:560px;width:100%;background-color:#ffffff;border-radius:14px;"
                + "overflow:hidden;box-shadow:0 6px 24px rgba(15,23,42,0.08);\">"

                // Header band
                + "<tr><td style=\"background:linear-gradient(90deg,#4f46e5,#6366f1);"
                + "padding:28px 32px;\">"
                + "<span style=\"display:inline-block;width:40px;height:40px;line-height:40px;"
                + "text-align:center;background:rgba(255,255,255,0.18);border-radius:10px;"
                + "color:#ffffff;font-weight:bold;font-size:16px;\">RM</span>"
                + "<span style=\"color:#ffffff;font-size:20px;font-weight:bold;"
                + "vertical-align:middle;margin-left:12px;\">RentManager</span>"
                + "</td></tr>"

                // Intro
                + "<tr><td style=\"padding:32px 32px 8px;\">"
                + "<h1 style=\"margin:0 0 8px;font-size:22px;color:#0f172a;\">Welcome aboard 👋</h1>"
                + "<p style=\"margin:0;font-size:15px;line-height:1.6;color:#475569;\">"
                + "An owner account has been created for you in RentManager. "
                + "Use the credentials below to sign in.</p>"
                + "</td></tr>"

                // Credential blocks
                + "<tr><td style=\"padding:20px 32px 4px;\">"
                + credentialBlock("Username", safeUsername)
                + credentialBlock("Temporary password", safePassword)
                + "</td></tr>"

                // CTA button
                + "<tr><td align=\"center\" style=\"padding:24px 32px 8px;\">"
                + "<a href=\"" + safeLoginUrl + "\" "
                + "style=\"display:inline-block;background:#4f46e5;color:#ffffff;text-decoration:none;"
                + "font-size:15px;font-weight:bold;padding:14px 34px;border-radius:10px;\">"
                + "Log in to RentManager</a>"
                + "</td></tr>"

                // Fallback link
                + "<tr><td align=\"center\" style=\"padding:0 32px 8px;\">"
                + "<p style=\"margin:0;font-size:13px;color:#64748b;\">Or open this link:<br/>"
                + "<a href=\"" + safeLoginUrl + "\" style=\"color:#4f46e5;\">" + safeLoginUrl + "</a></p>"
                + "</td></tr>"

                // Security note
                + "<tr><td style=\"padding:16px 32px 28px;\">"
                + "<div style=\"background-color:#fff7ed;border:1px solid #fed7aa;border-radius:10px;"
                + "padding:14px 16px;font-size:13px;line-height:1.5;color:#9a3412;\">"
                + "🔒 For your security, you'll be asked to set a new password the first time you sign in."
                + "</div>"
                + "</td></tr>"

                // Footer
                + "<tr><td style=\"background-color:#f8fafc;border-top:1px solid #e2e8f0;"
                + "padding:18px 32px;text-align:center;\">"
                + "<p style=\"margin:0;font-size:12px;color:#94a3b8;\">"
                + "This is an automated message from RentManager. Please do not reply.</p>"
                + "</td></tr>"

                + "</table></td></tr></table></body></html>";
    }

    /** A single labelled data block used inside the email body. */
    private String credentialBlock(String label, String value) {
        return "<div style=\"background-color:#f8fafc;border:1px solid #e2e8f0;border-radius:10px;"
                + "padding:14px 16px;margin-bottom:12px;\">"
                + "<div style=\"font-size:12px;text-transform:uppercase;letter-spacing:0.5px;"
                + "color:#94a3b8;margin-bottom:4px;\">" + label + "</div>"
                + "<div style=\"font-size:16px;font-weight:bold;color:#0f172a;"
                + "font-family:'Courier New',monospace;word-break:break-all;\">" + value + "</div>"
                + "</div>";
    }
}
