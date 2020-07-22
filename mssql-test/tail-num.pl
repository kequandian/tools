#!/usr/bin/perl

if(@ARGV==0){
   print "Usage:\n";
   print "   test-tail-num.pl <line.1>\n";
   exit(0);
}

my $input = shift @ARGV;
$inpu =~ s/[\s\r\n]+$//;

     if($input =~ /\.[0-9]+$/){
        $input =~ /\.([0-9]+)/;
	my $n = $1;
	$n = $n+1;
	$input =~ s/\.[0-9]+$//;
	$output = "$input.$n";
     }else{
        $output = "$input.1";
     }

print $output;
 
