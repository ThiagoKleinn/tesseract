package com.tesseract.module.config;

import java.util.List;

public interface Configurable {
    List<ModuleOption<?>> getOptions();
    /** Chamado quando qualquer opção muda — salva config */
    void onOptionChanged();
}