package com.ngleanhvu.learning;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        Class<AdminService> adminServiceClass = AdminService.class;

        AdminService adminService = new AdminService();

        Field fieldAge = adminServiceClass.getDeclaredField("age");
        fieldAge.setAccessible(true);
        Field fieldName = adminServiceClass.getDeclaredField("name");
        fieldName.setAccessible(true);

        fieldAge.set(adminService, 23);
        fieldName.set(adminService, "Anh Vu");

//        Method[] publicMethods = adminServiceClass.getMethods();
//        Method[] methods = adminServiceClass.getDeclaredMethods();

        Method method = adminServiceClass.getMethod("getInfo");
        method.setAccessible(true);

        Object result = method.invoke(adminService);

//        Constructor<?>[] publicConstructors = adminServiceClass.getConstructors();
//        Constructor<?>[] allConstructors = adminServiceClass.getDeclaredConstructors();
//

        Constructor<AdminService> adminServiceConstructor =
                AdminService.class.getConstructor(String.class, int.class);

        AdminService adminService1 =
                adminServiceConstructor.newInstance("AnhVu1", 231);

        System.out.println(adminService1.getInfo());


        Role role = adminServiceClass.getDeclaredAnnotation(Role.class);
        Annotation[] annotations = adminServiceClass.getAnnotations();

        int modifier = adminServiceClass.getModifiers();

        System.out.println(Modifier.isPublic(modifier));
        System.out.println(Modifier.isPrivate(modifier));


        System.out.println(result);
    }
}