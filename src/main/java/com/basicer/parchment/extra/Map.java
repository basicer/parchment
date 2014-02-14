package com.basicer.parchment.extra;

import com.basicer.parchment.Context;
import com.basicer.parchment.OperationalTargetedCommand;
import com.basicer.parchment.annotations.Operation;
import com.basicer.parchment.parameters.*;
import net.minecraft.server.v1_7_R1.PacketPlayInArmAnimation;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.MemoryImageSource;
import java.io.IOException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;

/**
 * Created by basicer on 2/8/14.
 */
public class Map extends OperationalTargetedCommand<IntegerParameter> {

	public Parameter affect(IntegerParameter target, Context ctx) {
		return doaffect(IntegerParameter.from((int)target.asInteger(ctx)), ctx);
	}

	public Parameter affect(ItemParameter target, Context ctx) {
		ItemStack i = target.asItemStack(ctx);
		return doaffect(IntegerParameter.from((int) i.getDurability()), ctx);
	}

	@Override
	public FirstParameterTargetType getFirstParameterTargetType(Context ctx) {
		return FirstParameterTargetType.ExactMatch;
	}


	private MapView resolveMapView(short target) {
		return Bukkit.getMap(target);
	}

	public CanvasMapRenderer findRender(MapView v) {
		for ( MapRenderer r : v.getRenderers() ) {
			if ( r instanceof CanvasMapRenderer ) return (CanvasMapRenderer)r;
		}
		return null;
	}

	public Graphics getMapGraphics(short id) {
		MapView v = resolveMapView(id);
		Image image = null;
		CanvasMapRenderer r = findRender(v);
		if ( r == null ) {
			r = new CanvasMapRenderer();
			v.addRenderer(r);
		}
		Graphics g = r.getGraphics();
		if ( g == null ) fizzle("Couldn't get graphics.");
		return g;
	}

	public Long create(Context ctx) {
		MapView v = Bukkit.createMap(ctx.getWorld());
		for ( MapRenderer r : v.getRenderers() ) v.removeRenderer(r);
		return new Long(v.getId());

	}

	public Parameter writeOperation(Long target, Context ctx, IntegerParameter x, IntegerParameter y, StringParameter i) {
		Graphics g = getMapGraphics(target.shortValue());
		g.setColor(Color.pink);
		Font f = Font.decode("Lucida Sans Typewriter");
		System.out.println(f);
		g.setFont(f);
		g.drawString(i.asString(), x.asInteger(), y.asInteger());
		return Parameter.EmptyString;
	}

	public Parameter sendOperation(Long target, Context ctx, PlayerParameter who) {
		MapView v = resolveMapView(target.shortValue());
		System.out.println("Sending " + target);
		if ( who == null ) fizzle("Must specify player to send map to");
		who.asPlayer(ctx).sendMap(v);

		return IntegerParameter.from(target);
	}

	@Operation(desc = "Burst send entire map contents to all online players.")
	public Parameter sendAllOperation(Long target, Context ctx) {
		MapView v = resolveMapView(target.shortValue());
		System.out.println("Sending " + target);
		for ( Player p : Bukkit.getServer().getOnlinePlayers() ) {
			p.sendMap(v);
		}
		return IntegerParameter.from(target);
	}



	@Operation(aliases = {"img"})
	public Parameter imageOperation(Long target, Context ctx, StringParameter i, IntegerParameter ox, IntegerParameter oy) {
		Image image = null;

		try {
			URL url = new URL(i.asString(ctx));
			image = ImageIO.read(url);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Graphics g = getMapGraphics(target.shortValue());

		int iox = ox == null ? 0 : ox.asInteger();
		int ioy = oy == null ? 0 : oy.asInteger();
		g.drawImage(image, iox, ioy, null);
		return Parameter.from(target);
	}


	public class CanvasMapRenderer extends MapRenderer {
		private BufferedImage image;
		private Graphics graphics;

		public BufferedImage getImage() { return image; }
		public Graphics getGraphics() { dirty = true; return graphics;  }
		private byte[] data;
		private boolean dirty;

		public CanvasMapRenderer() {
			image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
			graphics = image.getGraphics();
			data = new byte[image.getWidth() * image.getHeight()];
			dirty = true;
		}

		@Override
		public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
			BufferedImage temp = getImage();

			if ( dirty ) {
				int[] pixels = new int[image.getWidth() * image.getHeight()];
				temp.getRGB(0, 0, temp.getWidth(), temp.getHeight(), pixels, 0, temp.getWidth());

				for (int i = 0; i < pixels.length; i++) {
					data[i] = org.bukkit.map.MapPalette.matchColor(new Color(pixels[i], true));
				}
				dirty = false;
			}

			for (int i = 0; i < data.length; i++) {
				byte px = data[i];
				if ( px != MapPalette.TRANSPARENT ) mapCanvas.setPixel(i % 128, i / 128, px);
			}



		}
	}
}

