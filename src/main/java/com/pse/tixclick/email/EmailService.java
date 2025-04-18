package com.pse.tixclick.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;
    @Async
    public void sendNewMail(String to, String subject, String body,String fullname) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body.replace("{recipient_email}", fullname), true); // Replace {recipient_email} with actual recipient email

        mailSender.send(message);
    }

    public void sendOTPtoActiveAccount(String to, String otp,String fullname) throws MessagingException {
        String subject = "OTP to active account - TixClick";
        String body = "<html>" +
                "<body>" +
                "<h2 style=\"color: #0D6EFD;\">OTP code</h2>" +
                "<p>Dear " +  fullname +",</p>" +
                "<p>We received a request to active your account for the Koi Veterinary Service account associated with this email address. If you did not request this change, you can ignore this email.</p>" +
                "<p>To active your account, please use the following OTP code:</p>" +
                "<h3 style=\"color: #0D6EFD;\">" + otp + "</h3>" +
                "<p>This OTP code will expire in 15 minutes.</p>" +
                "<p>Thank you for using TixClick!</p>" +
                "<p>Best regards,<br/>TixClick</p>" +
                "</body>" +
                "</html>";
        sendNewMail(to, subject, body,fullname);
    }

    public void sendOTPtoChangePasswordAccount(String to, String otp, String fullname) throws MessagingException {
        String subject = "OTP to reset password - TixClick";
        String body = "<html>" +
                "<body>" +
                "<h2 style=\"color: #0D6EFD;\">OTP Code</h2>" +
                "<p>Dear " + fullname + ",</p>" +
                "<p>We received a request to reset the password for your account. Use the OTP code below to reset your password:</p>" +
                "<h3 style=\"color: #0D6EFD;\">" + otp + "</h3>" +
                "<p>This OTP will expire in 15 minutes.</p>" +
                "<p>Best regards,<br/>TixClick</p>" +
                "</body>" +
                "</html>";
        sendNewMail(to, subject, body, fullname);
    }

    public void sendAccountCreatedEmail(String to, String username, String password,String fullname) throws MessagingException {
        String subject = "Your TixClick Account Has Been Created!";

        String body = "<html>" +
                "<body>" +
                "<h2 style=\"color: #0D6EFD;\">Welcome to TixClick!</h2>" +
                "<p>Dear " + fullname + ",</p>" +
                "<p>Your account has been successfully created. Below are your login details:</p>" +
                "<p><strong>Username:</strong> " + username + "</p>" +
                "<p><strong>Password:</strong> " + password + "</p>" +
                "<p>Please change your password after logging in for security reasons.</p>" +
                "<p>You can log in to your account using the following link:</p>" +
                "<p><a href=\"https://tixclick.com/login\" style=\"color: #0D6EFD;\">Login to TixClick</a></p>" +
                "<p>Thank you for choosing TixClick!</p>" +
                "<p>Best regards,<br/>TixClick Team</p>" +
                "</body>" +
                "</html>";

        sendNewMail(to, subject, body, fullname);
    }

    public void sendCompanyCreationRequestNotification(String to, String companyName, String fullname) throws MessagingException {
        String subject = "New Company Creation Request - TixClick";
        String body = "<html>" +
                "<body>" +
                "<h2 style=\"color: #0D6EFD;\">Company Creation Request</h2>" +
                "<p>Dear " + fullname + ",</p>" +
                "<p>A new company creation request has been submitted with the following details:</p>" +
                "<p><strong>Company Name:</strong> " + companyName + "</p>" +
                "<p>Your request is currently under review. You will receive an update once the verification process is completed.</p>" +
                "<p>Thank you for using TixClick!</p>" +
                "<p>Best regards,<br/>TixClick Team</p>" +
                "</body>" +
                "</html>";

        sendNewMail(to, subject, body, fullname);
    }

    public void sendContractPaymentWarningToManager(String managerEmail, String companyName, double amount, LocalDate payDate, int contractId, int contractDetailId, String contractDetailName, String eventName) throws MessagingException {
        String subject = "Payment Due Notification - Contract #" + contractId;

        // Định dạng ngày
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formattedPayDate = payDate.format(formatter);

        String body = "<html>" +
                "<body>" +
                "<h2 style=\"color: #FF5733;\">Contract Payment Due</h2>" +
                "<p>Dear Manager,</p>" +
                "<p>The following contract is due for payment today:</p>" +
                "<ul>" +
                "<li><strong>Company Name:</strong> " + companyName + "</li>" +
                "<li><strong>Contract ID:</strong> " + contractId + "</li>" +
                "<li><strong>Event Name:</strong> " + eventName + "</li>" +
                "<li><strong>Contract Detail Id:</strong> " + contractDetailId + "</li>" +
                "<li><strong>Contract Detail Name:</strong> " + contractDetailName + "</li>" +
                "<li><strong>Amount Due:</strong> $" + amount + "</li>" +
                "<li><strong>Due Date:</strong> " + formattedPayDate + "</li>" +
                "</ul>" +
                "<p>Please ensure appropriate action is taken.</p>" +
                "<p>Best regards,<br/>TixClick System</p>" +
                "</body>" +
                "</html>";

        sendNewMail(managerEmail, subject, body, "Manager");
    }
    @Async
    public void sendEventStartNotification(String to, String eventName, String fullname) throws MessagingException {
        String subject = "Event Has Started - TixClick";
        String body = "<html>" +
                "<body>" +
                "<h2 style=\"color: #0D6EFD;\">Event Notification</h2>" +
                "<p>Dear " + fullname + ",</p>" +
                "<p>Your event has officially started:</p>" +
                "<p><strong>Event Name:</strong> " + eventName + "</p>" +
                "<p>We wish you a successful event!</p>" +
                "<p>Best regards,<br/>TixClick Team</p>" +
                "</body>" +
                "</html>";

        sendNewMail(to, subject, body, fullname);
    }
    @Async
    public void sendEventApprovalRequest(String to, String eventName, String creatorName) throws MessagingException {
        String subject = "Event Approval Required - TixClick";
        String body = "<html>" +
                "<body>" +
                "<h2 style=\"color: #0D6EFD;\">Event Approval Request</h2>" +
                "<p>Dear Manager,</p>" +
                "<p>The event <strong>" + eventName + "</strong> created by <strong>" + creatorName + "</strong> is pending approval.</p>" +
                "<p>Please review and approve the event at your earliest convenience.</p>" +
                "<p>Thank you for managing events on TixClick!</p>" +
                "<p>Best regards,<br/>TixClick Team</p>" +
                "</body>" +
                "</html>";
        sendNewMail(to, subject, body, "Manager");
    }
    public void sendAccountRegistrationToCompany(String to, String companyName, String registrationLink) throws MessagingException {
        String subject = "Account Registration for Company - TixClick";

        String body = "<html>" +
                "<body>" +
                "<h2 style=\"color: #0D6EFD;\">Account Registration</h2>" +
                "<p>Your account has been successfully registered with the following company:</p>" +
                "<p><strong>Company Name:</strong> " + companyName + "</p>" +
                "<p>We are excited to have you as a part of our company on TixClick!</p>" +
                "<p>Please <a href=\"" + registrationLink + "\" style=\"color: #0D6EFD;\">click here</a> to complete your registration or manage your account.</p>" +
                "<p>If you have any questions, feel free to contact us.</p>" +
                "<p>Best regards,<br/>TixClick Team</p>" +
                "</body>" +
                "</html>";

        sendNewMailCompany(to, subject, body, null);  // Truyền null vì không có fullname
    }

    public void sendEventApprovalNotification(String to, String eventName, String fullname) throws MessagingException {
        String subject = "Event Approved - TixClick";
        String body = "<html>" +
                "<body>" +
                "<h2 style=\"color: #0D6EFD;\">Event Approval Notification</h2>" +
                "<p>Dear " + fullname + ",</p>" +
                "<p>We are pleased to inform you that your event <strong>" + eventName + "</strong> has been <strong>approved</strong>.</p>" +
                "<p>Our team is now preparing the contract. We will contact you shortly for the next steps.</p>" +
                "<p>Thank you for choosing TixClick!</p>" +
                "<p>Best regards,<br/>TixClick Team</p>" +
                "</body>" +
                "</html>";

        sendNewMail(to, subject, body, fullname);
    }

    public void sendEventRejectionNotification(String to, String eventName, String fullname) throws MessagingException {
        String subject = "Event Rejected - TixClick";
        String body = "<html>" +
                "<body>" +
                "<h2 style=\"color: #dc3545;\">Event Rejection Notification</h2>" +
                "<p>Dear " + fullname + ",</p>" +
                "<p>We regret to inform you that your event <strong>" + eventName + "</strong> has been <strong>rejected</strong>.</p>" +
                "<p>Please review the submission criteria and make the necessary adjustments before resubmitting.</p>" +
                "<p>If you have any questions, feel free to contact our support team.</p>" +
                "<p>Best regards,<br/>TixClick Team</p>" +
                "</body>" +
                "</html>";

        sendNewMail(to, subject, body, fullname);
    }


    @Async
    public void sendNewMailCompany(String to, String subject, String body, String fullname) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        // Thay thế {recipient_email} bằng email của người nhận (to)
        body = body.replace("{recipient_email}", to);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true); // Chỉ gửi nội dung email đã thay thế

        mailSender.send(message);
    }

}
