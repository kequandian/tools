package com.jfeat.pdf.pages;

import com.itextpdf.text.*;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import com.jfeat.pdf.pages.util.StringUtils;

import com.jfeat.pdf.pages.util.ImageUtil;

import java.awt.*;
import java.awt.Image;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by vincent on 2018/5/9.
 */
public class PdfPages {

    public static void createPages(String newPdfFile, String... pdfFiles) throws IOException, DocumentException {
        // get last pdfFile to merge as the newPdfFile
        String lastPdfFile = pdfFiles[pdfFiles.length-1];

        String firstPdfFile = null;
        for(String url : pdfFiles) {
            if(firstPdfFile==null){
                firstPdfFile = url;
                continue;
            }

            String latestNewPdf = newPdfFile;
            if(!url.equals(lastPdfFile)){
                File tmp = File.createTempFile("ppg", ".tmp", new File("."));
                latestNewPdf = tmp.getCanonicalPath();
            }
            mergePdfPages(latestNewPdf, firstPdfFile, url);
            firstPdfFile = latestNewPdf;
        }
    }

    /**
     * create image pages only
     * @param pdfFilePath
     * @param imageUrls
     * @throws IOException
     * @throws DocumentException
     */
    public static void createImagesPages(String pdfFilePath, String... imageUrls) throws IOException, DocumentException {
        // create empty PdfReader
        Document document = new Document(PageSize.A4);
        PdfWriter pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(pdfFilePath));
        document.open();

        for (String url : imageUrls) {
            System.out.println(url);
            document.newPage();
            PdfContentByte canvas = pdfWriter.getDirectContent();
            addImage(canvas, url);
        }

