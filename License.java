package pl.autoreroll;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;

/** Sprawdzanie klucza licencyjnego. Przechowuje tylko skroty SHA-256 kluczy. */
public final class License {
	private static final List<String> HASHES = Arrays.asList(
		"50d43273fb29a7484432e025670e9d650cbb3066c6be09d02a3dcca25f9be2e8",
		"7f029ceebc60288bd7b61877a4cfa294f2a433cda8f5e47abb77c90d05e684a9",
		"89dd86b9f854d4db9004007289cfc16556a4c0b4eeac5c02ee38afab6537f717",
		"2c79d2072cdee1808014009a7a83a2264876bafc0878338c40e42dea0b47149b",
		"8f2392d7b3375fbab6cf3fa544a0de35ad2726a1ba7c563570ffba0ce2047747",
		"d97dc08bcf9b05adf94b14e2314029d3bde226012f618281cd149779bd9a361d",
		"9e1c6401cd5eb8da40ccc82039d4ee6a4348c580bca6aa5fd5956756f9a7c844",
		"09a622aaf7449de27cccb39e76f3e742ac89e92c10e1f621c7261ae4fc37dbfe",
		"d24a3e5e929d9a3292d9e7b8b91cd6e2882449602b22807f1bb69aa37d69bd24",
		"b339c2abfc6d47671a3445192f28532f50d05db75b44a9f0682a98636d7e66c2",
		"f7f4df6ed1fa4b58b89c9c57bc66be108d8e1bae5216aaff4dfd81fff080f685",
		"20ccf73d9bd7234869808bfef3ca55600211731f3f8200d8ecc535825486bc46",
		"6628ab11111e3b3af28eb61e525afb230de80310e3849176905ba277251c3a4e",
		"4ac9a0703e1d946f8ea3c858e533d19f91bd08a81ffd93d77b781fb3dfaaba85",
		"ed718a1b646f3ff82df72275fb3925581fbcee7d20c404adaf12056db2dcb185",
		"482580579868c78f8547149cd60fa6a6a18154b96e1f50d94e378beb7671e17b",
		"e987ad6323677dcbbbae99df6cbc19d9d203ed22a0c08d414ea1c0916d5fed77",
		"f5a3d43f7c79a431d41bba65b890bb8adf9d7664e6497758e595c2a94fcaca6f",
		"a203a7b155a800c310b4becfc6ebe06ffea8c3b19c19f17d6d542c900f277436",
		"d7ce00e43a9843969c63eb45f0975b6424892689c27bd3d2fa0729db02847231",
		"444b31474dce84f1204e1211c8bda8eebbbf3841aeac10036674079f4ac34c49",
		"b5c48300239908f4a17308a1b58ac0ac006948deb5c74c11d1029624c42f67a6",
		"124a795dd5eef98d18ea08f7bb81b2e83adae9783067dd55060681edaf86edd8",
		"17d8943ace424dc85761aab08996c874d2467a62019446a4ad769079836784cc",
		"3b4ec8320b71009300b4839eb65fd7372c897643bbac3de4d1daeb0d98b1becc",
		"d4861b7c68659863fc62098f158c89637503eb73ef311e822b3f6689ddb72d0f",
		"498fc2f696395202a34f6151ebddb60fd570abc9999db8936b82634d57e247d0",
		"c38e97eb2cd901f36b9212d169ea1ec4b0054cbdf9732a1c742b0aa4d24dadd2",
		"3947cb97fd43da68f8bc4e31869bd3448759c9986b4bc9803777cb4e3658d4c8",
		"4a94628a0fb9d2cbe9bccd9134ad3358525f89c1c4eae271f64cdf8af9264c40",
		"af0b9cf1cb3e9fd09aecf3d5bf056c107b31d50626d87046df8e741781c4ddda",
		"f150581832cace1a056f329d8d40ecc808b2dc1972b420f3c4acc0bd0b20be38",
		"19750c3058ae2c102b685a49bb1fe406151b5327a8ec21933ad61351bbc40338",
		"887b1877fe46e66c8dfd7c49399a156016eecf5ee71da3b498eb9498f52e8984",
		"d353053aec3d41a57c4a6ab53e88ae8e5fe2147d8e10bace797ec3b93393d241",
		"8269f3598f4c11547cb1d302914ef3901c3c26b6115f56d377d1fe453d020a16",
		"2480d2e75f9a172747c21c4e110fbbda1c60636bdce2ed2c13f999cfa0baa5fa",
		"bf7f1e24da2c96b8d15210b2ede70b47fcaf712c71476a820dd96e3439c8d64d",
		"246a566d2f3846edcf19045b46bb6316aa8b195111419b342d3067dc8ec91df6",
		"b8b772efb01e5620ba93e31f1d41523f513d332e4fde62ab71f52d0058a023eb",
		"beabc964bf1bd0ee61c7b01c4c1f73114e469738ec36699d98fb38f3e17477f6",
		"1b771c770c172e86d8108c2a2a0156ea495b6984770debf8264a699bbc69a70c",
		"26ec387305acfbb0f0599e3d2c23521d6d4e89bd856644e50c7b919b588d05c6",
		"5544e5af441f834b06bbbe30810df07bcbcace28c1bf7a73808769d8d3edf3e5",
		"30700f3dc13b4ae65074b23e80cfa3fceabbedb656aa3fcd7d268d5d36cf901c",
		"51ee39d2e2e03a21a2aa70b6c05f17075110c344bd4834e24faf405969025f82",
		"34c79e155dcfb2bd0722868bfa5b5d30434217e0caac976e66bc636975518f82",
		"4a57b92e006c546afc96d3e63eb739131e2cc375a753e832b43347a9c448f054",
		"5b4ed562e72c4b003e713aea1742b79ebc2432accfea67488aa4799c5a57babe",
		"3de6411668d3f534db623068f676b77424be65e1a49e364dbb8c1a72db85223f",
		"e8e464056384f2714ac09b81e5e4721936a1f18c271dd83895214e1834942cd4",
		"c8e8764273f3f5ee3fe2502b6a75f78fa044cbdebe68bed2b62b8d28e0b482df",
		"0ae98b1f12050d1dafd8ae56dc75ea0029a7cd81173ddeb5e276fa195b40f525",
		"52047521a5c61438805480acc01b71929f0a5a3fb5365f3616597b0a71272666",
		"29d54027fb442738e4a31d495a8a41a4c894eacb10ee87f27de81aa68f313e69",
		"21a42d7b856ce4a1d4a358d4f61a41cffaa01744e7e207176de3307509c8391f",
		"0acec106910f8b847d9e492599083bf24c0f01d8a94ed966222ad347ea6b60ce",
		"2e8a68dd1d3b71a054e71e8f6a9d3cf0c39f79d928d308437b3aa072ff3c1ee4",
		"d88a4676820db62ddf31ffadb0445448a8f4f4faabe961bf8f9e5f0a968d6314",
		"b8bd49080a916a0391f1443f1d6ef6731a66f4f2ba152a6e6ab7d681a52554a0",
		"16c73b70053aa19c46b24fa3d1ced8b38cd27e03b4d788efdecfd31583fed6df",
		"8c2fe1bb392361c4511f40e58b9c006749421d0170b9dda5da123f8831f8fdbe",
		"c4cd9d61f15be81244b7bcf10b9a3876678448a1ff117748db75a1589d03211e",
		"bd51e4aff32dc5845b43c4dc534b247a6410881c9b3fac9b9f0451f65ba31fb4",
		"687690dd0ce28fed23e9864eec9b7ede97cbd231da8971904bcd0df71d39785e",
		"e3d9f5cc11ed4b0c317d65ec3432a695146bb1f6908060e7becbe2ed785d009c",
		"b81b85499f7cb06b9885608d237a08910ed48304ac1670aade6884ad77a3037d",
		"6271e3c8f93d6aa22df89bea120226bca99300e4af4f02660dc5f3d6e4422b3b",
		"f091c4c8810610e91635f62f621003667e806e95ed77e121a8d8b900fc486f07",
		"4524a0c4ddddb1ccdc1fcca8b8224e5aa1e8f5fcdcf40443aec9a1477591eb72",
		"c2cf46e3847dead54a7239b649c86e898649bc410b8326b67e54dde17672fc75",
		"0dd1463ffe323ba7d1c22e268d770193c38f30e3f9c5a25807c9beb40836fd9b",
		"ae35201c7059680ff047db1b3fc3edc569ffcabde42714d31ccda96329891007",
		"39d445a946dad16628e65a519c9985fbe933f5307bd2d5a7414f3c962a4620a4",
		"8b14f794f23712222e05989be7a8ae2f9977212646602885ff6a9da112829798",
		"5ea825ef2f6f007b5cb0344f40d6ce4a4216e7dd1bec053f2def0130f0a37046",
		"cce4df74f96957b3e15fe56fce6080ac7da2191a002395d20f0e18687c034c1f",
		"5eaafad33daef0bdba1f79d6ab0c59fb74b8a2caa1e5feadf29ddb7a5501107e",
		"03a9631b41123aae7a6ac39e57de0294d5e96448a95c607b1f760ff1dfa79d6a",
		"b4ba060761982f6f0683acdf86730fa537713b4b6374cd2698474686a677c469",
		"a0eef4eab03613821d9dfef91a6d5b109f05716b2cd5772859271cb72868e70d",
		"568f72865ce594f85e5cd191d8111c5f11195168276a971950135a6b60ae04a6",
		"18668c148b7d5d23a898a7994dba47523cd83b62f8b86ad0b775382068351824",
		"6722295800e39134c185d9f97bef3b3f1986374bbf58b934d1ba846a0344c7fd",
		"b21ce7a602dea99adfebb49f11565a7b2bacdea71ace596c2708b24082e467c5",
		"47d730cca54c3fa0736e2cc3471f4774a750d8107ff770465ed6a86bc4b72552",
		"0da2a93b52cbadef9f4ef7499110d929e0f9631257164dc56bba4f9e98b8bf7e",
		"a03cc9bd59fbadc78c05c72170c745c6a085f8a68026828bf7f2ef064d5b89e7",
		"5da549cbca6115d38d563e4a3c91400d2ba0eed66179c52956cb3f64ffd76960",
		"144f9b08cc842c8ae05afea86058a1a7392147dac9d999a3e7f86d14202ae387",
		"fa817d7d13c2bf9d318e5d7de73d3e3d7256de4d5a796cef2b04cbdb9a24c7f0",
		"5b8b117be759e24766c49b89604cdf8c2b291fd0185e2d4fbf59406811fc4d03",
		"b38b72a0dca160bc4326820ecb022cbf37b06536771dcc91945712b6f8934ceb",
		"68d96ff4ecba836a476b5d4517a2af98f9e888305248e115eec6935c5d729e5f",
		"042f1a113a2b0496b93bd5fa4752623aa6d34d4dfe0559fb24063cac999c78ce",
		"62c02a1cba087ccffc7dc7038ca460e5533411f2da9464e911ebb236dc4314e5",
		"31883ed126854b4ecca88cd0b399627ab2b39fff0a2c530a127cf4bd627c4d66",
		"a563450d100c5f634cfa9ecab114f86fb329ba0e210a2594e8ad1d8341a45eb5",
		"74218a35cef570cb7e80b1f496e3944679e44cd2bfa6363fa4b6b6fa30d999fb",
		"edb3ae5ab0d1457727677a43e2b0b0c0d5fbf11ef3895e0d459722b8dfe77c41"
	);

	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("autoreroll.license");
	private static boolean active = loadActive();

