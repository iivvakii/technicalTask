package com.example.technicaltask.entity;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter

public enum JobFunctions {
    ACOUNTING("Accounting & Finance"),
    ADMINISTRATION("Administration"),
    COMPALIANCE_REGULATORY("Compliance / Regulatory"),
    CUSTOMER_SERVICE("Customer Service"),
    DATA_SCIENCE("Data Science"),
    DESIGN("Design"),
    IT("IT"),
    LEGAL("Legal"),
    MARKETING("Marketing & Communications"),
    OPERATIONS("Operations"),
    OTHER_ENGINEERING("Other Engineering"),
    PEOPLE_HR("People & HR"),
    PRODUCT("Product"),
    QUALITY_ASSURANCE("Quality Assurance"),
    SALES_BUSINESS_DEVELOPMENT("Sales & Business Development"),
    SOFTWARE_ENGINEERING("Software Engineering");

    String name;

}
