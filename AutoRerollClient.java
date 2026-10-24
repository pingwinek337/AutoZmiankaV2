package pl.autoreroll;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoRerollClient implements ClientModInitializer {
	public enum LearnMode { NONE, SWORD, SCROLL }

	/** Implementowane przez mixin, daje pozycje okna GUI na ekranie. */
	public interface ScreenPos {
		int autorerollX();
		int autorerollY();
	}

	public static final RerollConfig CONFIG = RerollConfig.load();

	public static LearnMode learn = LearnMode.NONE;

	public static boolean running = false;
	public static int rolls = 0;
	public static double lastPercent = -1;

	private static int stage = 0;
	private static int cooldown = 0;
	private static int misses = 0;
	private static Item swordItem = null;

	private static KeyBinding toggleKey;
	private static KeyBinding configKey;
	private static Pattern pattern;

	@Override
	public void onInitializeClient() {
		toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.autoreroll.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.autoreroll"));
		configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.autoreroll.config", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O, "category.autoreroll"));

		ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
			if (screen instanceof HandledScreen<?>) {
				ScreenKeyboardEvents.afterKeyPress(screen).register((s, key, scancode, modifiers) -> {
					if (toggleKey.matchesKey(key, scancode)) {
						if (running) stop(client, "Zatrzymano.");
						else if (learn == LearnMode.NONE) start(client);
					}
				});
				ScreenEvents.afterRender(screen).register((s, ctx, mx, my, td) -> overlay(client, s, ctx));
			}
		});

		ClientTickEvents.END_CLIENT_TICK.register(AutoRerollClient::tick);
	}

	public static void notify(MinecraftClient client, String text) {
		if (client.player != null) {
			client.player.sendMessage(Text.literal("§6[AutoReroll] §f" + text), false);
		}
	}

	public static String keyName() {
		return toggleKey.getBoundKeyLocalizedText().getString();
	}

	private static boolean validate(MinecraftClient client) {
		if (!License.isActive()) {
			notify(client, "§cBrak licencji. Wcisnij O i wpisz klucz.");
			return false;
		}
		if (CONFIG.swordSlot < 0 || CONFIG.scrollSlot < 0) {
			notify(client, "§cNajpierw wybierz miecz i zmianki (klawisz O).");
			return false;
		}
		try {
			pattern = Pattern.compile(CONFIG.percentRegex);
		} catch (Exception e) {
			notify(client, "§cZly regex w configu: " + e.getMessage());
			return false;
		}
		return true;
	}

	private static void start(MinecraftClient client) {
		if (!validate(client)) return;
		running = true;
		rolls = 0;
		misses = 0;
		cooldown = 0;
		stage = 0;
		lastPercent = -1;
		notify(client, String.format("Start! Szukam %d%% - %d%%. Stop: %s",
				CONFIG.min, CONFIG.max, keyName()));
	}

	private static void stop(MinecraftClient client, String reason) {
		if (running) {
			running = false;
			notify(client, reason + " (rolli: " + rolls + ")");
		}
	}

	/** Slot w ekwipunku gracza (po numerze) albo w oknie serwera (po numerze slotu). */
	private static Slot findSlot(ScreenHandler handler, boolean player, int index) {
		if (index < 0) return null;
		if (player) {
			for (Slot s : handler.slots) {
				if (s.inventory instanceof PlayerInventory && s.getIndex() == index) return s;
			}
			return null;
		}
		if (index >= handler.slots.size()) return null;
		Slot s = handler.slots.get(index);
		return (s.inventory instanceof PlayerInventory) ? null : s;
	}

	private static void click(MinecraftClient client, ScreenHandler handler, Slot slot, int button) {
		client.interactionManager.clickSlot(handler.syncId, slot.id, button, SlotActionType.PICKUP, client.player);
	}

	private static void tick(MinecraftClient client) {
		while (configKey.wasPressed()) {
			if (client.currentScreen == null) {
				client.setScreen(License.isActive()
						? new RerollConfigScreen(null)
						: new License.PromptScreen());
			}
		}

		if (!running) return;

		if (!(client.currentScreen instanceof HandledScreen<?> screen)
				|| client.player == null || client.interactionManager == null) {
			stop(client, "Okno zamkniete, stop.");
			return;
		}

		if (cooldown > 0) {
			cooldown--;
			return;
		}

		ScreenHandler handler = screen.getScreenHandler();
		Slot sword = findSlot(handler, CONFIG.swordPlayer, CONFIG.swordSlot);
		Slot scroll = findSlot(handler, CONFIG.scrollPlayer, CONFIG.scrollSlot);
		if (sword == null || scroll == null) {
			stop(client, "§cNie widze slotow miecza/zmianek w tym oknie.");
			return;
		}

		ItemStack cursor = handler.getCursorStack();
		int delay = Math.max(1, CONFIG.delayTicks);

		switch (stage) {
			case 0 -> {
				if (!cursor.isEmpty()) {
					stop(client, "§cMasz przedmiot na kursorze - odloz go i wystartuj ponownie.");
					return;
				}
				Double p = parse(sword.getStack());
				if (p == null) {
					misses++;
					if (misses > 10) {
						stop(client, "§cNie znalazlem procentu na mieczu. Sprawdz wybrany slot.");
					}
					cooldown = delay;
					return;
				}
				misses = 0;
				lastPercent = p;

				if (p >= CONFIG.min && p <= CONFIG.max) {
					stop(client, String.format("§aTrafiono %s%%!", fmt(p)));
					return;
				}
				if (CONFIG.maxRolls > 0 && rolls >= CONFIG.maxRolls) {
					stop(client, "Osiagnieto limit rolli.");
					return;
				}
				if (scroll.getStack().isEmpty()) {
					stop(client, "§cSkonczyly sie zmianki.");
					return;
				}
				swordItem = sword.getStack().getItem();
				// 1) prawy (lub lewy) klik na slocie zmianki
				click(client, handler, scroll, CONFIG.rightClickPickup ? 1 : 0);
				stage = 1;
				cooldown = delay;
			}
			case 1 -> {
				// 2) klik w miecz
				click(client, handler, sword, 0);
				rolls++;
				stage = 2;
				cooldown = delay;
			}
			case 2 -> {
				if (!cursor.isEmpty()) {
					// 3) odkladamy to, co zostalo na kursorze (zmianka albo miecz)
					Slot back = (swordItem != null && cursor.isOf(swordItem)) ? sword : scroll;
					click(client, handler, back, 0);
					stage = 3;
					cooldown = delay;
				} else {
					stage = 0;
				}
			}
			case 3 -> {
				if (!cursor.isEmpty()) {
					stop(client, "§cCos zostalo na kursorze, zatrzymano dla bezpieczenstwa.");
					return;
				}
				stage = 0;
			}
			default -> stage = 0;
		}
	}

	private static String fmt(double d) {
		return d == Math.floor(d) ? String.valueOf((long) d) : String.format("%.1f", d);
	}

	private static void overlay(MinecraftClient client, Screen screen, DrawContext ctx) {
		var tr = client.textRenderer;
		int w = screen.width;

		if (learn != LearnMode.NONE) {
			String t = learn == LearnMode.SWORD ? "Kliknij MIECZ w tym oknie" : "Kliknij ZMIANKE w tym oknie";
			ctx.fill(w / 2 - 120, 6, w / 2 + 120, 26, 0xE0101018);
			ctx.drawCenteredTextWithShadow(tr, Text.literal(t), w / 2, 12, 0xFFFFD166);
		} else if (running) {
			String t = String.format("AutoReroll: roll #%d | ostatnio %s | cel %d-%d%% | %s = stop",
					rolls, lastPercent < 0 ? "-" : fmt(lastPercent) + "%",
					CONFIG.min, CONFIG.max, keyName());
			int tw = tr.getWidth(t);
			ctx.fill(w / 2 - tw / 2 - 8, 6, w / 2 + tw / 2 + 8, 26, 0xE0101018);
			ctx.drawCenteredTextWithShadow(tr, Text.literal(t), w / 2, 12, 0xFF7CFFB0);
		}

		if (screen instanceof ScreenPos sp && screen instanceof HandledScreen<?> hs) {
			ScreenHandler h = hs.getScreenHandler();
			highlight(ctx, sp, findSlot(h, CONFIG.swordPlayer, CONFIG.swordSlot), 0xFFFFD166);
			highlight(ctx, sp, findSlot(h, CONFIG.scrollPlayer, CONFIG.scrollSlot), 0xFFB07CFF);
		}
	}

	private static void highlight(DrawContext ctx, ScreenPos sp, Slot slot, int color) {
		if (slot == null) return;
		int x = sp.autorerollX() + slot.x;
		int y = sp.autorerollY() + slot.y;
		ctx.fill(x - 1, y - 1, x + 17, y, color);
		ctx.fill(x - 1, y + 16, x + 17, y + 17, color);
		ctx.fill(x - 1, y, x, y + 16, color);
		ctx.fill(x + 16, y, x + 17, y + 16, color);
	}

	private static Double parse(ItemStack stack) {
		if (stack.isEmpty() || pattern == null) return null;

		StringBuilder sb = new StringBuilder(stack.getName().getString());
		LoreComponent lore = stack.get(DataComponentTypes.LORE);
		if (lore != null) {
			for (Text line : lore.lines()) {
				sb.append('\n').append(line.getString());
			}
		}

		Matcher m = pattern.matcher(sb);
		if (m.find()) {
			try {
				return Double.parseDouble(m.group(1).replace(',', '.'));
			} catch (NumberFormatException ignored) {
			}
		}
		return null;
	}
}
