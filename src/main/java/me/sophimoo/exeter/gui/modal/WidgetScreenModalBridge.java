package me.sophimoo.exeter.gui.modal;

import meteordevelopment.meteorclient.gui.WidgetScreen;

public interface WidgetScreenModalBridge {
    void exeter$openModal(WidgetScreen modal);

    WidgetScreen exeter$getModalTarget();

    boolean exeter$hasModals();

    void exeter$closeModal(WidgetScreen modal);

    void exeter$setModalHost(WidgetScreen host);
}
