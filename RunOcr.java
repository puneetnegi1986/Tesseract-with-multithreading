import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;

import net.sourceforge.tess4j.Tesseract;

public class RunOcr {

	public static void main(String[] args) {
		
		Tesseract tessInst = new Tesseract();
		long startTime = System.currentTimeMillis();
		File file = new File("F:\\tesseract\\output\\data.txt");
		List<Callable<String>> callalbeList= new ArrayList<>();
		ExecutorService executor = Executors.newFixedThreadPool(12);
		List<File> documents;
		StringBuffer result=new StringBuffer();
		int counter = 0;
		Long timeStart = System.currentTimeMillis();
		try {
			documents = loadPDF();
			for (File document : documents) {
				callalbeList.add(new OcrCallable(new AtomicInteger(counter), documents));
				counter++;
			}
			
			List<Future<String>> futures = executor.invokeAll(callalbeList);
			for(Future<String> futureObj:futures) {
				
					result.append(futureObj.get());
			}
			executor.shutdown();
			file.createNewFile();
			Files.write(Paths.get(file.toURI()), result.toString().getBytes("utf-8"), StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
			System.out.println("Total time.."+(System.currentTimeMillis()-timeStart));
		} catch (Exception e1) {

			e1.printStackTrace();
		}

		long endTime = System.currentTimeMillis();

		System.out.println("Total Time: " + (endTime - startTime));
	}

	private static String cleanTextContent(String rawData) {

		rawData = rawData.replaceAll("[^\\x00-\\x7F]", "");

		rawData = rawData.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");

		rawData = rawData.replaceAll("[-+|@_^.:,~]", "");

		return rawData;
	}

	private static List<File> loadPDF() throws Exception {

		List<PDDocument> documents = new ArrayList<>();
		List<File> files = new ArrayList<>();
		try (PDDocument document = PDDocument.load(new File("F:\\tesseract\\SKD 2016 1065.pdf"))) {
			int totalpage = document.getPages().getCount();

			Splitter splitter = new Splitter();
			
			if (totalpage > 10) {
				int counter = totalpage / 10;
				for (int i = 0; i < 10; i++) {

					splitter.setSplitAtPage(totalpage / counter);
					documents = splitter.split(document);
				}
			}
			
			int index=0;
			for(PDDocument document1:documents) {
				File file = new File("F:\\ocr\\ocrFile"+index +".pdf");
				file.createNewFile();
				document1.save(file);
				files.add(file);
				index++;
			}
		}
		
		return files;
	}
}

class OcrCallable implements Callable<String> {

	private AtomicInteger index;
	private List<File> documents;

	OcrCallable(AtomicInteger index, List<File> documents) {
		this.index = index;
		this.documents = documents;
	}

	@Override
	public String call() throws Exception {
		String rawData=null;
		try {
			
			Tesseract tessInst = new Tesseract();
			String result = tessInst.doOCR(documents.get(index.get()));
			
			rawData = result.replaceAll("[^\\x00-\\x7F]", "");

			rawData = rawData.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");

			rawData = rawData.replaceAll("[-+|@_^.:,~]", "");

		} catch (Exception e) {
     
		}
		return rawData;
	}

}
