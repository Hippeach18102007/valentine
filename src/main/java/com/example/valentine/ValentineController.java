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

    // 4. Xử lý Gửi thư tình (Form thường)
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
    // ... Các phần cũ giữ nguyên ...

    // ============================================================
    // 👇 API MỚI: Nhận đánh giá từ người dùng
    // ============================================================
    @PostMapping("/api/send-feedback")
    @ResponseBody
    public ResponseEntity<?> sendFeedback(@RequestBody Map<String, Object> payload, HttpSession session) {
        // 1. Lấy thông tin người gửi từ Session
        String userEmail = (String) session.getAttribute("userEmail");
        if (userEmail == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Vui lòng đăng nhập để đánh giá!"));
        }

        // 2. Lấy dữ liệu gửi lên
        String content = (String) payload.get("content");
        Integer rating = (Integer) payload.get("rating");

        // Email Admin nhận phản hồi (Email của bạn)
        String adminEmail = "daod1068@gmail.com";

        try {
            emailService.sendFeedbackEmail(adminEmail, userEmail, content, rating);
            return ResponseEntity.ok(Map.of("message", "Cảm ơn bạn đã góp ý!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    @PostMapping("/send-wish") // 👈 Phải khớp chính xác với action="/send-wish" bên HTML
    public String sendWish(@RequestParam("loverEmail") String loverEmail,
                           @RequestParam("message") String message,
                           HttpSession session, Model model) {
        String userEmail = (String) session.getAttribute("userEmail");
        if (userEmail == null) return "redirect:/";

        try {
            emailService.sendLoveLetter(loverEmail, message, userEmail);
            model.addAttribute("success", "💌 Thư đã được gửi đi thành công!");
        } catch (Exception e) {
            model.addAttribute("error", "❌ Có lỗi xảy ra: " + e.getMessage());
        }

        model.addAttribute("userEmail", userEmail);
        return "home";
    }
    @PostMapping("/api/send-wish-ajax")
    @ResponseBody
    public ResponseEntity<?> sendWishAjax(@RequestBody Map<String, String> payload, HttpSession session) {
        String userEmail = (String) session.getAttribute("userEmail");
        if (userEmail == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Vui lòng đăng nhập lại!"));
        }

        String loverEmail = payload.get("loverEmail");
        String message = payload.get("message");

        if (loverEmail == null || message == null || loverEmail.isEmpty() || message.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng điền đầy đủ thông tin!"));
        }

        try {
            // Gọi hàm gửi thư trong EmailService
            emailService.sendLoveLetter(loverEmail, message, userEmail);
            return ResponseEntity.ok(Map.of("message", "Thư đã được gửi đi thành công! 💌"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Lỗi: " + e.getMessage()));
        }
    }

}