package pl.elevenclients.module;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import pl.elevenclients.ElevenClients;
import pl.elevenclients.config.ElevenConfig;
import pl.elevenclients.gui.Draw;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Locale;

/** Wszystkie wbudowane moduly. */
public final class Builtins {
    private Builtins() {}

    private static int accent() { return 0xFF000000 | ElevenConfig.accent; }

    /* ===================== HUD ===================== */

    public static class Watermark extends Module {
        public Watermark() { super("visual", "HUD", "Watermark", "Nazwa klienta"); }
        @Override public void onRender(DrawContext c) { Hud.left(c, ElevenClients.NAME + " " + ElevenClients.VERSION); }
    }

    public static class Fps extends Module {
        public Fps() { super("visual", "HUD", "FPS", "Licznik klatek"); }
        @Override public void onRender(DrawContext c) { Hud.left(c, "FPS: " + mc.getCurrentFps()); }
    }

    public static class Coordinates extends Module {
        public Coordinates() { super("mods", "World", "Coordinates", "Koordynaty gracza"); }
        @Override public void onRender(DrawContext c) {
            if (mc.player == null) return;
            Hud.left(c, String.format(Locale.ROOT, "XYZ: %.1f / %.1f / %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ()));
        }
    }

    public static class Direction extends Module {
        private static final String[] N = {"Po\u0142udnie", "Zach\u00f3d", "P\u00f3\u0142noc", "Wsch\u00f3d"};
        public Direction() { super("mods", "World", "Direction", "Kierunek patrzenia"); }
        @Override public void onRender(DrawContext c) {
            if (mc.player == null) return;
            float yaw = MathHelper.wrapDegrees(mc.player.getYaw());
            int idx = ((int) Math.floor(yaw / 90f + 0.5f)) & 3;
            Hud.left(c, "Kierunek: " + N[idx] + " (" + Math.round(yaw) + "\u00b0)");
        }
    }

    public static class Clock extends Module {
        public Clock() { super("mods", "World", "Clock", "Aktualna godzina"); }
        @Override public void onRender(DrawContext c) { Hud.left(c, LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))); }
    }

    public static class Ping extends Module {
        public Ping() { super("mods", "Combat", "Ping", "Op\u00f3\u017anienie serwera"); }
        @Override public void onRender(DrawContext c) {
            if (mc.player == null) return;
            String t = "--";
            if (mc.getNetworkHandler() != null) {
                var e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
                if (e != null) t = e.getLatency() + " ms";
            }
            Hud.left(c, "Ping: " + t);
        }
    }

