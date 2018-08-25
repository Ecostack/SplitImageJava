import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;

public class SplitImageMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		int counter = 1;

		for (File lcFile : new File("C:/shared/russisch_mit_system/").listFiles(getFileFilter())) {
			try {
			new SplitImage(lcFile,
					SplitImage.SPLIT_HORIZONT, 50, true).split(counter++);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private static FileFilter getFileFilter() {
		return new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				if (pathname.isDirectory()) {
					return false;
				}
				return true;
			}
		};
	}
}
