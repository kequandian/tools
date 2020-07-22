#!/bin/perl

while(<>){
   my $desc_input = $_;
   $desc_input =~ s/[\t\r\n]*$//;

   if(!(-f $desc_input)){
      next;
   }
   my $desc_out = "$desc_input.sql";
   print $desc_out."\n";

   my @lines;
   open my $fh, "<", "$desc_input";
   @lines = <$fh>;
   close $fh;
   if(@lines==0){
      next;
   }

   ## start to build sql
   my $table_name;
   {
       my $line0 = $lines[0];
       my @arr0 = split(/\|/, $line0);
       $table_name = $arr0[0];
   }
   my $sql = "DROP TABLE IF EXISTS $table_name;\n";
   $sql = $sql."CREATE TABLE IF NOT EXISTS $table_name (\n";
   #print $sql;
   
   foreach(@lines){
      my $line = $_;
      my @arr = split(/\|/, $line);
      shift @arr;
      my $column_name = shift @arr;
      if($column_name eq 'id'){
         next; ## auto created
      }
      my $data_type = shift @arr;
      my $char_len = shift @arr;
      my $field = "$column_name $data_type";
      if($char_len>0 && $char_len<=2048){
         $field = $field."($char_len)";
      }elsif($data_type eq 'image' || $data_type eq 'text'){
         ## ignore length and skip die 
      }elsif($char_len > 2048){
         die "Field len is greater than 255: [$column_name,$char_len]";
      }
      shift @arr; ## shift number len, ignore;

      my $is_null = shift @arr;
      if($is_null eq 'NO'){
         $field = $field." NOT NULL";
      }
      my $default = shift @arr;
      if($default ne 'null'){
         $field = $field." DEFAULT $default";
      }

      my $key = shift @arr;
      if($key =~ /\S+/){
          $key =~ s/[\s\r\n\t]+$//;
          $field = $field." $key";
      }
      $field =~ s/^\s+//;
      $field =~ s/\s+$//;
      #print $field."\n";
      $sql = $sql."$field,\n";
   }
   $sql =~ s/\,$//;
   $sql = $sql.");\n";
   #print $sql;next;

   if(length($sql) > 0){
     open (OUT, ">$desc_out");
     print OUT $sql;
     close (OUT);
   }
}

