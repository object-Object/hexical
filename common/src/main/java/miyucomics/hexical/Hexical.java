package miyucomics.hexical;

import miyucomics.hexical.registry.HexicalBlocks;
import miyucomics.hexical.registry.HexicalItems;
import miyucomics.hexical.registry.HexicalNetworking;
import miyucomics.hexical.registry.HexicalPatterns;
import net.minecraft.util.Identifier;

public class Hexical {
	public static final String MOD_ID = "hexical";
	public static final Identifier CAST_CONJURED_STAFF_PACKET = new Identifier(MOD_ID, "cast_conjured_staff");

	public static void init() {
		HexicalAbstractions.initPlatformSpecific();
		HexicalBlocks.init();
		HexicalItems.init();
		HexicalPatterns.init();
		HexicalNetworking.init();
	}

	public static Identifier id(String string) {
		return new Identifier(MOD_ID, string);
	}
}