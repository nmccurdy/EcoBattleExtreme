package torusworld;

/**
 * A Class that implements this will keep a list of ImageReceivers that will 
 * then be given images according to the ImageProducer's own schedule.
 * 
 *  The Publisher side of a publish/subscribe pattern
 *
 */
public interface ImageProducer {
    /**
     * Produces a snapshot of spaceland which will be sent to the given ir
     * note the ir should remove itself from the screenshot receiver list when it receives a new image
     * otherwise it will cause a screenshot to occur for each frame.
     */
    public void addImageReceiver(ImageReceiver ir);    
 
    /**
     * causes ImageReceiver ir to be removed from the screebshot subscriber list
     * @param ir
     */
    
    public void removeImageReceiver(ImageReceiver ir);
}
