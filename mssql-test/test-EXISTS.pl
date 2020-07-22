#!/usr/bin/perl

while(<>){
   my $input = $_;
   $input =~ s/[\s\r\n]+$//;
   my $output = `./tail-num.pl $input`;


   print $output."\n";

   `perl -p -e 's/IF EXISTS //' $input > $output`;
}

