package com.myapp.recipe.domain.model;

import lombok.Getter;

@Getter
public enum Unit {
    GRAM("g"),
    KILOGRAM("kg"),
    MILLIGRAM("mg"),
    MICROGRAM("µg"),
    POUND("lb"),
    OUNCE("oz"),

    MILLILITER("ml"),
    LITER("l"),
    CENTILITER("cl"),
    DECILITER("dl"),
    FLUID_OUNCE("fl oz"),
    PINT("pt"),
    QUART("qt"),
    GALLON("gal"),

    TEASPOON("TL"),
    TABLESPOON("EL"),
    CUP("Tasse"),
    SHOT("Shot"),

    PIECE("Stück"),
    SLICE("Scheibe"),
    LEAF("Blatt"),
    CLOVE("Zehe"),
    PINCH("Prise"),
    DASH("Schuss"),
    DROP("Tropfen"),

    PACKAGE("Packung"),
    CAN("Dose"),
    JAR("Glas"),
    BUNCH("Bund"),
    SPRIG("Zweig")
    ;

    private final String abbreviation;

    Unit(String abbreviation) {
        this.abbreviation = abbreviation;
    }


    public static Unit fromAbbreviation(String abbreviation) {
        for (Unit unit : values()) {
            if (unit.getAbbreviation().equalsIgnoreCase(abbreviation)
                    || unit.name().equalsIgnoreCase(abbreviation)) {
                return unit;
            }
        }
        throw new IllegalArgumentException("Unknown unit abbreviation: " + abbreviation);
    }



}
