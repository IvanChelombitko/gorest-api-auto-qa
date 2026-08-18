package ua.solvd.gorest.util;

import ua.solvd.gorest.constant.Constant;
import ua.solvd.gorest.model.UserPayload;

import java.util.UUID;

public class PayloadTemplate {
    private static String generateUniqueEmail() {
        return UUID.randomUUID().toString().substring(0, 8) + Constant.DEFAULT_EMAIL;
    }

    public static UserPayload getValidUser() {
        return new UserPayload(Constant.DEFAULT_USERNAME, generateUniqueEmail(), Constant.MALE_GENDER, Constant.STATUS_ACTIVE);
    }

    public static UserPayload getValidUserWithSpecificName(String name) {
        return new UserPayload(name, generateUniqueEmail(), Constant.FEMALE_GENDER, Constant.STATUS_ACTIVE);
    }

    public static UserPayload getUserWithInvalidEmail() {
        return new UserPayload(Constant.INVALID_USERNAME, Constant.INVALID_EMAIL, Constant.MALE_GENDER, Constant.STATUS_ACTIVE);
    }
}