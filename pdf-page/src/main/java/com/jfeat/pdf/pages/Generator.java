package com.jfeat.pdf.pages;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfContentByte;
import com.jfeat.pdf.pages.util.ImageUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by vincenthuang on 16/10/26.
 */
public class Generator {

    public static void printUsage(){
        System.out.println("Usage: pdf-page <source.pdf> <OPTIONS> <PARAM> [..]");
        System.out.println("       ");
        System.out.println("Page range support 1,2 [3-5], -1 mean last page.");
        System.out.println("OPTIONS:");
        System.out.println(" -a,--all     push all image or .pdf files within path into a new single pdf file");
        System.out.println("              e.g. pdf-page <new.pdf> -a <image-dir|pdf-dir>");
        System.out.println(" -m,--merge   merge two .pdf files into one pdf file");
        System.out.println("              e.g. pdf-page <source> -m <page2.pdf>");
        System.out.println(" -h,--head    Head with images/pages.");
        System.out.println("              e.g. pdf-page <source> -h <image-url|image-dir|pages.pdf>");
        System.out.println(" -t,--tail    Tail up images/pages");
        System.out.println("              e.g. pdf-page <source> -t <image-url|image-dir|pages.pdf>");
        System.out.println(" -p,--pick    Pick up the range pages as new file.(page-range format: 1:2)");
        System.out.println("              e.g. pdf-page <source> -p <page-range>");
        //System.out.println(" -u,--under   overlay one page onto another page");
        //System.out.println("              e.g. pdf-page <source> -o <page2.pdf>");
        System.out.println(" -d,--delete  Delete page.");
        System.out.println("              e.g. pdf-page <source> -d <page-range>");
        System.out.println(" -s,--split   Split the .pdf into multi ones with certain number of pages.");
        System.out.println("              e.g. pdf-page <source> -s <pages>");
        System.out.println(" -c,--cover   Cover specific area on page.");
        System.out.println("              e.g. pdf-page <source> -c <page-number> <pos> <size>");
        System.out.println(" -r,--ruler   Draw a ruler on page.");
        System.out.println("              e.g. pdf-page <source> -r <page-number>");
        System.out.println(" -T,--text    Draw text on page.");
        System.out.println("              e.g. pdf-page <source> -t <page-number> <posx> <posy> <text> <LEFT|CENTER|RIGHT> <fontSize> [fontFamily]");
        System.out.println(" -n,--number  Number the page.");
        System.out.println("              e.g. pdf-page <source> -n <page-number>");
    }

