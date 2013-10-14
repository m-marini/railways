package org.mmarini.railways.model.graphics;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.net.URL;

import javax.swing.ImageIcon;

/**
 * @author $Author: marco $
 * @version $Id: ImagePainter.java,v 1.6 2012/02/08 22:03:31 marco Exp $
 */
public class ImagePainter implements Painter {
	private AffineTransform transform;
	private Image image;

	/**
	 * @param name
	 * @param x
	 *            pixels
	 * @param y
	 *            pixels
	 * @param scale
	 *            scale
	 */
	public ImagePainter(String name, double x, double y, double scale) {
		transform = AffineTransform.getScaleInstance(1 / scale, 1 / scale);
		transform.translate(x, y);
		URL url = getClass().getResource(name);
		if (url != null) {
			image = new ImageIcon(url).getImage();
		}
	}

	/**
	 * @return Returns the image.
	 */
	protected Image getImage() {
		return image;
	}

	/**
	 * @return Returns the transform.
	 */
	protected AffineTransform getTransform() {
		return transform;
	}

	/**
	 * @see org.mmarini.railways.model.graphics.Painter#paint(org.mmarini.railways.model.graphics.GraphicsContext)
	 */
	@Override
	public void paint(GraphicsContext ctx) {
		if (image == null)
			return;
		Graphics2D gr = ctx.getGraphics();
		gr.drawImage(image, transform, null);
	}
}