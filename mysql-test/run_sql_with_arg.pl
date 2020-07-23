#!/usr/bin/perl

if(@ARGV==0){
  print "Usage:\n";
  print "   run_sql_with.pl <sql-file> [param]\n";
  exit(0);
}

my $param_file = shift @ARGV;
my $param_1 = shift @ARGV;

if( !(-e $param_file)){
   print "$param_file not exists\n";
   exit(0);
}

my @sqls_raw;
#local $/;
open my $fh, "<", "$param_file";
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


## check sql 
if(!$param_1 && $sql =~ /\$1/){
   print "found param required!\n";
   print " - please check: $param_file\n";
   exit(1);
}

my $sql = join(" ", @sqls);
$sql =~ s/\`/\\\`/g;

$sql =~ s/[\t\s\r\n]+/ /g;
if($param_1){
   $sql =~ s/\$1/$param_1/;
}

#print `./run_sql.sh $sql`;
#exit(0);

my @sql_lines = split(/\;/,$sql);
foreach(@sql_lines){
   $sql = $_;
   $sql =~ s/^\s+//;
   $sql =~ s/\s+$//;
   if(length($sql)==0){
      next;
   }

   $sql = "\"$sql\"";
   my $result = `./run_sql.sh $sql`;
   print $result;
}   

