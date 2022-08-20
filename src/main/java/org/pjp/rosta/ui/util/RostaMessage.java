package org.pjp.rosta.ui.util;

public record RostaMessage(MessageType messageType, int uiId) {

    public enum MessageType { DAY_CREATE, DAY_DELETE, SHIFT_UPDATE }

}
