package bioast.mods.gt6scan;

import net.minecraft.block.material.MapColor;

import java.awt.*;

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

	private static final double[][] mapColors = new double[][]{
			ColorUtility.getLab(new Color(airColor))
			, ColorUtility.getLab(new Color(grassColor))
			, ColorUtility.getLab(new Color(sandColor))
			, ColorUtility.getLab(new Color(clothColor))
			, ColorUtility.getLab(new Color(tntColor))
			, ColorUtility.getLab(new Color(iceColor))
			, ColorUtility.getLab(new Color(ironColor))
			, ColorUtility.getLab(new Color(foliageColor))
			, ColorUtility.getLab(new Color(snowColor))
			, ColorUtility.getLab(new Color(clayColor))
			, ColorUtility.getLab(new Color(dirtColor))
			, ColorUtility.getLab(new Color(stoneColor))
			, ColorUtility.getLab(new Color(waterColor))
			, ColorUtility.getLab(new Color(woodColor))
			, ColorUtility.getLab(new Color(quartzColor))
			, ColorUtility.getLab(new Color(adobeColor))
			, ColorUtility.getLab(new Color(magentaColor))
			, ColorUtility.getLab(new Color(lightBlueColor))
			, ColorUtility.getLab(new Color(yellowColor))
			, ColorUtility.getLab(new Color(limeColor))
			, ColorUtility.getLab(new Color(pinkColor))
			, ColorUtility.getLab(new Color(grayColor))
			, ColorUtility.getLab(new Color(silverColor))
			, ColorUtility.getLab(new Color(cyanColor))
			, ColorUtility.getLab(new Color(purpleColor))
			, ColorUtility.getLab(new Color(blueColor))
			, ColorUtility.getLab(new Color(brownColor))
			, ColorUtility.getLab(new Color(greenColor))
			, ColorUtility.getLab(new Color(redColor))
			, ColorUtility.getLab(new Color(blackColor))
			, ColorUtility.getLab(new Color(goldColor))
			, ColorUtility.getLab(new Color(diamondColor))
			, ColorUtility.getLab(new Color(lapisColor))
			, ColorUtility.getLab(new Color(emeraldColor))
			, ColorUtility.getLab(new Color(obsidianColor))
			, ColorUtility.getLab(new Color(netherrackColor))
	};
	public static int[] cols = new int[]{
			airColor
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

	public static MapColor asMinecraftMapColor(int color) {
		Color ob = new Color(color);
		double[] labOB = ColorUtility.getLab(ob);
		int bestColorIndex = 0;
		double closestDistance = Double.MAX_VALUE;
		for (int i = 0; i < ColorMap.mapColors.length; i++) {
			double[] c = mapColors[i];
			double diffLinner = Math.abs(c[0] - labOB[0]);
			double diffAinner = Math.abs(c[1] - labOB[1]);
			double diffBinner = Math.abs(c[2] - labOB[2]);
			double distance = diffLinner * diffLinner + diffAinner * diffAinner + diffBinner * diffBinner;
			if (distance < closestDistance) {
				closestDistance = distance;
				bestColorIndex = i;
			}
		}
		return MapColor.mapColorArray[bestColorIndex];
	}
}
