package com.tools.checksum;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import org.apache.commons.cli.*;

import java.io.File;

public class Main {

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        Option adler32 = new Option("a", "adler32", false, "");
        options.addOption(adler32);
        Option crc32 = new Option("c", "crc32", false, "");
        options.addOption(crc32);
        Option crc32c = new Option("C", "crc32c", false, "");
        options.addOption(crc32c);
        Option md5 = new Option("m", "md5", false, "");
        options.addOption(md5);
        Option sha1 = new Option("s", "sha1", false, "");
        options.addOption(sha1);
        Option sha256 = new Option("S", "sha256", false, "");
        options.addOption(sha256);
        Option sha512 = new Option("X", "sha512", false, "");
        options.addOption(sha512);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;//not a good practice, it serves it purpose

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("checksum", options);
            System.exit(1);
        }
        if(cmd.getArgList()==null || cmd.getArgList().size()==0){
            formatter.printHelp("checksum", options);
            System.exit(1);
        }

//        cmd.getArgList().forEach(
//                arg->System.out.println(arg)
//        );

        //Files.hash(cmd.get, Hashing.adler32()).padToLong()
//        Stream.of(cmd.getOptions()).forEach(
//                opt->System.out.println(opt)
//        );

        String target = cmd.getArgList().get(0);
        File targetFile = new File(target);

        HashCode checksumCode = Files.hash(targetFile, Hashing.md5());

        String opt = (cmd.getOptions()!=null&&cmd.getOptions().length>0)?
                        cmd.getOptions()[0].getLongOpt() : "sha256";

        if(opt!=null && opt.length()>0) {
            final String[] supportedType  =new String[]{"adler32","crc32","crc32c","md5","sha1","sha256","sha512",
                    "adler32l","crc32l","crc32cl","md5l","sha1l","sha256l","sha512l"};
            //Assert.isTrue(Stream.of(supportedType).collect(Collectors.toList()).contains(opt), "supported type: " + String.join(",", supportedType));
            switch (opt) {
                case "adler32":
                case "adler32l":
                    checksumCode = Files.hash(targetFile, Hashing.adler32());
                    break;
                case "crc32":
                case "crc32l":
                    checksumCode = Files.hash(targetFile, Hashing.crc32());
                    break;
                case "crc32c":
                case "crc32cl":
                    checksumCode = Files.hash(targetFile, Hashing.crc32c());
                    break;
                case "md5":
                case "md5l":
                    checksumCode = Files.hash(targetFile, Hashing.md5());
                    break;
                case "sha1":
                case "sha1l":
                    checksumCode = Files.hash(targetFile, Hashing.sha1());
                    break;
                case "sha256":
                case "sha256l":
                    checksumCode = Files.hash(targetFile, Hashing.sha256());
                    break;
                case "sha512":
                case "sha512l":
                    checksumCode = Files.hash(targetFile, Hashing.sha512());
                    break;
                default:
                    checksumCode = Files.hash(targetFile, Hashing.sha256());
                    break;
            }
        }else{
            opt = "adler32l";
        }

        System.out.println(checksumCode.toString()+" " + opt);
    }
}
