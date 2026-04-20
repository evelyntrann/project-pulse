package com.projectpulse.registration.dto;

public record InvitationValidateResponse(
        String email,       // pre-fills the read-only email field on the form
        String role,        // STUDENT or INSTRUCTOR — frontend uses this to call the right register endpoint
        String sectionName, // shown on the form so the student knows which section they're joining
        boolean expired,    // true → show "this link has expired" message instead of form
        boolean alreadyUsed // true → Extension 2a: redirect to login
) {}
