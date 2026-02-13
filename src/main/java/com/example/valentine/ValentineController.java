package com.example.valentine;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ValentineController {

    private final EmailService emailService;

    // 1. Trang Đăng nhập
    @GetMapping("/")
    public String showLogin() {
        return "login";
    }

    // 2. Xử lý Đăng nhập (Lưu email vào Session)
    @PostMapping("/login")
    public String processLogin(@RequestParam("userEmail") String userEmail, HttpSession session) {
        System.out.println("✅ LOGIN - Saving to session: " + userEmail);
        session.setAttribute("userEmail", userEmail);
        return "redirect:/home";
    }

    // 3. Trang Chủ (Chứa nhạc, quay thưởng, gửi thư)
    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        String userEmail = (String) session.getAttribute("userEmail");

        // Nếu chưa đăng nhập thì đá về trang login
        if (userEmail == null) {
            System.out.println("⚠️ HOME - No session found, redirecting to login");
            return "redirect:/";
        }

        System.out.println("✅ HOME - User logged in: " + userEmail);
        model.addAttribute("userEmail", userEmail);
        return "home";
    }

    // 4. Đăng xuất
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        System.out.println("✅ LOGOUT - Session invalidated");
        return "redirect:/";
    }

    // 5. API GỬI QUÀ (AJAX)
    @PostMapping("/api/send-prize")
    @ResponseBody
    public ResponseEntity<?> sendPrize(@RequestBody Map<String, String> payload, HttpSession session) {
        System.out.println("📦 PRIZE API - Received payload: " + payload);

        // 1. Lấy thông tin từ Frontend gửi lên
        String receiverEmail = payload.get("email");
        String prize = payload.get("prize");

        System.out.println("📦 PRIZE API - Receiver: " + receiverEmail + ", Prize: " + prize);

        // 2. Lấy email người quay (người đang đăng nhập) từ Session
        String senderEmail = (String) session.getAttribute("userEmail");
        System.out.println("📦 PRIZE API - Sender from session: " + senderEmail);

        // Kiểm tra nếu chưa đăng nhập (hoặc session hết hạn)
        if (senderEmail == null) {
            System.out.println("❌ PRIZE API - Session is null!");
            return ResponseEntity.status(401).body(Map.of("error", "Bạn cần đăng nhập lại để gửi quà!"));
        }

        System.out.println("✅ PRIZE API - Sending email from " + senderEmail + " to " + receiverEmail);

        try {
            emailService.sendPrizeEmail(receiverEmail, prize, senderEmail);
            System.out.println("✅ PRIZE API - Email sent successfully!");
            return ResponseEntity.ok(Map.of("message", "Thành công"));
        } catch (Exception e) {
            System.out.println("❌ PRIZE API - Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 6. API GỬI LỜI CHÚC (AJAX) - ENDPOINT MỚI
    @PostMapping("/api/send-wish")
    @ResponseBody
    public ResponseEntity<?> sendWish(@RequestBody Map<String, String> payload, HttpSession session) {
        System.out.println("💌 WISH API - Received raw payload: " + payload);
        System.out.println("💌 WISH API - Payload class: " + payload.getClass().getName());
        System.out.println("💌 WISH API - Payload keys: " + payload.keySet());

        // Debug: Print each key-value
        for (Map.Entry<String, String> entry : payload.entrySet()) {
            System.out.println("  Key: '" + entry.getKey() + "' -> Value: '" + entry.getValue() + "'");
        }

        String senderEmail = (String) session.getAttribute("userEmail");
        System.out.println("💌 WISH API - Sender from session: " + senderEmail);

        if (senderEmail == null) {
            System.out.println("❌ WISH API - Session is NULL! User needs to login.");
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Vui lòng đăng nhập!");
            return ResponseEntity.status(401).body(errorResponse);
        }

        String loverEmail = payload.get("loverEmail");
        String message = payload.get("message");

        System.out.println("💌 WISH API - Extracted loverEmail: '" + loverEmail + "'");
        System.out.println("💌 WISH API - Extracted message: '" + message + "'");

        if (loverEmail == null || loverEmail.trim().isEmpty()) {
            System.out.println("❌ WISH API - loverEmail is null or empty");
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Email người nhận không được để trống!");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        if (message == null || message.trim().isEmpty()) {
            System.out.println("❌ WISH API - message is null or empty");
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Lời nhắn không được để trống!");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        System.out.println("✅ WISH API - All validations passed");
        System.out.println("✅ WISH API - Sending email from " + senderEmail + " to " + loverEmail);

        try {
            emailService.sendLoveLetter(loverEmail, message, senderEmail);
            System.out.println("✅ WISH API - Email sent successfully!");
            Map<String, String> successResponse = new HashMap<>();
            successResponse.put("message", "Gửi thành công! 💌");
            return ResponseEntity.ok(successResponse);
        } catch (Exception e) {
            System.out.println("❌ WISH API - Error sending email: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Lỗi gửi email: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // 7. API GỬI FEEDBACK (AJAX)
    @PostMapping("/api/send-feedback")
    @ResponseBody
    public ResponseEntity<?> sendFeedback(@RequestBody Map<String, Object> payload, HttpSession session) {
        System.out.println("⭐ FEEDBACK API - Received payload: " + payload);

        String userEmail = (String) session.getAttribute("userEmail");
        System.out.println("⭐ FEEDBACK API - User from session: " + userEmail);

        if (userEmail == null) {
            System.out.println("❌ FEEDBACK API - Session is null!");
            return ResponseEntity.status(401).body(Map.of("error", "Vui lòng đăng nhập!"));
        }

        String content = (String) payload.get("content");

        // Cách lấy Rating an toàn cho cả Integer và Double
        Object ratingObj = payload.get("rating");
        Integer rating = 0;
        if (ratingObj instanceof Number) {
            rating = ((Number) ratingObj).intValue();
        }

        System.out.println("⭐ FEEDBACK API - Content: " + content + ", Rating: " + rating);

        String adminEmail = "daod1068@gmail.com";

        try {
            emailService.sendFeedbackEmail(adminEmail, userEmail, content, rating);
            System.out.println("✅ FEEDBACK API - Email sent successfully!");
            return ResponseEntity.ok(Map.of("message", "Cảm ơn bạn đã đánh giá!"));
        } catch (Exception e) {
            System.out.println("❌ FEEDBACK API - Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}