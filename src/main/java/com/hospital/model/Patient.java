package com.hospital.model;
import java.time.*;
public record Patient(long id, String fullName, LocalDate dateOfBirth, String gender, String phone, String address, String bloodGroup, LocalDateTime registeredAt, boolean active) { }
