package com.company.taskmanagement.controller;

import com.company.taskmanagement.service.ReportService;
import com.company.taskmanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserService userService;

    /**
     * Страница генерации отчетов
     */
    @GetMapping
    public String reportsPage(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("defaultStartDate", LocalDate.now().minusDays(30));
        model.addAttribute("defaultEndDate", LocalDate.now());
        return "admin/reports";
    }

    /**
     * Скачивание отчета в формате CSV (для Excel)
     */
    @GetMapping("/download-excel")
    public ResponseEntity<byte[]> downloadExcelReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long userId) {

        try {
            // Валидация дат
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest()
                        .body("Дата начала не может быть позже даты окончания".getBytes());
            }

            if (endDate.isAfter(LocalDate.now())) {
                return ResponseEntity.badRequest()
                        .body("Дата окончания не может быть в будущем".getBytes());
            }

            byte[] excelContent = reportService.generateExcelReport(startDate, endDate, userId);

            // ФИКС: Улучшенное имя файла
            String filename;
            if (userId != null) {
                var user = userService.getUserById(userId);
                if (user.isPresent()) {
                    filename = "отчет_" + user.get().getUsername() + "_" +
                            startDate + "_по_" + endDate + ".csv";
                } else {
                    filename = "отчет_пользователя_" + userId + "_" +
                            startDate + "_по_" + endDate + ".csv";
                }
            } else {
                filename = "отчет_всех_пользователей_" + startDate + "_по_" + endDate + ".csv";
            }

            // ФИКС: Правильные заголовки для CSV
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                    .header("Content-Transfer-Encoding", "binary")
                    .body(excelContent);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(("Ошибка генерации отчета: " + e.getMessage()).getBytes());
        }
    }

    /**
     * Быстрый отчет за последние 7 дней
     */
    @GetMapping("/last-week")
    public String lastWeekReport() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7);
        return "redirect:/admin/reports/download-excel?startDate=" + startDate + "&endDate=" + endDate;
    }

    /**
     * Быстрый отчет за последние 30 дней
     */
    @GetMapping("/last-month")
    public String lastMonthReport() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        return "redirect:/admin/reports/download-excel?startDate=" + startDate + "&endDate=" + endDate;
    }
}