        document.close();
    }

    /**
     * insert pages ahead the first page
     * @param pdfFilePath
     * @param fileUrls
     * @throws IOException
     * @throws DocumentException
     */
    public static void insertPage(String pdfFilePath, String... fileUrls) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(pdfFilePath);
        String newPdfFile = StringUtils.removeExtension(pdfFilePath) + "-new.pdf";

        addPage(reader, 1, newPdfFile, fileUrls);
    }

    /**
     * append the new pages to the end of the pdf file
     * @param pdfFilePath
     * @param fileUrls
     * @throws IOException
     * @throws DocumentException
     */
    public static void addPage(String pdfFilePath, String... fileUrls) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(pdfFilePath);
        String newPdfFile = StringUtils.removeExtension(pdfFilePath) + "-new.pdf";

        int newPages = reader.getNumberOfPages() + 1;
        addPage(reader, newPages, newPdfFile, fileUrls);
    }

    /**
     * add image page or merge pdf page
     * @param reader
     * @param pageNumber
     * @param newPdfFile  generate a new pdf file
     * @param fileUrls
     * @throws IOException
     * @throws DocumentException
     */
    private static void addPage(PdfReader reader, int pageNumber, String newPdfFile, String... fileUrls) throws IOException, DocumentException {
        Rectangle pageSize = reader.getPageSize(1);
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(newPdfFile));

        /// append image as new page for append all pages within pdf file
        for (String url : fileUrls) {
            stamper.insertPage(pageNumber, pageSize);

            if(url.endsWith(".pdf")){
                addPdfFilePages(reader, stamper, url);
            }else {
                PdfContentByte canvas = stamper.getOverContent(pageNumber);
                addImage(canvas, url);
            }
        }

        stamper.close();
        reader.close();
    }

    /**
     * 增加图片
     * @param canvas
     * @param imageUrl
     * @throws IOException
     * @throws DocumentException
     */
    private static void addImage(PdfContentByte canvas, String imageUrl) throws IOException, DocumentException {
        Rectangle pageSize = canvas.getPdfDocument().getPageSize();

        Image img = ImageUtil.getImage(imageUrl);
        com.itextpdf.text.Image image = com.itextpdf.text.Image.getInstance(img, Color.BLACK);
        image.setAlignment(Element.ALIGN_LEFT);
        image.scaleAbsolute(pageSize.getWidth(), pageSize.getHeight());
        image.setAbsolutePosition(0, 0);

        canvas.addImage(image);
    }


    /**
     * merge all pages for two pdf files
     * @param pdfFilePath
     * @param mergePdfFile
     */
    public static  void  mergePdfPages(String pdfFilePath, String mergePdfFile) throws IOException, DocumentException {
        String newPdfFile = StringUtils.removeExtension(pdfFilePath) + "-new.pdf";
        mergePdfPages(newPdfFile, pdfFilePath, mergePdfFile);
    }

    /**
     * merge two pdf files into a new pdf file
     * @param newPdfFile
     * @param pdfFilePath
     * @param mergePdfFile
     * @throws IOException
     * @throws DocumentException
     */
    public static  void  mergePdfPages(String newPdfFile, String pdfFilePath, String mergePdfFile) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(pdfFilePath);
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(newPdfFile));

        PdfReader mergeReader = new PdfReader(mergePdfFile);
        int mergePdfNumbers = mergeReader.getNumberOfPages();

        int newPages = reader.getNumberOfPages() + 1;

        for(int index=1; index<=mergePdfNumbers; index++) {
            Rectangle pageSize = mergeReader.getPageSize(index);
            int r = pageSize.getRotation();
            pageSize.setRotation(90);
            int r1= pageSize.getRotation();

            stamper.insertPage(newPages, pageSize);
            newPages++;

            PdfImportedPage mergePage = stamper.getImportedPage(mergeReader, index);
            PdfContentByte background = stamper.getOverContent(newPages);
            background.addTemplate(mergePage, 0, 0);
        }

        stamper.close();
        reader.close();

        // must be closed last
        mergeReader.close();
    }



    /**
     * 增加一个pdf文件
     * @param reader
     * @param mergePdfFile
     * @throws IOException
     * @throws DocumentException
     */
    public static  void  addPdfFilePages(PdfReader reader, PdfStamper writer, String mergePdfFile) throws IOException, DocumentException {
        PdfReader mergeReader = new PdfReader(mergePdfFile);

        for(int index=1; index<=mergeReader.getNumberOfPages(); index++) {
            int newPages = reader.getNumberOfPages() + 1;

            Rectangle pageSize = mergeReader.getPageSize(index);

            writer.insertPage(newPages, pageSize);
            PdfImportedPage mergePage = writer.getImportedPage(mergeReader, index);
            PdfContentByte background = writer.getOverContent(newPages);
            background.addTemplate(mergePage, 0, 0);
        }
        mergeReader.close();
    }

    /**
     * merge two pages from two file into one new pdf file
     * @param pdfFilePath
     * @param pageNumber
     * @param mergePdfFile
     * @param mergePageNumber
     * @throws IOException
     * @throws DocumentException
     */
    public static  void  mergePages(String pdfFilePath, int pageNumber, String mergePdfFile, int mergePageNumber)
            throws IOException, DocumentException {
        PdfReader reader = new PdfReader(pdfFilePath);
        PdfReader mergeReader = new PdfReader(mergePdfFile);
        String newPdfFile = StringUtils.removeExtension(pdfFilePath) + "-new.pdf";

        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(newPdfFile));

        // add single page
        PdfImportedPage mergePage = stamper.getImportedPage(mergeReader, mergePageNumber);
        PdfContentByte background = stamper.getOverContent(pageNumber);
        background.addTemplate(mergePage, 0, 0);

        stamper.close();
        reader.close();
        mergeReader.close();
    }

    public static void rotatePdf(String originalPdfFile, String outputPdfFile, int degrees) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(originalPdfFile);
        for (int i = 1; i <= reader.getNumberOfPages(); i++) {
            PdfDictionary dictionary = reader.getPageN(i);
            dictionary.put(PdfName.ROTATE, new PdfNumber(degrees));
        }
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(outputPdfFile));
        stamper.close();
        reader.close();
    }

    public static  void pickPage(String pdfFilePath, int[] pageNum) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(pdfFilePath);
        int numberOfPages = reader.getNumberOfPages();
        java.util.List<Integer> list = new ArrayList<>();

        for(int i=1;i<=numberOfPages;i++){
            boolean pick=false;
            for(int pi : pageNum) {

                // if page num < 0, means the last one
                if(pi<0){
                    pi = numberOfPages + pi + 1;
                }
                // end

                if(i==pi){
                    pick = true;
                }
            }

            if (pick) {
                list.add(i);
            }

            //reset
            pick = false;
        }

        String rangeString =  pageNum.length==1 ? String.valueOf(pageNum[0]) : (
                String.valueOf(pageNum[0]) + "-" + String.valueOf(pageNum[pageNum.length-1])
        );
        String newPdfFile = StringUtils.removeExtension(pdfFilePath) + "-" + rangeString + ".pdf";

        reader.selectPages(list);
        PdfStamper stamp = new PdfStamper(reader, new FileOutputStream(newPdfFile));
        stamp.close();
        reader.close();
    }

    public static  void splitPage(String pdfFilePath, int pagesOfNum) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(pdfFilePath);
        int numberOfPages = reader.getNumberOfPages();
        reader.close();

        java.util.List<Integer> list = new ArrayList<>();
        
        // new file dir
        String pdfSplitPath = StringUtils.removeExtension(pdfFilePath);
        File theDir = new File(pdfSplitPath);
        if(!theDir.exists()){
            try{
                theDir.mkdir();
            } 
            catch(SecurityException se){
            }        
        }

        for(int i=1;i<=numberOfPages;i++){

            list.add(i);

            // new pdf or last pdf
            if(i%pagesOfNum == 0 || i==numberOfPages){
                // first page and the past page
                String rangeString =  String.valueOf(i- pagesOfNum + 1) + "-" + String.valueOf(i);
                String newPdfFile = pdfSplitPath + "/" + StringUtils.getBaseName(pdfFilePath) + "-" + rangeString + ".pdf";

                File checkNewFile = new File(newPdfFile);
                if(!checkNewFile.exists()){
                    System.out.println(newPdfFile);

                    PdfReader nextReader = new PdfReader(pdfFilePath);

                    nextReader.selectPages(list);
                    PdfStamper stamp = new PdfStamper(nextReader, new FileOutputStream(newPdfFile));
                    stamp.close();
                    nextReader.close();
                }

                // reset, and go next
                list.clear();
            }
        }

        
    }


    public static  void deletePage(String pdfFilePath, int[] pageNum) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(pdfFilePath);
        int numberOfPages = reader.getNumberOfPages();
        java.util.List<Integer> list = new ArrayList<>();

        for(int i=1;i<=numberOfPages;i++){
            boolean skip=false;
            for(int pi : pageNum) {

                // if page num < 0, means the last one
                if(pi<0){
                    pi = numberOfPages + pi + 1;
                }
                // end

                if(i==pi){
                    skip = true;
                }
            }
            if (!skip) {
                list.add(i);
            }

            //reset
            skip = false;
        }

        String newPdfFile = StringUtils.removeExtension(pdfFilePath) + "-new.pdf";

        reader.selectPages(list);
        PdfStamper stamp = new PdfStamper(reader, new FileOutputStream(newPdfFile));
        stamp.close();
        reader.close();
    }
}
