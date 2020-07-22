#!/usr/bin/perl

while(<>){
   my $input = $_;
   $input =~ s/[\s\r\n]+$//;
   
   my $output = $input;
   $output =~ s/\.[0-9]+//;
   print $output."\n";
   `mv $input $output`;
}
