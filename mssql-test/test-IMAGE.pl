#!/bin/perl

#print "This script is used to convert [IMAGE] to null for sql\n";

while(<>){
     my $file = $_;
     $file =~ s/[\r\n\s\t]+//;  
     if(!(-f $file)){
        next;
     }
     my $content;

     local $/;
     open my $fh, "<", "$file";
     $content= <$fh>;
     close $fh;
     
     if($content =~ /\[IMAGE\]/){
        $content =~ s/\[IMAGE\]/null/g;
     }
     #print $content;
     
     my $output;
     if($file =~ /\.[0-9]+$/){
        $file =~ /\.([0-9]+)/;
	my $n = $1;
	$n = $n+1;
	$file =~ s/\.[0-9]+$//;
	$output = "$file.$n";
     }else{
        $output = "$file.1";
     }

     print $output."\n";
     open my $wh, ">", "$output";
     print $wh $content;
     close $wh;
}
