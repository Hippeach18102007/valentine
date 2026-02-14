package com.example.valentine;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    // Inject JavaMailSender thay vì Resend
    private final JavaMailSender mailSender;

    // Gửi thư tình (Gửi cho bất kỳ ai)
    public void sendLoveLetter(String toEmail, String messageBody, String senderEmail) {
        String htmlContent = """
            <div style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto; border: 1px solid #ffccd5; border-radius: 10px; overflow: hidden;">
                <div style="background-color: #ff4d6d; padding: 20px; text-align: center;">
                    <h1 style="color: white; margin: 0;">💌 Lời Nhắn Yêu Thương</h1>
                </div>
                <div style="padding: 20px; background-color: #fff0f3;">
                    <p><strong>Người gửi:</strong> %s</p>
                    <hr style="border: 0; border-top: 1px dashed #ff8fa3; margin: 20px 0;">
                    <p style="font-size: 16px; line-height: 1.6; white-space: pre-wrap;">%s</p>
                    <hr style="border: 0; border-top: 1px dashed #ff8fa3; margin: 20px 0;">
                    <p style="text-align: center; color: #ff4d6d; font-weight: bold;">Happy Valentine's Day! 💘</p>
                </div>
            </div>
            """.formatted(senderEmail, messageBody);

        sendHtmlEmail(toEmail, "💖 Có một lời nhắn bí mật dành cho bạn!", htmlContent);
    }

    // Gửi quà trúng thưởng
    public void sendPrizeEmail(String toEmail, String prize, String senderEmail) {
        String htmlContent = """
            <div style="font-family: Arial, sans-serif; color: #333; text-align: center; border: 2px dashed #ff4d6d; padding: 20px; border-radius: 10px; max-width: 600px; margin: 0 auto;">
                <h1 style="color: #ff4d6d;">🎉 Chúc Mừng Valentine! 🎉</h1>
                <p style="font-size: 16px;">Người dùng <strong>%s</strong> đã dành tặng bạn món quà:</p>
                <h2 style="background-color: #ffe6ea; padding: 15px; border-radius: 8px; display: inline-block; color: #d63384; margin: 20px 0;">%s</h2>
                <p style="color: #666; font-size: 14px;">Hãy liên hệ lại với họ để nhận quà nhé! 🎁</p>
            </div>
            """.formatted(senderEmail, prize);

        sendHtmlEmail(toEmail, "🎁 " + senderEmail + " vừa gửi tặng bạn một món quà!", htmlContent);
    }

    // Gửi đánh giá cho Admin
    public void sendFeedbackEmail(String adminEmail, String userEmail, String content, int rating) {
        String stars = "⭐".repeat(rating);
        String htmlContent = """
            <div style="font-family: Arial, sans-serif; color: #333; border: 2px solid #6366f1; padding: 20px; border-radius: 10px; max-width: 600px; margin: 0 auto;">
                <h2 style="color: #6366f1; text-align: center;">📣 CÓ ĐÁNH GIÁ MỚI!</h2>
                <p><strong>Người gửi:</strong> %s</p>
                <p><strong>Đánh giá:</strong> %s (%d/5)</p>
                <p><strong>Nội dung:</strong> %s</p>
            </div>
            """.formatted(userEmail, stars, rating, content);

        sendHtmlEmail(adminEmail, "⭐ Đánh giá mới từ " + userEmail, htmlContent);
    }

    // --- HÀM HỖ TRỢ GỬI MAIL CHUNG ---
    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true để gửi định dạng HTML
            helper.setFrom("Valentine App <daod1068@gmail.com>"); // Thay bằng Gmail của bạn

            mailSender.send(message);
            System.out.println("✅ Email sent successfully to: " + to);
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi gửi email qua Gmail: " + e.getMessage());
        }
    }
}