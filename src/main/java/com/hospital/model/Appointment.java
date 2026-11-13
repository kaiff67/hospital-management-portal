package com.hospital.model;
import java.time.LocalDateTime;
public record Appointment(long id, long patientId, long doctorId, LocalDateTime startAt, LocalDateTime endAt, String status, String reason) { }
