#!/usr/bin/perl

if(@ARGV==0){
  print "Usage:\n";
  print "   run_sql_with.pl <sql-file>\n";
  exit(0);
}

my $input = shift @ARGV;
if( !(-e $input)){
   print "$input not exists\n";
   exit(0);
}

my @sqls_raw;
#local $/;
open my $fh, "<", "$input";
@sqls_raw = <$fh>;
close $fh;

my @sqls;
foreach (@sqls_raw){
  if(/^--/){
     next;
  }else{
     push(@sqls, $_);
  }
}

my $sql = join(" ", @sqls);
$sql =~ s/\`/\\\`/g;
$sql =~ s/[\t\s\r\n]+/ /g;


##
#my @sql_lines = split(/\;/,$sql);
#foreach(@sql_lines){
#   $sql = $_;
#   $sql =~ s/^\s+//;
#   $sql =~ s/\s+$//;
#   if(length($sql)==0){
#      next;
#   }
#
#   $sql = "\"$sql\"";
#}   


my $dir = `dirname $0`;
$dir =~ s/[\r\t\n]*$//;

$sql = "\"$sql\"";
my $result =`$dir/run_sql.sh $sql`;
print $result;

