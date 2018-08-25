import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import com.sun.media.jai.codec.FileSeekableStream;

public class SplitImage {
	public static final int SPLIT_HORIZONT = 1;
	public static final int SPLIT_VERTICAL = 2;

	private static final int PIC_FIRST = 3;
	private static final int PIC_SECOND = 4;

	private File fileToSplit;
	private int splitBehavior;
	private long whereToSplit;
	private boolean percent;

	public SplitImage(File pFileToSplit, int pSplitBehavior,
			long pWhereToSplit, boolean pPercent) throws Exception {
		if (!pFileToSplit.exists()) {
			throw new FileNotFoundException();
		}
		fileToSplit = pFileToSplit;
		splitBehavior = pSplitBehavior;
		whereToSplit = pWhereToSplit;
		percent = pPercent;
		if (pWhereToSplit < 1) {
			throw new Exception(
					"Value of splitpoint is greater lower than 1, please adjust splitpoint value.");
		}

		if (pPercent && (pWhereToSplit > 100)) {
			throw new Exception(
					"Value of splitpoint is greater than 100, please adjust splitpoint value or set percent to false.");
		}
	}

	public void split(int pCounter) throws Exception {
		BufferedImage img = null;
		String lcSplittedPreFix = fileToSplit.getParent() + "/" + "splitted/"
				+pCounter;
//				+ fileToSplit.getName();
		File lcSplittedFileA = new File(lcSplittedPreFix + "_2.jpg");
		File lcSplittedFileB = new File(lcSplittedPreFix + "_1.jpg");

		// img = ImageIO.read(fileToSplit);
		img = readPicFromTiff();

		// Splitted A
		{
			Dimension lcDem = getDimensionForSplitPic(img, PIC_FIRST);

//			System.err.println("dem ; X:" + lcDem.width + " Y:" + lcDem.height);

			BufferedImage lcResultImage = new BufferedImage(lcDem.width,
					lcDem.height, BufferedImage.TYPE_INT_RGB);

			Graphics lcGraphics = lcResultImage.getGraphics();

			if (getSplitBehavior() == SPLIT_HORIZONT) {
				lcGraphics.drawImage(img, 0, 0, lcDem.width, lcDem.height, 0,
						0, img.getWidth(), lcDem.height, null);
			} else if (getSplitBehavior() == SPLIT_VERTICAL) {
				lcGraphics.drawImage(img, 0, 0, lcDem.width, lcDem.height, 0,
						0, lcDem.width, img.getHeight(), null);
			}
			ImageIO.write(lcResultImage, "jpg", lcSplittedFileA);
		}

		// Splitted B
		{
			Dimension lcDem = getDimensionForSplitPic(img, PIC_SECOND);

//			System.err.println("dem ; X:" + lcDem.width + " Y:" + lcDem.height);

			BufferedImage lcResultImage = new BufferedImage(lcDem.width,
					lcDem.height, BufferedImage.TYPE_INT_RGB);

			Graphics lcGraphics = lcResultImage.getGraphics();

			if (getSplitBehavior() == SPLIT_HORIZONT) {

				lcGraphics.drawImage(img, 0, 0, lcDem.width, lcDem.height, 0,
						img.getHeight() - lcDem.height, img.getWidth(),
						img.getHeight(), null);
			} else if (getSplitBehavior() == SPLIT_VERTICAL) {
				lcGraphics.drawImage(img, 0, 0, lcDem.width, lcDem.height,
						img.getWidth() - lcDem.width, 0, img.getWidth(),
						img.getHeight(), null);
			}
			

			ImageIO.write(lcResultImage, "jpg", lcSplittedFileB);
			
			System.err.println("counter:" + pCounter);
		}
	}
	
	private void writeFileAsPNG(BufferedImage pImg, File pFile) {
		
	}

	private Dimension getDimensionForSplitPic(BufferedImage pImg,
			int pPicWhichOne) throws Exception {
		int lcParentImgWidth = pImg.getWidth();
		int lcParentImgHeight = pImg.getHeight();

		if (isPercent()) {

			int lcPercentVal = 0;

			if (getSplitBehavior() == SPLIT_HORIZONT) {
				lcPercentVal = (int) (lcParentImgHeight * (getWhereToSplit() / 100D));
			} else if (getSplitBehavior() == SPLIT_VERTICAL) {
				lcPercentVal = (int) (lcParentImgWidth * (getWhereToSplit() / 100D));
			}

			if (pPicWhichOne == PIC_FIRST) {
				if (getSplitBehavior() == SPLIT_HORIZONT) {
					return new Dimension(lcParentImgWidth, lcPercentVal);
				} else if (getSplitBehavior() == SPLIT_VERTICAL) {
					return new Dimension(lcPercentVal, lcParentImgHeight);
				}
			} else if (pPicWhichOne == PIC_SECOND) {
				if (getSplitBehavior() == SPLIT_HORIZONT) {
					return new Dimension(lcParentImgWidth,
							(lcParentImgHeight - lcPercentVal));
				} else if (getSplitBehavior() == SPLIT_VERTICAL) {
					return new Dimension((lcParentImgWidth - lcPercentVal),
							lcParentImgHeight);
				}
			} else {
				throw new Exception("Wrong parameter for pPicWhichOne.");
			}
		} else {
			if (pPicWhichOne == PIC_FIRST) {
				if (getSplitBehavior() == SPLIT_HORIZONT) {
					return new Dimension(lcParentImgWidth, new Long(
							getWhereToSplit()).intValue());
				} else if (getSplitBehavior() == SPLIT_VERTICAL) {
					return new Dimension(
							new Long(getWhereToSplit()).intValue(),
							lcParentImgHeight);
				}
			} else if (pPicWhichOne == PIC_SECOND) {
				if (getSplitBehavior() == SPLIT_HORIZONT) {
					return new Dimension(lcParentImgWidth,
							lcParentImgHeight
									- (lcParentImgHeight - new Long(
											getWhereToSplit()).intValue()));
				} else if (getSplitBehavior() == SPLIT_VERTICAL) {
					return new Dimension(
							lcParentImgWidth
									- (lcParentImgWidth - new Long(
											getWhereToSplit()).intValue()),
							lcParentImgHeight);
				}
			} else {
				throw new Exception("Wrong parameter for pPicWhichOne.");
			}
		}
		return new Dimension();
	}

	public BufferedImage readPicFromTiff() {

		FileSeekableStream image = null;
		try {
			image = new FileSeekableStream(fileToSplit);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}

		ParameterBlock pb = new ParameterBlock();
		pb.add(image);

		RenderedOp image1 = JAI.create("tiff", pb);

		return image1.getAsBufferedImage();
	}

	public File getFileToSplit() {
		return fileToSplit;
	}

	public void setFileToSplit(File fileToSplit) {
		this.fileToSplit = fileToSplit;
	}

	public int getSplitBehavior() {
		return splitBehavior;
	}

	public void setSplitBehavior(int splitBehavior) {
		this.splitBehavior = splitBehavior;
	}

	public long getWhereToSplit() {
		return whereToSplit;
	}

	public void setWhereToSplit(long whereToSplit) {
		this.whereToSplit = whereToSplit;
	}

	public boolean isPercent() {
		return percent;
	}

	public void setPercent(boolean percent) {
		this.percent = percent;
	}
}
