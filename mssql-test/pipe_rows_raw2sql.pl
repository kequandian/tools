#!/bin/perl

while(<>){
   my $desc_input = $_;
   $desc_input =~ s/[\t\r\n]*$//;
   my $table_name;
   {
      my $in = $desc_input;
      my @ar = split(/\//, $in);
      my $m = pop(@ar);
      $m =~ /^(\w+)/;
      $table_name = $1;
   }
   #print $table_name;next;

   if(!(-f $desc_input)){
      next;
   }
   my $desc_out = "$desc_input.sql";
   #print $desc_out."\n";

   my @lines;
   open my $fh, "<", "$desc_input";
   @lines = <$fh>;
   close $fh;
   if(@lines==0){
      next;
   }

   my @sqls;
   foreach(@lines){
      my $sql = "INSERT INTO $table_name VALUES (";
      my $line = $_;
      $line =~ s/[\s\r\n\t]+$//;

      $line =~ tr/\|/,/;	  
      #if($line =~ /\,$/){  ##end with ',', append null
      #   $line = $line."null";
      #}
      #if($line =~ /^\,/){
      #   $line =~ "null".$line;
      #}
      
      ## unhex
      ##if($line =~ /\'[0-9A-F]{16}\'/){
      ##    $line =~ /(\'[0-9A-F]{16}\')/;
      ##    my $ss = $1;
      ##    $line =~ s/$ss/X$ss/;
      ##}

      $sql = $sql."$line";
      $sql = $sql.");\n";
      #print $sql;

      push(@sqls, $sql);
   }

   print $desc_out."\n";
   if(@sqls > 0){
     open (OUT, ">$desc_out");
     foreach(@sqls){
        my $sql = $_;
	    print OUT $sql;
     }
     close (OUT);
   }
}

