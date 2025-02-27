package com.pse.tixclick.controller;

import com.pse.tixclick.payload.dto.SeatDTO;
import com.pse.tixclick.payload.request.create.CreateSeatRequest;
import com.pse.tixclick.payload.request.update.UpdateSeatRequest;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.service.SeatService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/seat")
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SeatController {
    @Autowired
    SeatService seatService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<SeatDTO>> creatSeat(@RequestBody CreateSeatRequest createSeatRequest){
        try {
            SeatDTO seatDTO = seatService.createSeat(createSeatRequest);
            return ResponseEntity.ok(
                    ApiResponse.<SeatDTO>builder()
                            .code(200)
                            .message("Seat created successfully")
                            .result(seatDTO)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.<SeatDTO>builder()
                            .code(400)
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<SeatDTO>> updateSeat(@RequestBody UpdateSeatRequest updateSeatRequest, @PathVariable int id){
        try {
            SeatDTO seatDTO = seatService.updateSeat(updateSeatRequest, id);
            return ResponseEntity.ok(
                    ApiResponse.<SeatDTO>builder()
                            .code(200)
                            .message("Seat updated successfully")
                            .result(seatDTO)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.<SeatDTO>builder()
                            .code(400)
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<String>> deleteSeat(@PathVariable int id){
        try {
            seatService.deleteSeat(id);
            return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                            .code(200)
                            .message("Seat deleted successfully")
                            .result("Seat deleted successfully")
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.<String>builder()
                            .code(400)
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<SeatDTO>>> getAllSeats(){
        try {
            List<SeatDTO> seatDTOs = seatService.getAllSeats();
            return ResponseEntity.ok(
                    ApiResponse.<List<SeatDTO>>builder()
                            .code(200)
                            .message("Successfully fetched all seats")
                            .result(seatDTOs)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.<List<SeatDTO>>builder()
                            .code(400)
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }

    @GetMapping("/zone/{zoneId}")
    public ResponseEntity<ApiResponse<List<SeatDTO>>> getSeatsByZone(@PathVariable int zoneId){
        try {
            List<SeatDTO> seatDTOs = seatService.getSeatsByZone(zoneId);
            return ResponseEntity.ok(
                    ApiResponse.<List<SeatDTO>>builder()
                            .code(200)
                            .message("Successfully fetched seats by zone")
                            .result(seatDTOs)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.<List<SeatDTO>>builder()
                            .code(400)
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }

    @GetMapping("/seat_map/{seatMapId}")
    public ResponseEntity<ApiResponse<List<SeatDTO>>> getSeatsBySeatMap(@PathVariable int seatMapId){
        try {
            List<SeatDTO> seatDTOs = seatService.getSeatsBySeatMap(seatMapId);
            return ResponseEntity.ok(
                    ApiResponse.<List<SeatDTO>>builder()
                            .code(200)
                            .message("Successfully fetched seats by seat map")
                            .result(seatDTOs)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.<List<SeatDTO>>builder()
                            .code(400)
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }

}
