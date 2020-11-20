package eu.opertusmundi.web.model.catalogue.client;

import lombok.Getter;

public enum EnumDraftStatus {
    DRAFT("draft"),
    REVIEW("review"),
    ACCEPTED("accepted"),
    EMBARGO("embargo")
    ;

    @Getter
    private String value;

    private EnumDraftStatus(String value) {
        this.value = value;
    }

    public static EnumDraftStatus fromValue(String value) {
        for (final EnumDraftStatus e : EnumDraftStatus.values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }

        throw new IllegalArgumentException(String.format("Value [%s] is not a valid member of enum [EnumDraftStatus]", value));
    }

}