    public static class Cps extends Module {
        private final ArrayDeque<Long> left = new ArrayDeque<>(), right = new ArrayDeque<>();
        private boolean lp, rp;
        public Cps() { super("mods", "Combat", "CPS Counter", "Klikni\u0119cia na sekund\u0119"); }
        @Override public void onRender(DrawContext c) {
            long now = Util.getMeasuringTimeMs();
            long h = mc.getWindow().getHandle();
            boolean l = mc.currentScreen == null && GLFW.glfwGetMouseButton(h, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
            boolean r = mc.currentScreen == null && GLFW.glfwGetMouseButton(h, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
            if (l && !lp) left.add(now);
            if (r && !rp) right.add(now);
            lp = l; rp = r;
            while (!left.isEmpty() && now - left.peekFirst() > 1000) left.pollFirst();
            while (!right.isEmpty() && now - right.peekFirst() > 1000) right.pollFirst();
            Hud.left(c, "CPS: " + left.size() + " | " + right.size());
        }
    }

    public static class Speed extends Module {
        public Speed() { super("mods", "Movement", "Speed", "Pr\u0119dko\u015b\u0107 w blokach na sekund\u0119"); }
        @Override public void onRender(DrawContext c) {
            PlayerEntity p = mc.player;
            if (p == null) return;
            double dx = p.getX() - p.prevX, dz = p.getZ() - p.prevZ;
            Hud.left(c, String.format(Locale.ROOT, "Pr\u0119dko\u015b\u0107: %.1f b/s", Math.sqrt(dx * dx + dz * dz) * 20));
        }
    }

    public static class Keystrokes extends Module {
        public Keystrokes() { super("mods", "Combat", "Keystrokes", "Wci\u015bni\u0119te klawisze (WASD, spacja)"); }
        private void key(DrawContext c, int x, int y, int w, String label, boolean down) {
            Draw.round(c, x, y, x + w, y + 18, 5, down ? 0xCC000000 | ElevenConfig.accent : 0x99090E1C);
            c.drawText(mc.textRenderer, label, x + w / 2 - mc.textRenderer.getWidth(label) / 2, y + 5, 0xFFFFFFFF, false);
        }
        @Override public void onRender(DrawContext c) {
            int x0 = 6, y0 = mc.getWindow().getScaledHeight() - 66;
            key(c, x0 + 20, y0, 18, "W", mc.options.forwardKey.isPressed());
            key(c, x0, y0 + 20, 18, "A", mc.options.leftKey.isPressed());
            key(c, x0 + 20, y0 + 20, 18, "S", mc.options.backKey.isPressed());
            key(c, x0 + 40, y0 + 20, 18, "D", mc.options.rightKey.isPressed());
            key(c, x0, y0 + 40, 58, "SPACE", mc.options.jumpKey.isPressed());
        }
    }

    public static class ArmorStatus extends Module {
        public ArmorStatus() { super("mods", "Player", "Armor Status", "Pancerz i wytrzyma\u0142o\u015b\u0107"); }
        @Override public void onRender(DrawContext c) {
            if (mc.player == null) return;
            EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
            int x = mc.getWindow().getScaledWidth() - 26, y = mc.getWindow().getScaledHeight() / 2 - 36;
            for (int i = 0; i < slots.length; i++) {
                ItemStack st = mc.player.getEquippedStack(slots[i]);
                if (st.isEmpty()) continue;
                Draw.round(c, x - 3, y + i * 20 - 2, x + 19, y + i * 20 + 18, 5, 0x99090E1C);
                c.drawItem(st, x, y + i * 20);
                c.drawStackOverlay(mc.textRenderer, st, x, y + i * 20);
            }
        }
    }

    public static class PotionStatus extends Module {
        public PotionStatus() { super("mods", "Player", "Potion Status", "Aktywne efekty"); }
        @Override public void onRender(DrawContext c) {
            if (mc.player == null) return;
            for (StatusEffectInstance e : mc.player.getStatusEffects()) {
                int sec = e.getDuration() / 20;
                String name = e.getEffectType().value().getName().getString() + " " + (e.getAmplifier() + 1);
                Hud.right(c, name + "  " + (sec / 60) + ":" + String.format("%02d", sec % 60));
            }
        }
    }

    public static class TotemCounter extends Module {
        public TotemCounter() { super("mods", "Player", "Totem Counter", "Ile totem\u00f3w zosta\u0142o"); }
        @Override public void onRender(DrawContext c) {
            if (mc.player == null) return;
            int n = 0;
            var inv = mc.player.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                ItemStack st = inv.getStack(i);
                if (st.isOf(Items.TOTEM_OF_UNDYING)) n += st.getCount();
            }
            Hud.right(c, "Totemy: " + n);
        }
    }

    public static class Crosshair extends Module {
        public Crosshair() { super("visual", "Visual", "Crosshair", "W\u0142asny celownik"); }
        @Override public void onRender(DrawContext c) {
            if (mc.currentScreen != null || mc.player == null) return;
            int cx = mc.getWindow().getScaledWidth() / 2, cy = mc.getWindow().getScaledHeight() / 2;
            int size = Math.round(val(0)), gap = Math.round(val(1)), col = accent();
            c.fill(cx - gap - size, cy - 1, cx - gap, cy + 1, col);
            c.fill(cx + gap, cy - 1, cx + gap + size, cy + 1, col);
            c.fill(cx - 1, cy - gap - size, cx + 1, cy - gap, col);
            c.fill(cx - 1, cy + gap, cx + 1, cy + gap + size, col);
        }
    }

