package com.github.gpf.java.date;

import java.time.*;

public class DateTest {
    public static void main(String[] args) {
        System.out.println(LocalDate.now());
        System.out.println(LocalDate.now(ZoneId.systemDefault()));
        System.out.println(LocalDate.now(Clock.systemDefaultZone()));
        System.out.println(LocalDate.now().plusDays(1));

        System.out.println("##########");

        System.out.println(LocalTime.now());

        System.out.println("##########");

        System.out.println(LocalDateTime.now());
        System.out.println(LocalDateTime.now(ZoneId.systemDefault()));
        System.out.println(LocalDateTime.now(Clock.systemDefaultZone()));;
        System.out.println(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));
    }
}
