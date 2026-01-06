package com.example.PressuAttend;

import java.util.ArrayList;

public class StudentInformation {

    private String name;
    private String rollno;
    private String department;
    private String phone;
    private String password;
    private String email;
    private ArrayList<Integer> subjects;

    // 🔥 PENTING: untuk attendance checkbox
    private boolean present = false;

    // WAJIB: Empty constructor untuk Firebase
    public StudentInformation() {}

    public StudentInformation(
            String name,
            String rollno,
            String department,
            String phone,
            String email,
            String password,
            ArrayList<Integer> subjects
    ) {
        this.name = name;
        this.rollno = rollno;
        this.department = department;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.subjects = subjects;
        this.present = false;
    }

    // ---------- ATTENDANCE ----------
    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }

    // ---------- SUBJECT ----------
    public ArrayList<Integer> getSubjects() {
        return subjects;
    }

    public void setSubjects(ArrayList<Integer> subjects) {
        this.subjects = subjects;
    }

    // ---------- BASIC INFO ----------
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRollno() {
        return rollno;
    }

    public void setRollno(String rollno) {
        this.rollno = rollno;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // ---------- PASSWORD ----------
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
