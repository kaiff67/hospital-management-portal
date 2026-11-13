package com.hospital.model;
public record User(long id, String username, String passwordHash, Role role, Long doctorId, boolean active) { }
