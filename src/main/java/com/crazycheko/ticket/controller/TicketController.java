package com.crazycheko.ticket.controller;

import com.crazycheko.ticket.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/init")
    public ResponseEntity<?> initEvent(
            @RequestParam String eventId,
            @RequestParam int totalTickets) {
        ticketService.initEvent(eventId, totalTickets);
        return ResponseEntity.ok(Map.of(
                "eventId", eventId,
                "totalTickets", totalTickets,
                "message", "活动初始化成功"
        ));
    }

    @PostMapping("/buy")
    public ResponseEntity<?> buyTicket(@RequestBody BuyRequest request) {
        TicketService.BuyResult result = ticketService.buyTicket(
                request.eventId(),
                request.userId(),
                request.quantity(),
                request.maxPerUser()
        );

        if (result.success()) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", result.message(),
                    "remainingTickets", result.remainingTickets(),
                    "orderNo", result.orderNo()
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", result.message()
            ));
        }
    }

    @GetMapping("/remaining")
    public ResponseEntity<?> getRemaining(@RequestParam String eventId) {
        int remaining = ticketService.getRemainingTickets(eventId);
        return ResponseEntity.ok(Map.of(
                "eventId", eventId,
                "remainingTickets", remaining
        ));
    }

    public record BuyRequest(String eventId, String userId, int quantity, int maxPerUser) {}


    @GetMapping("/test")
    public ResponseEntity<?> test() {

        return ResponseEntity.ok(Map.of(
                "test", "goodjob"
        ));
    }
}
