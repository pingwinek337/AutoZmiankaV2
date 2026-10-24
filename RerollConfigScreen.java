package pl.autoreroll;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.function.DoubleConsumer;

public class RerollConfigScreen extends Screen {
	private static final int W = 280;
	private static final int H = 290;

	private static final int BG = 0xF0101018;
	private static final int HEADER = 0xFF1B1B2B;
	private static final int ACCENT = 0xFF7C5CFF;
	private static final int ROW = 0xFF181826;
	private static final int TEXT_DIM = 0xFFA8A8C0;
	private static final int GOLD = 0xFFFFD166;
	private static final int PURPLE = 0xFFB07CFF;

	private final Screen parent;
	private Slider minSlider, maxSlider;
	private TextFieldWidget limitField;
	private ButtonWidget pickBtn;

	private int px, py;

	public RerollConfigScreen(Screen parent) {
		super(Text.literal("AutoReroll"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		RerollConfig c = AutoRerollClient.CONFIG;
		px = (width - W) / 2;
		py = Math.max(4, (height - H) / 2);

		minSlider = new Slider(px + 14, py + 56, W - 28, 20, "Zatrzymaj od", 0, 100, c.min, "%", v -> {
			c.min = (int) Math.round(v);
			if (c.min > c.max) {
				c.max = c.min;
				maxSlider.setReal(c.max);
			}
		});
		maxSlider = new Slider(px + 14, py + 80, W - 28, 20, "Zatrzymaj do", 0, 100, c.max, "%", v -> {
			c.max = (int) Math.round(v);
			if (c.max < c.min) {
				c.min = c.max;
				minSlider.setReal(c.min);
			}
		});
		addDrawableChild(minSlider);
		addDrawableChild(maxSlider);

		addDrawableChild(ButtonWidget.builder(Text.literal("Wybierz"), b -> beginLearn(AutoRerollClient.LearnMode.SWORD))
				.dimensions(px + W - 14 - 84, py + 120, 84, 20).build());
		addDrawableChild(ButtonWidget.builder(Text.literal("Wybierz"), b -> beginLearn(AutoRerollClient.LearnMode.SCROLL))
				.dimensions(px + W - 14 - 84, py + 146, 84, 20).build());

		addDrawableChild(new Slider(px + 14, py + 184, 160, 20, "Opoznienie", 1, 20, c.delayTicks, " tk",
				v -> c.delayTicks = (int) Math.round(v)));

		limitField = new TextFieldWidget(textRenderer, px + W - 14 - 84, py + 184, 84, 20, Text.empty());
		limitField.setMaxLength(6);
		limitField.setText(String.valueOf(c.maxRolls));
		addDrawableChild(limitField);

		pickBtn = ButtonWidget.builder(pickText(), b -> {
			c.rightClickPickup = !c.rightClickPickup;
			b.setMessage(pickText());
		}).dimensions(px + 14, py + 212, W - 28, 20).build();
		addDrawableChild(pickBtn);

		addDrawableChild(ButtonWidget.builder(Text.literal("Gotowe"), b -> close())
				.dimensions(px + 14, py + H - 34, W - 28, 22).build());
	}

	private Text pickText() {
		return Text.literal("Klik na zmianke: "
				+ (AutoRerollClient.CONFIG.rightClickPickup ? "prawy przycisk myszy" : "lewy przycisk myszy"));
	}

	private void beginLearn(AutoRerollClient.LearnMode mode) {
		save();
		AutoRerollClient.learn = mode;
		client.setScreen(null);
		AutoRerollClient.notify(client, mode == AutoRerollClient.LearnMode.SWORD
				? "Otworz okno z mieczem (np. Miksowanie przedmiotu) i kliknij MIECZ."
				: "Otworz okno ze zmiankami (np. Miksowanie przedmiotu) i kliknij ZMIANKE.");
	}

	private void save() {
		RerollConfig c = AutoRerollClient.CONFIG;
		try {
			c.maxRolls = Math.max(0, Integer.parseInt(limitField.getText().trim()));
		} catch (NumberFormatException ignored) {
		}
		c.save();
	}

	@Override
	public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.renderBackground(ctx, mouseX, mouseY, delta);

		ctx.fill(px - 1, py - 1, px + W + 1, py + H + 1, ACCENT);
		ctx.fill(px, py, px + W, py + H, BG);
		ctx.fill(px, py, px + W, py + 30, HEADER);
		ctx.fill(px, py + 30, px + W, py + 31, ACCENT);
		ctx.drawTextWithShadow(textRenderer, "AutoReroll", px + 14, py + 11, 0xFFFFFFFF);
		String hint = "R w oknie = start/stop";
		ctx.drawTextWithShadow(textRenderer, hint, px + W - 14 - textRenderer.getWidth(hint), py + 11, TEXT_DIM);

		ctx.drawTextWithShadow(textRenderer, "ZAKRES ZATRZYMANIA", px + 14, py + 41, TEXT_DIM);
		ctx.drawTextWithShadow(textRenderer, "PRZEDMIOTY", px + 14, py + 107, TEXT_DIM);
		ctx.drawTextWithShadow(textRenderer, "USTAWIENIA", px + 14, py + 171, TEXT_DIM);
		ctx.drawTextWithShadow(textRenderer, "Limit rolli (0 = brak)", px + W - 14 - 84, py + 171, TEXT_DIM);

		RerollConfig c = AutoRerollClient.CONFIG;
		row(ctx, py + 118, "Miecz", c.swordSlot, c.swordPlayer, c.swordName, GOLD);
		row(ctx, py + 144, "Zmianki", c.scrollSlot, c.scrollPlayer, c.scrollName, PURPLE);

		String tip = "Otworz okno Miksowania i wcisnij R.";
		ctx.drawCenteredTextWithShadow(textRenderer, Text.literal(tip), px + W / 2, py + 240, TEXT_DIM);
	}

	private void row(DrawContext ctx, int y, String label, int slot, boolean player, String name, int color) {
		ctx.fill(px + 14, y, px + W - 14 - 90, y + 24, ROW);
		ctx.fill(px + 14, y, px + 16, y + 24, color);
		ctx.drawTextWithShadow(textRenderer, label, px + 22, y + 3, color);

		String info;
		int infoColor = 0xFFFFFFFF;
		if (slot < 0) {
			info = "nie wybrano";
			infoColor = TEXT_DIM;
		} else {
			String n = name == null ? "" : name;
			if (textRenderer.getWidth(n) > 70) n = textRenderer.trimToWidth(n, 66) + "..";
			info = n + " (" + (player ? "ekw." : "okno") + " #" + slot + ")";
			if (textRenderer.getWidth(info) > 150) info = textRenderer.trimToWidth(info, 146) + "..";
		}
		ctx.drawTextWithShadow(textRenderer, info, px + 22, y + 13, infoColor);
	}

	@Override
	public void close() {
		save();
		client.setScreen(parent);
	}

	/** Suwak z etykieta i calkowita wartoscia. */
	private static class Slider extends SliderWidget {
		private final String label;
		private final double min, max;
		private final String suffix;
		private final DoubleConsumer onChange;

		Slider(int x, int y, int w, int h, String label, double min, double max,
			   double value, String suffix, DoubleConsumer onChange) {
			super(x, y, w, h, Text.empty(), (value - min) / (max - min));
			this.label = label;
			this.min = min;
			this.max = max;
			this.suffix = suffix;
			this.onChange = onChange;
			updateMessage();
		}

		private double real() {
			double v = min + this.value * (max - min);
			return Math.max(min, Math.min(max, Math.round(v)));
		}

		void setReal(double v) {
			this.value = (v - min) / (max - min);
			updateMessage();
		}

		@Override
		protected void updateMessage() {
			if (label == null) return;
			setMessage(Text.literal(label + ": " + (long) real() + suffix));
		}

		@Override
		protected void applyValue() {
			onChange.accept(real());
		}
	}
}
