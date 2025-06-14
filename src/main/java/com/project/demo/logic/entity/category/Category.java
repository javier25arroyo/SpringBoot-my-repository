package com.project.demo.logic.entity.category;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.demo.logic.entity.product.Product;
import jakarta.persistence.*;
import java.util.List;

@Table(name = "category")
@Entity
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String descripcion;

    @JsonIgnore
    @OneToMany(mappedBy = "category")
    private List<Product> productos;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<Product> getProductos() {
        return productos;
    }

    public void setProductos(List<Product> productos) {
        this.productos = productos;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", productos=" + productos +
                '}';
    }
}

//id, nombre, descripción
//Relacion de 1 categoria a 1 o + productos
