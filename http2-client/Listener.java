package wu.test;

import java.io.IOException;
import java.net.URISyntaxException;

public interface Listener {
	
	public void produce_item_push() throws URISyntaxException, IOException, InterruptedException;
}
