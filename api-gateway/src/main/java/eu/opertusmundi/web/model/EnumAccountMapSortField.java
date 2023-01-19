package eu.opertusmundi.web.model;

import lombok.Getter;

public enum EnumAccountMapSortField {
    TITLE("title"),
    UPDATED_ON("updatedOn"),
    ;

    @Getter
    private String value;

    EnumAccountMapSortField(String value) {
        this.value = value;
    }

}
