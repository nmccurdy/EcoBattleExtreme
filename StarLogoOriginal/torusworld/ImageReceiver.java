package torusworld;

import java.awt.image.BufferedImage;

/**
 * A Class that implements this is can then register with an image producer to be sent images from the producer.
 * 
 * The Subscriber side of a publish/subscribe pattern
 *
 */
public interface ImageReceiver {
	public void receive(BufferedImage image, ImageProducer producer);
}
