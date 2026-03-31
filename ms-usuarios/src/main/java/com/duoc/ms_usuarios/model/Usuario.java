package com.duoc.ms_usuarios.model;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    @Id
    @GeneratedValue
    private Long id;
    @NotBlank(message = "El nombre es obligatorio.") //mensaje solo backend
    @Size(min=3, max=20,message = "El nombre debe tener entre 3 y 20 caracteres.")
    private String nombre;
    private String apellido;
    //Debe tener un formato de email
    @Email(message = "El correo no tiene con un formato valido")
    @NotBlank(message = "El correo es un campo obligatorio")
    private String email;
    private String telefono;
    private Date fechaNacimiento;
    private String rol;
    private String username;
    private String password;
    private boolean activo = true; //?
}
