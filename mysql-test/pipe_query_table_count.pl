#!/bin/perl

while(<>){
   my $table = $_;
   $table =~ s/[\t\n\r]*$//;
   print "$table \t\t";

   my $cnt = `query_table_count.sh $table`;
   print $cnt;
}


