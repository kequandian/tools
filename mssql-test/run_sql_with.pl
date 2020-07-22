#!/usr/bin/perl

if(@ARGV==0){
  print "Usage:\n";
  print "   run_sql_with.pl <file contains sql>\n";
  exit(0);
}

my $param_file = shift @ARGV;
my $param_1 = shift @ARGV;


my $sql;
local $/;
open my $fh, "<", "$param_file";
$sql = <$fh>;
close $fh;

## check sql 
if(!$param_1 && $sql =~ /\$1/){
   print "found param required!\n";
   print " - please check: $param_file\n";
   exit(1);
}

$sql =~ s/[\t\s\r\n]+/ /g;
if($param_1){
   $sql =~ s/\$1/$param_1/;
}
$sql = "\"$sql\"";

my $result = `./run_sql.sh $sql`;
print $result;


