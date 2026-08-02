package com.tesseract.core;

import com.tesseract.module.BaseModule;
import com.tesseract.module.modules.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Gerencia o ciclo de vida de todos os módulos.
 * Para adicionar um novo módulo: instanciar aqui em init().
 */
public class ModuleManager {

    private final List<BaseModule> modules = new ArrayList<>();

    // -------------------------------------------------------------------------

    public void init() {
        // --- MODS ---
        register(new FPSModule());
        register(new ArmorHUDModule());
        register(new KeystrokesModule());
        register(new ZoomModule());
        register(new ClickGuiModule());
        register(new DamageIndicatorModule());
        register(new StatusEffectModule());

        // --- COSMETICS ---
        register(new CapeModule());

        // Ativa o CapeModule por padrão, já que é cosmético passivo
        // (o layer precisa estar injetado mesmo sem o usuário togglear)
        getModule(CapeModule.class).setEnabled(true);

        System.out.println("{Tesseract} " + modules.size() + " módulos carregados.");
    }

    // -------------------------------------------------------------------------

    private void register(BaseModule module) {
        modules.add(module);
    }

    /** Retorna o módulo pelo nome (case-insensitive), ou null se não existir. */
    public BaseModule getModule(String name) {
        return modules.stream()
                .filter(m -> m.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    /** Atalho para pegar módulo tipado. Ex: getModule(ZoomModule.class) */
    @SuppressWarnings("unchecked")
    public <T extends BaseModule> T getModule(Class<T> clazz) {
        return (T) modules.stream()
                .filter(m -> m.getClass() == clazz)
                .findFirst()
                .orElse(null);
    }

    /** Todos os módulos. */
    public List<BaseModule> getModules() {
        return modules;
    }

    /** Módulos filtrados por categoria. */
    public List<BaseModule> getModules(BaseModule.Category category) {
        return modules.stream()
                .filter(m -> m.getCategory() == category)
                .collect(Collectors.toList());
    }

    /** Verifica se algum módulo tem aquela keybind e faz toggle. */
    public void onKeyPress(int keyCode) {
        for (BaseModule module : modules) {
            if (module.getKeybind() == keyCode && module.isToggleByKey()) {
                module.toggle();
            }
        }
    }
}