	private License() {
	}

	public static boolean isActive() {
		return active;
	}

	private static String normalize(String key) {
		return key == null ? "" : key.trim().toUpperCase().replace(" ", "");
	}

	private static String hash(String key) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] d = md.digest(("autoreroll-v1:" + key).getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			for (byte b : d) sb.append(String.format("%02x", b));
			return sb.toString();
		} catch (Exception e) {
			return "";
		}
	}

	private static boolean loadActive() {
		try {
			if (Files.exists(FILE)) {
				return HASHES.contains(Files.readString(FILE).trim());
			}
		} catch (Exception ignored) {
		}
		return false;
	}

	/** Zwraca true i zapisuje aktywacje, jesli klucz jest poprawny. */
	public static boolean activate(String key) {
		String h = hash(normalize(key));
		if (!HASHES.contains(h)) return false;
		try {
			Files.writeString(FILE, h);
		} catch (Exception ignored) {
		}
		active = true;
		return true;
	}

	/** Okno wpisywania klucza. */
	public static class PromptScreen extends Screen {
		private static final int W = 260;
		private static final int H = 150;
		private TextFieldWidget field;
		private String status = "";
		private int statusColor = 0xFFA8A8C0;
		private int px, py;

		public PromptScreen() {
			super(Text.literal("AutoReroll - licencja"));
		}

		@Override
		protected void init() {
			px = (width - W) / 2;
			py = Math.max(4, (height - H) / 2);

			field = new TextFieldWidget(textRenderer, px + 14, py + 62, W - 28, 20, Text.empty());
			field.setMaxLength(40);
			field.setPlaceholder(Text.literal("XXXX-XXXX-XXXX-XXXX"));
			addDrawableChild(field);
			setInitialFocus(field);

			addDrawableChild(ButtonWidget.builder(Text.literal("Aktywuj"), b -> tryActivate())
					.dimensions(px + 14, py + H - 34, 112, 22).build());
			addDrawableChild(ButtonWidget.builder(Text.literal("Anuluj"), b -> close())
					.dimensions(px + W - 14 - 112, py + H - 34, 112, 22).build());
		}

		private void tryActivate() {
			if (activate(field.getText())) {
				client.setScreen(new RerollConfigScreen(null));
			} else {
				status = "Niepoprawny klucz licencyjny.";
				statusColor = 0xFFFF6B6B;
			}
		}

		@Override
		public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
			if (keyCode == 257 || keyCode == 335) {
				tryActivate();
				return true;
			}
			return super.keyPressed(keyCode, scanCode, modifiers);
		}

		@Override
		public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
			super.renderBackground(ctx, mouseX, mouseY, delta);
			ctx.fill(px - 1, py - 1, px + W + 1, py + H + 1, 0xFF7C5CFF);
			ctx.fill(px, py, px + W, py + H, 0xF0101018);
			ctx.fill(px, py, px + W, py + 30, 0xFF1B1B2B);
			ctx.fill(px, py + 30, px + W, py + 31, 0xFF7C5CFF);
			ctx.drawTextWithShadow(textRenderer, "AutoReroll - licencja", px + 14, py + 11, 0xFFFFFFFF);
			ctx.drawTextWithShadow(textRenderer, "Wpisz klucz licencyjny:", px + 14, py + 46, 0xFFA8A8C0);
			ctx.drawTextWithShadow(textRenderer, status, px + 14, py + 90, statusColor);
		}

		@Override
		public void close() {
			client.setScreen(null);
		}
	}
}
