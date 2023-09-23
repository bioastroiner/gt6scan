package bioast.mods.gt6scan;

import gregapi.data.MT;
import gregapi.util.UT;
import net.minecraft.block.material.MapColor;

public class ColorMap {
	public static final int airColor = 0;
	public static final int grassColor = 8368696;
	public static final int sandColor = 16247203;
	public static final int clothColor = 10987431;
	public static final int tntColor = 16711680;
	public static final int iceColor = 10526975;
	public static final int ironColor = 10987431;
	public static final int foliageColor = 31744;
	public static final int snowColor = 16777215;
	public static final int clayColor = 10791096;
	public static final int dirtColor = 12020271;
	public static final int stoneColor = 7368816;
	public static final int waterColor = 4210943;
	public static final int woodColor = 6837042;
	public static final int quartzColor = 16776437;
	public static final int adobeColor = 14188339;
	public static final int magentaColor = 11685080;
	public static final int lightBlueColor = 6724056;
	public static final int yellowColor = 15066419;
	public static final int limeColor = 8375321;
	public static final int pinkColor = 15892389;
	public static final int grayColor = 5000268;
	public static final int silverColor = 10066329;
	public static final int cyanColor = 5013401;
	public static final int purpleColor = 8339378;
	public static final int blueColor = 3361970;
	public static final int brownColor = 6704179;
	public static final int greenColor = 6717235;
	public static final int redColor = 10040115;
	public static final int blackColor = 1644825;
	public static final int goldColor = 16445005;
	public static final int diamondColor = 6085589;
	public static final int lapisColor = 4882687;
	public static final int emeraldColor = 55610;
	public static final int obsidianColor = 1381407;
	public static final int netherrackColor = 7340544;

	public static int[] cols = new int[]{airColor
			, grassColor
			, sandColor
			, clothColor
			, tntColor
			, iceColor
			, ironColor
			, foliageColor
			, snowColor
			, clayColor
			, dirtColor
			, stoneColor
			, waterColor
			, woodColor
			, quartzColor
			, adobeColor
			, magentaColor
			, lightBlueColor
			, yellowColor
			, limeColor
			, pinkColor
			, grayColor
			, silverColor
			, cyanColor
			, purpleColor
			, blueColor
			, brownColor
			, greenColor
			, redColor
			, blackColor
			, goldColor
			, diamondColor
			, lapisColor
			, emeraldColor
			, obsidianColor
			, netherrackColor};

	public static int findClosestColorTo(final int color) {
		int closestColor = -1;
		var closestDistance = Double.POSITIVE_INFINITY;
		for (final int colorTarget : ColorMap.cols) {
			if (colorTarget == ColorMap.magentaColor && (color == UT.Code.getRGBInt(MT.Lapis.mRGBaSolid) || color == UT.Code.getRGBInt(MT.BlueSapphire.mRGBaSolid)))
				continue;
			var h0 = getHue(colorTarget);
			var h1 = getHue(color);
			var v0 = getValue(colorTarget);
			var v1 = getValue(color);
			var s0 = getSaturation(colorTarget);
			var s1 = getSaturation(color);
			var dh = Math.min(Math.abs(h1 - h0), 360 - Math.abs(h1 - h0)) / 180.0;
			var ds = Math.abs(s1 - s0);
			var dv = Math.abs(v1 - v0) / 255.0;
			final var distance = Math.sqrt(dh * dh + ds * ds + dv * dv);
			if (distance < closestDistance) {
				closestDistance = distance;
				closestColor = colorTarget;
			}
		}
		return closestColor;
	}

	public static int getHue(int argb) {
		float r = UT.Code.getR(argb), g = UT.Code.getG(argb), b = UT.Code.getB(argb);
		if (r == g && r == b) return 0;
		float min = Math.min(r, Math.min(g, b));
		if (r >= g && r >= b) {
			return (int) (((g - b) / (r - min)) % 6) * 60;
		}
		if (g >= r && g >= b) {
			return (int) (((b - r) / (g - min)) + 2) * 60;
		}
		if (b >= r && b >= g) {
			return (int) (((r - g) / (b - min)) + 4) * 60;
		}
		return 0;
	}


	public static float getSaturation(int argb) {
		float r = UT.Code.getR(argb), g = UT.Code.getG(argb), b = UT.Code.getB(argb);
		float min = Math.min(r, Math.min(g, b));
		float max = Math.max(r, Math.max(g, b));
		return max == 0 ? 0 : (max - min) / max;
	}

	public static float getValue(int argb) {
		float r = UT.Code.getR(argb), g = UT.Code.getG(argb), b = UT.Code.getB(argb);
		return Math.max(r, Math.max(g, b));
	}

	public static MapColor asMinecraftMapColor(int color) {
		color = findClosestColorTo(color);
		for (int i = 0; i < cols.length; i++) {
			if (color == cols[i]) {
				if (MapColor.mapColorArray.length > i)
					return MapColor.mapColorArray[i];
			}
		}
		return MapColor.airColor;
	}
}
