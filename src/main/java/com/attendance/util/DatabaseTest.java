
package com.attendance.util;

import java.sql.Connection;

public class DatabaseTest {

    public static void main(String[] args) {

        try (Connection con =
                 DBConnection.getConnection()) {

            System.out.println(
                "MySQL connected successfully!"
            );

        } catch (Exception e) {
            System.out.println(
                "Database connection failed!"
            );

            e.printStackTrace();
        }
    }
}
