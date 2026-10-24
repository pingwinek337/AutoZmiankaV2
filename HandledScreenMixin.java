package pl.autoreroll.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.autoreroll.AutoRerollClient;

/** Tryb wyboru slotow: klikniecie slotu (w ekwipunku lub w oknie serwera) zapisuje go do configu. */
@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin implements AutoRerollClient.ScreenPos {
	@Shadow
	protected Slot focusedSlot;
	@Shadow
	protected int x;
	@Shadow
	protected int y;

	@Override
	public int autorerollX() {
		return this.x;
	}

	@Override
	public int autorerollY() {
		return this.y;
	}

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void autoreroll$learnSlot(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		if (AutoRerollClient.learn == AutoRerollClient.LearnMode.NONE) return;

		MinecraftClient client = MinecraftClient.getInstance();
		cir.setReturnValue(true);

		Slot slot = this.focusedSlot;
		if (slot == null) return;

		if (slot.getStack().isEmpty()) {
			AutoRerollClient.notify(client, "§cTo pusty slot - kliknij slot z przedmiotem.");
			return;
		}

		boolean player = slot.inventory instanceof PlayerInventory;
		int index = player ? slot.getIndex() : slot.id;
		String name = slot.getStack().getName().getString();
		String where = player ? "ekwipunek" : "okno serwera";

		if (AutoRerollClient.learn == AutoRerollClient.LearnMode.SWORD) {
			AutoRerollClient.CONFIG.swordPlayer = player;
			AutoRerollClient.CONFIG.swordSlot = index;
			AutoRerollClient.CONFIG.swordName = name;
			AutoRerollClient.notify(client, "Miecz ustawiony: " + name + " (" + where + ", slot " + index + ").");
		} else {
			AutoRerollClient.CONFIG.scrollPlayer = player;
			AutoRerollClient.CONFIG.scrollSlot = index;
			AutoRerollClient.CONFIG.scrollName = name;
			AutoRerollClient.notify(client, "Zmianki ustawione: " + name + " (" + where + ", slot " + index + ").");
		}
		AutoRerollClient.CONFIG.save();
		AutoRerollClient.learn = AutoRerollClient.LearnMode.NONE;
		AutoRerollClient.notify(client, "Zamknij okno i wcisnij O, zeby dalej ustawiac, albo wcisnij R, zeby startowac.");
	}
}
