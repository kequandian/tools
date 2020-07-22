#!/bin/perl

#print "This script is used to convert TIMESTAMP to DEFAULT for sql server\n";

while(<>){
     my $file = $_;
     $file =~ s/[\r\n\s\t]+//;  
     if(!(-f $file)){
        next;
     }
     my $content;
     my @lines;

     open my $fh, "<", "$file";
     @lines= <$fh>;
     close $fh;
     
     my @list;
     foreach(@lines){
        my $line = $_;
	if($line =~ /\,\'[0-9A-F]{16}\'\,/){
	    $line =~ /\,(\'[0-9A-F]{16}\')\,/;
	    my $ss = $1;
	    $line =~ s/$ss/DEFAULT/;
	}
	push(@list, $line);
     }
     $content = join('',@list);
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