    /* ===================== ruch ===================== */

    public static class ToggleSprint extends Module {
        public ToggleSprint() { super("mods", "Movement", "Toggle Sprint", "Sprint bez trzymania"); }
        @Override public void onTick() {
            var p = mc.player;
            if (p == null) return;
            if (mc.options.forwardKey.isPressed() && !p.isSneaking() && !p.horizontalCollision
                    && !p.isUsingItem() && p.getHungerManager().getFoodLevel() > 6) {
                p.setSprinting(true);
            }
        }
    }

    public static class ToggleSneak extends Module {
        public ToggleSneak() { super("mods", "Movement", "Toggle Sneak", "Skradanie bez trzymania"); }
        @Override public void onTick() { if (mc.player != null) mc.options.sneakKey.setPressed(true); }
        @Override public void onDisable() { mc.options.sneakKey.setPressed(false); }
    }

    public static class Zoom extends Module {
        private final KeyBinding key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.elevenclients.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, "category.elevenclients"));
        private boolean zoomed;
        private int orig;

        public Zoom() { super("mods", "Movement", "Zoom", "Przybli\u017cenie (trzymaj C, FOV min. 30)"); }

        private void restore() {
            if (zoomed) { mc.options.getFov().setValue(orig); zoomed = false; }
        }

        @Override public void onTick() {
            if (mc.player == null) return;
            boolean down = key.isPressed() && mc.currentScreen == null;
            if (down && !zoomed) { orig = mc.options.getFov().getValue(); zoomed = true; }
            if (down) {
                int f = Math.round(orig / Math.max(1f, val(0)));
                mc.options.getFov().setValue(Math.max(30, Math.min(110, f)));
            } else restore();
        }

        @Override public void onDisable() { restore(); }
    }

    /* ===================== swiat / wyglad ===================== */

    public static class Fullbright extends Module {
        public Fullbright() { super("visual", "Visual", "Fullbright", "Pe\u0142na jasno\u015b\u0107"); }
        @Override public void onTick() {
            if (mc.player == null) return;
            StatusEffectInstance cur = mc.player.getStatusEffect(StatusEffects.NIGHT_VISION);
            if (cur == null || cur.getDuration() < 600) {
                mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 1200, 0, false, false, false));
            }
        }
        @Override public void onDisable() {
            if (mc.player != null) mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }

    /** Znaczniki - dzialanie robia mixiny (HurtCam / FireOverlay / Crosshair). */
    public static class HurtCam extends Module {
        public HurtCam() { super("visual", "Visual", "Hurt Cam", "Bez trz\u0119sienia kamery po obra\u017ceniach"); }
    }

    public static class FireOverlay extends Module {
        public FireOverlay() { super("visual", "Visual", "Fire Overlay", "Bez ognia na ekranie"); }
    }

    public static class TimeChanger extends Module {
        public TimeChanger() { super("visual", "Mods", "Time Changer", "W\u0142asna pora dnia"); }
        @Override public void onTick() {
            if (mc.world == null) return;
            long ticks = ((Math.round(val(0)) - 6 + 24) % 24) * 1000L;
            mc.world.setTime(mc.world.getTime(), ticks, false);
        }
    }

    public static class WeatherChanger extends Module {
        public WeatherChanger() { super("visual", "Mods", "Weather Changer", "Bez deszczu i burz"); }
        @Override public void onTick() {
            if (mc.world == null) return;
            mc.world.setRainGradient(0f);
            mc.world.setThunderGradient(0f);
        }
    }
}
