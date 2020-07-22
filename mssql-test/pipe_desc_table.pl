#!/bin/perl

if(@ARGV!=1){
   print "Usage:\n";
   print "   pipe_desc_table.pl <target-dir>\n";
   exit(0);
}

my $param_dir = shift @ARGV;
if(!(-d $param_dir)){
   print "$param_dir dir not exist!\n";
   exit(1);
}

while(<>){
   my $table = $_;
   $table =~ s/[\t\n\r]*$//;
   print "\n";
   print "$table\n";
   print "-----------------\n";

   my $desc = `desc_table.sh $table`;
   print $desc;

   ## save to file
   my $target = "$param_dir/$table.desc";
   open (OUT, ">$target");
   print OUT $desc;
   close (OUT);
}

