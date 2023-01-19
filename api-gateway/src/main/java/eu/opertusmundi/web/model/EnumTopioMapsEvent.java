package eu.opertusmundi.web.model;

public enum EnumTopioMapsEvent {
    MAP_CREATED,
    MAP_DELETED,
    ;

    public static EnumTopioMapsEvent from(String value) {
        for (final EnumTopioMapsEvent e : EnumTopioMapsEvent.values()) {
            if (e.name().equals(value)) {
                return e;
            }
        }
        return null;
    }
}
