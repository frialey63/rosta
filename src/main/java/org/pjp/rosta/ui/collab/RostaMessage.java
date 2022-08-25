package org.pjp.rosta.ui.collab;

import java.time.LocalDate;

public record RostaMessage(MessageType messageType, int uiId, LocalDate date) {

    public enum MessageType { DAY_CREATE, DAY_DELETE, SHIFT_UPDATE }

}
