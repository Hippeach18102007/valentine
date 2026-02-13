package com.example.valentine;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
// 👇 HAI DÒNG IMPORT QUAN TRỌNG ĐỂ HẾT LỖI ĐỎ
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final Resend resend;

    @Value("${mail.from}")
    private String fromEmailAddress;

    @Value("${mail.from-name}")
    private String fromName;

    // Gửi thư tình
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

        // ✅ Dùng CreateEmailOptions (Code mới)
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromName + " <" + fromEmailAddress + ">")
                .to(toEmail)
                .subject("💖 Có một lời nhắn bí mật dành cho bạn!")
                .html(htmlContent)
                .build();

        try {
            CreateEmailResponse data = resend.emails().send(params);
            System.out.println("Email seant. ID: " + data.getId());
        } catch (ResendException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi gửi email: " + e.getMessage());
        }
    }

    // Gửi quà trúng thưởng
    public void sendPrizeEmail(String toEmail, String prize, String senderEmail) {
        String htmlContent = """
                <div style="font-family: Arial, sans-serif; color: #333; text-align: center; border: 2px dashed #ff4d6d; padding: 20px; border-radius: 10px; max-width: 600px; margin: 0 auto;">
                    <h1 style="color: #ff4d6d;">🎉 Chúc Mừng Valentine! 🎉</h1>
                
                    <p style="font-size: 16px;">
                        Người dùng <strong>%s</strong> đã quay vòng quay may mắn và dành tặng bạn món quà:
                    </p>
                
                    <h2 style="background-color: #ffe6ea; padding: 15px; border-radius: 8px; display: inline-block; color: #d63384; margin: 20px 0;">
                        %s
                    </h2>
                
                    <p style="color: #666; font-size: 14px;">Hãy liên hệ lại với họ để nhận quà nhé! 🎁</p>
                </div>
                """.formatted(senderEmail, prize); // Thay thế %s đầu tiên bằng email người gửi, %s thứ hai bằng giải thưởng

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromName + " <" + fromEmailAddress + ">")
                .to(toEmail)
                .subject("🎁 " + senderEmail + " vừa gửi tặng bạn một món quà!")
                .html(htmlContent)
                .build();

        try {
            resend.emails().send(params);
            System.out.println("Prize email sent to: " + toEmail + " from: " + senderEmail);
        } catch (ResendException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi gửi email quà: " + e.getMessage());
        }
    }
    // ... Các hàm cũ (sendLoveLetter, sendPrizeEmail) giữ nguyên ...

    // 👇 HÀM MỚI: Gửi đánh giá về cho Admin (bạn)
    public void sendFeedbackEmail(String adminEmail, String userEmail, String content, int rating) {
        String stars = "⭐".repeat(rating); // Tạo chuỗi sao (Ví dụ: ⭐⭐⭐⭐⭐)

        String htmlContent = """
            <div style="font-family: Arial, sans-serif; color: #333; border: 2px solid #6366f1; padding: 20px; border-radius: 10px; max-width: 600px; margin: 0 auto;">
                <h2 style="color: #6366f1; text-align: center;">📣 CÓ ĐÁNH GIÁ MỚI!</h2>
                
                <div style="background-color: #f5f3ff; padding: 15px; border-radius: 8px; margin: 20px 0;">
                    <p><strong>Người gửi:</strong> %s</p>
                    <p><strong>Đánh giá:</strong> <span style="font-size: 20px;">%s</span> (%d/5)</p>
                </div>
                
                <p><strong>Nội dung góp ý:</strong></p>
                <blockquote style="border-left: 4px solid #6366f1; padding-left: 15px; color: #555; font-style: italic;">
                    "%s"
                </blockquote>
                
                <hr style="border: 0; border-top: 1px dashed #ccc; margin: 20px 0;">
                <p style="text-align: center; font-size: 12px; color: #888;">Email này được gửi tự động từ hệ thống Valentine Dashboard.</p>
            </div>
            """.formatted(userEmail, stars, rating, content);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromName + " <" + fromEmailAddress + ">")
                .to(adminEmail) // Gửi cho chính bạn (daod1068@gmail.com)
                .subject("⭐ Đánh giá mới từ " + userEmail)
                .html(htmlContent)
                .build();

        try {
            resend.emails().send(params);
            System.out.println("Feedback sent to Admin from: " + userEmail);
        } catch (ResendException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi gửi feedback: " + e.getMessage());
        }
    }
}