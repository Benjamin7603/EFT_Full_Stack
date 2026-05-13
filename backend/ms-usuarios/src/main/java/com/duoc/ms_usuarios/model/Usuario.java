package com.duoc.ms_usuarios.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(min=3, max=20,message = "El nombre debe tener entre 3 y 20 caracteres.")
    private String nombre;

    private String apellido;

    @Email(message = "El correo no tiene un formato valido")
    @NotBlank(message = "El correo es un campo obligatorio")
    @Column(unique = true, nullable = false) // <-- EVITA CORREOS REPETIDOS
    private String email;

    private String telefono;
    private Date fechaNacimiento;
    private String rol = "USER";

    @Column(unique = true, nullable = false) // <-- EVITA USERNAMES REPETIDOS
    private String username;

    private String password;
    private Boolean activo = true;
}