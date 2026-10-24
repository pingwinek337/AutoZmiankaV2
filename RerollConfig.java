package pl.autoreroll;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RerollConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("autoreroll.json");

	/** Zakres zatrzymania w pelnych procentach. */
	public int min = 47;
	public int max = 50;
	/** Odstep miedzy kliknieciami w tickach (20 = 1 sekunda). */
	public int delayTicks = 6;
	/** Maksymalna liczba rolli, 0 = bez limitu. */
	public int maxRolls = 0;

	/** Slot miecza: swordPlayer = true -> numer w Twoim ekwipunku, false -> numer slotu w oknie serwera. */
	public boolean swordPlayer = false;
	public int swordSlot = -1;
	public String swordName = "";

	/** Slot zmianki, jak wyzej. */
	public boolean scrollPlayer = false;
	public int scrollSlot = -1;
	public String scrollName = "";

	/** true = zmianke bierzemy prawym przyciskiem myszy, false = lewym. */
	public boolean rightClickPickup = true;

	/** Regex wyciagajacy procent z nazwy i opisu miecza. Grupa 1 = liczba. */
	public String percentRegex = "obra.enia\\s*(\\d+(?:[.,]\\d+)?)\\s*%";

	public static RerollConfig load() {
		try {
			if (Files.exists(PATH)) {
				RerollConfig c = GSON.fromJson(Files.readString(PATH), RerollConfig.class);
				if (c != null) return c;
			}
		} catch (Exception e) {
			System.err.println("[AutoReroll] Nie udalo sie wczytac configu: " + e);
		}
		RerollConfig c = new RerollConfig();
		c.save();
		return c;
	}

	public void save() {
		try {
			Files.writeString(PATH, GSON.toJson(this));
		} catch (IOException e) {
			System.err.println("[AutoReroll] Nie udalo sie zapisac configu: " + e);
		}
	}
}
