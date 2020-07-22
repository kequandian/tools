#!/bin/perl

my $param_target = shift @ARGV;

if($param_target){
   if(!(-d $param_target)){
      print "$param_target is not a directory\n";
      exit(0);
   } 
}


while(<>){

    my $sql_file = $_;
    $sql_file =~ s/[\r\n]+$//;

    print `map_mssql_mysql.pl $sql_file $param_target`;
}