    public static void main(String[] args) throws IOException {
        //args = new String[]{"C:\\Users\\vincent\\Desktop\\就业政策综合培训通知.pdf","-r","5"};
        //args = new String[]{"C:\\Users\\vincent\\Desktop\\就业政策综合培训通知.pdf","-T","5","230","530","GZ","LEFT","12","宋体"};
        if (args == null || (args.length < 3)) {
            printUsage();
            return;
        }

        String pdfFilePath = args[0];
        /// ensure first file is pdf
        if (!pdfFilePath.endsWith(".pdf")) {
            System.out.println("invalid pdf: no an valid pdf file : " + pdfFilePath);
            return;
        }

        String op = args.length > 1 ? args[1] : null;
        String param = args.length > 2 ? args[2] : null;
        String param2 = args.length > 3 ? args[3] : null;

        //System.out.println(new File(".").getCanonicalPath());

        try {
            if (op.equals("-m") || op.equals("--merge")){
                String mergePdf = param;
                PdfPages.mergePdfPages(pdfFilePath, mergePdf);

            } else if (op.equals("-a") || op.equals("--all")) {
                String url = param;
//                boolean is_image_param =   op.equals("-a")  || op.equals("--all");
//                boolean is_pdf_param =   op.equals("-A")  || op.equals("--path");

                List<String> fileUrls = new ArrayList<>();
                boolean isFromWeb = ImageUtil.isFromWeb(url);
                File checkFile = new File(url);

                if (isFromWeb) {
                    fileUrls.add(url);
                } else if (checkFile.exists()) {
                    if (checkFile.isDirectory()) {
                        File[] listOfFiles = checkFile.listFiles();


                        for (File f : listOfFiles) {
                            String filename = f.getName();
                            if(!filename.contains(".")) continue;

                            String ext = filename.substring(filename.indexOf("."));
                            if(
                                    ((ext.endsWith(".png") || ext.endsWith(".jpg") || ext.endsWith(".jpeg"))) ||
                                    (ext.endsWith(".pdf"))
                            ) {
                                if (f.isFile()) {
                                    //System.out.println(f.getCanonicalPath().replace(new File(url).getCanonicalPath(), ""));
                                    fileUrls.add(f.getCanonicalPath());
                                }
                            }
                        }
                    } else {
                        // insert images
                        fileUrls.add(url);
                    }

                } else {
                    System.out.println("fatal: image file not exists: " + url);
                    return;
                }
                if(fileUrls==null || fileUrls.size()==0){
                    return;
                }

                //sort by name
                fileUrls.sort(new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        return o1.compareToIgnoreCase(o2);
                    }
                });

                if(new File(pdfFilePath).exists()){
                    PdfPages.addPage(pdfFilePath, fileUrls.toArray(new String[0]));
                }else {
                    if(fileUrls.get(0).endsWith(".pdf")){
                        // get the first pdf file as the source file
                        //String firstPdfFile = fileUrls.remove(0);
                        PdfPages.createPages(pdfFilePath, fileUrls.toArray(new String[0]));
                    }else {
                        PdfPages.createImagesPages(pdfFilePath, fileUrls.toArray(new String[0]));
                    }
                }

            } else if (op.equals("-h") || op.equals("--head")) {
                    String url = param;

                    List<String> imageUrls = new ArrayList<>();
                    boolean isFromWeb = ImageUtil.isFromWeb(url);
                    File checkFile = new File(url);

                    if (isFromWeb) {
                        imageUrls.add(url);
                    } else if (checkFile.exists()) {
                        if (checkFile.isDirectory()) {
                            File[] listOfFiles = checkFile.listFiles();
                            for (File f : listOfFiles) {
                                if (f.isFile()) {
                                    imageUrls.add(f.getAbsolutePath());
                                }
                            }
                        } else {
                            // insert images
                            imageUrls.add(url);
                        }

                    } else {
                        System.out.println("fatal: image file not exists: " + url);
                        return;
                    }

                    PdfPages.insertPage(pdfFilePath, imageUrls.toArray(new String[0]));

            } else if (op.equals("-t") || op.equals("--tail")) {
                /// tail up pages
                String url = param;

                List<String> imageUrls = new ArrayList<>();
                boolean isFromWeb = ImageUtil.isFromWeb(url);
                File checkFile = new File(url);

                if (isFromWeb) {
                    imageUrls.add(url);
                } else if(checkFile.exists()) {
                    if(checkFile.isDirectory()){
                        File[] listOfFiles = checkFile.listFiles();
                        for (File f : listOfFiles) {
                            if (f.isFile()) {
                                imageUrls.add(f.getAbsolutePath());
                            }
                        }
                    }
                }else {
                    System.out.println("fatal: input file not exists: " + url);
                    return;
                }

                File f = new File(pdfFilePath);
                if (f.exists()) {
                    PdfPages.addPage(pdfFilePath, imageUrls.toArray(new String[0]));
                } else {
                    PdfPages.createImagesPages(pdfFilePath, imageUrls.toArray(new String[0]));
                }

            }else if(op.equals("-n") || op.equals("--number")){
                int pageNumber = Integer.parseInt(param);
                PdfPageNum.printPageNumber(pdfFilePath, pageNumber);

            }
            else if (op.equals("-c") || op.equals("--cover")) {
                if(args.length<5){
                    printUsage();
                    return;
                }
                int pageNumber = Integer.parseInt(param);
                int pos = Integer.parseInt(args[3]);
                int size = Integer.parseInt(args[4]);

                PdfCover.coverPage(pdfFilePath, pageNumber, pos, size);

            } else if (op.equals("-r") || op.equals("--ruler")) {
                if (args.length < 3) {
                    printUsage();
                    return;
                }
                int pageNumber = Integer.parseInt(param);

                PdfRuler.drawRuler(pdfFilePath, pageNumber);

            }else if (op.equals("-T") || op.equals("--text")) {
                if(args.length<8){
                    printUsage();
                    return;
                }
                int pageNumber = Integer.parseInt(param);
                int posx = Integer.parseInt(args[3]);
                int posy = Integer.parseInt(args[4]);
                String text = args[5];
                String alignmentString = args[6];
                float fontSize = Integer.parseInt(args[7]);
                String fontFamily = args.length>8 ? args[8] : "";

                int alignment = "CENTER".compareTo(alignmentString)==0 ? PdfContentByte.ALIGN_CENTER
                        : ("RIGHT".compareTo(alignmentString)==0 ? PdfContentByte.ALIGN_RIGHT : PdfContentByte.ALIGN_LEFT);

                PdfText.drawText(pdfFilePath, pageNumber, posx, posy, text, alignment, fontSize, fontFamily);

            } else if (op.equals("-d") || op.equals("--delete")) {
                // delete page
                String range = param;
                int[] range_num = new int[0];
                if (range.contains(",")) {
                    String[] nums = range.split(",");
                    range_num = new int[nums.length];

                    for (int i = 0; i < nums.length; i++) {
                        int pi = Integer.parseInt(nums[i]);
                        range_num[i] = pi;
                    }
                } else if ( (range.contains("-") && range.indexOf("-") > 0) ||
                            (range.contains(":") && range.indexOf(":") > 0)
                          ) {
                    String[] nums = range.contains(":")? range.split(":") : range.split("-");

                    int starti = Integer.parseInt(nums[0]);
                    int endi = Integer.parseInt(nums[1]);
                    range_num = new int[endi - starti + 1];

                    for (int i = starti; i <= endi; i++) {
                        range_num[i - starti] = i;
                    }
                } else {
                    range_num = new int[1];
                    range_num[0] = Integer.parseInt(range);
                }

                PdfPages.deletePage(pdfFilePath, range_num);

            } else if (op.equals("-p") || op.equals("--pick")) {
                // pick the pages within range number
                String range = param;
                int[] range_num = new int[0];
                if (range.contains(",")) {
                    String[] nums = range.split(",");
                    range_num = new int[nums.length];

                    for (int i = 0; i < nums.length; i++) {
                        int pi = Integer.parseInt(nums[i]);
                        range_num[i] = pi;
                    }
                } else if ( (range.contains("-") && range.indexOf("-") > 0) ||
                            (range.contains(":") && range.indexOf(":") > 0) ) {
                    String[] nums = range.contains(":")?range.split(":") : range.split("-");

                    int starti = Integer.parseInt(nums[0]);
                    int endi = Integer.parseInt(nums[1]);
                    range_num = new int[endi - starti + 1];

                    for (int i = starti; i <= endi; i++) {
                        range_num[i - starti] = i;
                    }
                } else {
                    range_num = new int[1];
                    range_num[0] = Integer.parseInt(range);
                }

                PdfPages.pickPage(pdfFilePath, range_num);

            } else if (op.equals("-s") || op.equals("--split")) {
                int number_page = Integer.parseInt(param);
                PdfPages.splitPage(pdfFilePath, number_page);

            } else if (op.equals("-u") || op.equals("--under")) {
                String mergePdf = param;
                String rangePick = param2;
                if (rangePick == null) {
                    rangePick = "1:1";
                }

                if (!rangePick.contains(":")) {
                    printUsage();
                    return;
                }
                String[] ranges = rangePick.split(":");
                if (ranges.length != 2) {
                    printUsage();
                    return;
                }

                try {
                    int num1 = Integer.parseInt(ranges[0]);
                    int num2 = Integer.parseInt(ranges[1]);

                    PdfPages.mergePages(pdfFilePath, num1, mergePdf, num2);
                } catch (NumberFormatException e) {
                    printUsage();
                    return;
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("fatal: invalid page-num: " + args[1]);
        } catch (IOException e) {
            System.out.println("fatal: file not found: " + pdfFilePath);
        } catch (DocumentException e) {
            System.out.println("fatal: " + e.toString());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
