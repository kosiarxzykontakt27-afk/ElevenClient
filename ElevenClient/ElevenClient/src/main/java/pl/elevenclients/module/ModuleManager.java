package pl.elevenclients.module;

import net.fabricmc.loader.api.FabricLoader;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        // Mods / Combat
        add(new Builtins.Cps().on());
        add(new Builtins.Keystrokes());
        add(new Builtins.Ping());
        // Mods / Player
        add(new Builtins.ArmorStatus().on());
        add(new Builtins.PotionStatus().on());
        add(new Builtins.TotemCounter());
        // Mods / Movement
        add(new Builtins.ToggleSprint().on());
        add(new Builtins.ToggleSneak());
        add(new Builtins.Zoom().slider("Si\u0142a", 2, 10, 4, "x"));
        add(new Builtins.Speed());
        // Mods / World
        add(new Builtins.Coordinates().on());
        add(new Builtins.Direction());
        add(new Builtins.Clock());
        // Visual / Mods
        add(new Builtins.TimeChanger().slider("Godzina", 0, 24, 12, "h"));
        add(new Builtins.WeatherChanger());
        // Visual / Visual
        add(new Builtins.Fullbright());
        add(new Builtins.Crosshair().slider("Rozmiar", 2, 20, 8, "px").slider("Przerwa", 0, 10, 3, "px"));
        add(new Builtins.HurtCam());
        add(new Builtins.FireOverlay());
        // Visual / HUD
        add(new Builtins.Watermark().on());
        add(new Builtins.Fps().on());
        load();
    }

    private void add(Module m) { modules.add(m); }

    public List<Module> getAll() { return modules; }

    public List<Module> get(String page, String group) {
        List<Module> out = new ArrayList<>();
        for (Module m : modules) if (m.page.equals(page) && m.group.equals(group)) out.add(m);
        return out;
    }

    public boolean isOn(String name) {
        for (Module m : modules) if (m.name.equals(name)) return m.isEnabled();
        return false;
    }

    /* ---------- zapis stanu modulow ---------- */

    private Path file() { return FabricLoader.getInstance().getConfigDir().resolve("elevenclients-modules.properties"); }

    private void load() {
        try {
            Path p = file();
            if (!Files.exists(p)) return;
            Properties pr = new Properties();
            try (InputStream in = Files.newInputStream(p)) { pr.load(in); }
            for (Module m : modules) {
                String en = pr.getProperty(m.name);
                if (en != null) m.setEnabled(Boolean.parseBoolean(en));
                for (Module.Setting s : m.settings) {
                    String v = pr.getProperty(m.name + "." + s.label);
                    if (v != null) s.value = Float.parseFloat(v);
                }
            }
        } catch (Exception ignored) { }
    }

    public void save() {
        try {
            Properties pr = new Properties();
            for (Module m : modules) {
                pr.setProperty(m.name, Boolean.toString(m.isEnabled()));
                for (Module.Setting s : m.settings) pr.setProperty(m.name + "." + s.label, Float.toString(s.value));
            }
            try (OutputStream out = Files.newOutputStream(file())) { pr.store(out, "ElevenClients modules"); }
        } catch (Exception ignored) { }
    }
}
