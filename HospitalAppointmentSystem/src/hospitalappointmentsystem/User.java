/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hospitalappointmentsystem;

/**
 *
 * @author zubay
 */
public class User {
    
    private Integer id;
    private String fullname;
    private String username;
    private String password;
    private String role;
    private String contact;

    public User(Integer id, String fullname, String username, String password, String role, String contact) {
        this.id = id;
        this.fullname = fullname;
        this.username = username;
        this.password = password;
        this.role = role;
        this.contact = contact;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public Integer getId() {
        return id;
    }

    public String getFullname() {
        return fullname;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String getContact() {
        return contact;
    }
    
}
