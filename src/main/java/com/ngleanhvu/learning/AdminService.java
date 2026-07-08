package com.ngleanhvu.learning;

@Author(
        name = "Anh Vu",
        email = "anhvu@gmail.com"
)
@Role("ADMIN")
public class AdminService {
    String name;
    private int age = 10;

    public AdminService() {

    }

    public AdminService(String name, int age) {
        this.age = age;
        this.name = name;
    }

    public String getInfo() {
        return String.format("Name: %s - Age: %d", name, age);
    }

    private int calculateBirthday() {
        return 2026 - age;
    }
}
