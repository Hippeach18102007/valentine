package com.example.valentine;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        session.setAttribute("userEmail", userEmail);
        return "redirect:/home";
    }

    // 3. Trang Chủ (Chứa nhạc, quay thưởng, gửi thư)
    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        String userEmail = (String) session.getAttribute("userEmail");

        // Nếu chưa đăng nhập thì đá về trang login
        if (userEmail == null) {
            return "redirect:/";
        }

        model.addAttribute("userEmail", userEmail);
        return "home";
    }

    // 4. Đăng xuất
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    // 5. API GỬI QUÀ (AJAX)
    @PostMapping("/api/send-prize")
    @ResponseBody
    public ResponseEntity<?> sendPrize(@RequestBody Map<String, String> payload, HttpSession session) {
        // 1. Lấy thông tin từ Frontend gửi lên
        String receiverEmail = payload.get("email");
        String prize = payload.get("prize");

        // 2. Lấy email người quay (người đang đăng nhập) từ Session
        String senderEmail = (String) session.getAttribute("userEmail");

        // Kiểm tra nếu chưa đăng nhập (hoặc session hết hạn)
        if (senderEmail == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Bạn cần đăng nhập lại để gửi quà!"));
        }

        System.out.println("LOG: " + senderEmail + " gửi quà '" + prize + "' tới " + receiverEmail);

        try {
            // 3. Gọi hàm gửi mail mới (truyền đủ 3 tham số)
            emailService.sendPrizeEmail(receiverEmail, prize, senderEmail);
            return ResponseEntity.ok(Map.of("message", "Thành công"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 6. API GỬI LỜI CHÚC (AJAX) - ENDPOINT MỚI
    @PostMapping("/api/send-wish")
    @ResponseBody
    public ResponseEntity<?> sendWish(@RequestBody Map<String, String> payload, HttpSession session) {
        String senderEmail = (String) session.getAttribute("userEmail");

        if (senderEmail == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Vui lòng đăng nhập!"));
        }

        String loverEmail = payload.get("loverEmail");
        String message = payload.get("message");

        if (loverEmail == null || loverEmail.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email người nhận không được để trống!"));
        }

        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Lời nhắn không được để trống!"));
        }

        System.out.println("LOG: " + senderEmail + " gửi thư tình tới " + loverEmail);

        try {
            emailService.sendLoveLetter(loverEmail, message, senderEmail);
            return ResponseEntity.ok(Map.of("message", "Gửi thành công! 💌"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Lỗi gửi email: " + e.getMessage()));
        }
    }

    // 7. API GỬI FEEDBACK (AJAX)
    @PostMapping("/api/send-feedback")
    @ResponseBody
    public ResponseEntity<?> sendFeedback(@RequestBody Map<String, Object> payload, HttpSession session) {
        String userEmail = (String) session.getAttribute("userEmail");
        if (userEmail == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Vui lòng đăng nhập!"));
        }

        String content = (String) payload.get("content");

        // Cách lấy Rating an toàn cho cả Integer và Double
        Object ratingObj = payload.get("rating");
        Integer rating = 0;
        if (ratingObj instanceof Number) {
            rating = ((Number) ratingObj).intValue();
        }

        String adminEmail = "daod1068@gmail.com";

        try {
            emailService.sendFeedbackEmail(adminEmail, userEmail, content, rating);
            return ResponseEntity.ok(Map.of("message", "Cảm ơn bạn đã đánh giá!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}