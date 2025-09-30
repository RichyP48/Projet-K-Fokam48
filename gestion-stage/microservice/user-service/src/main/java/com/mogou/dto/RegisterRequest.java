package com.mogou.dto;


import com.mogou.model.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    @Email(message = "L'adresse email doit être valide.")
    private String email;

    @NotBlank
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères.")
    private String password;

    @NotNull(message = "Le rôle est obligatoire.")
    private Role role;

    @NotNull
    @Valid // Valide également les champs de l'objet imbriqué
    private UserProfileDto profile;
}
