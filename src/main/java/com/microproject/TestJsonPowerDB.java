
package com.microproject;

import com.google.gson.JsonObject;

public class TestJsonPowerDB {

    public static void main(String[] args) {

        try {
            JsonPowerDBService service = new JsonPowerDBService();

            JsonObject response = service.getStudent(900001);

            System.out.println("JSONPowerDB Response:");
            System.out.println(response);

        } catch (Exception e) {
            System.out.println("Error connecting to JSONPowerDB:");
            e.printStackTrace();
        }
    }
}
