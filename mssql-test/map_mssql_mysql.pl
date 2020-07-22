#!/bin/perl
use Data::Dumper;

if(@ARGV == 0){
    print "Usage:\n";
    print "   map_mssql_mysql.pl <sql-file> [target-dir]\n";
    exit(0);
}

my ($param_file, $param_target) = @ARGV;
if($param_target){
   if(!(-d $param_target)){
      print "$param_target is not a directory\n";
      exit(0);
   }
}

## creating hash map
my @mapping = <DATA>;
my $hash = {};

foreach(@mapping){
    my $line = $_;
    $line =~ s/\s+$//;
    my @lis = split(/\s/,$line);
    my ($ms,$my) = @lis;

    $hash->{$ms} = $my;
} 
#print Dumper($hash);


my $sql= $param_file;

if(-f $param_file){
   local $/;
   open my $fh, "<", "$param_file";
   $sql = <$fh>; 
   close $fh;
}
#print $sql;
my @list;

my @sqls = split(/\;/, $sql);
foreach(@sqls){
   my $line = $_;
   $line =~ s/^[\s\r\n\t]+//;
   $line =~ s/[\s\t\r\n]+$//;

   if($line =~ /CREATE /i){
       $line = &convert_create_sql($line);

       push(@list, $line);

   }else{
      push(@list, $line);
   }
}


## get new sql
my $convertedSql = join(";\n", @list);
if(!$param_target){
   print $convertedSql;
}else{
   ## save to file
   ## get file 
   my $output = $param_file;

   if($param_file =~ /\//){
      $param_file =~ /\/(.+)$/;
      $output = $1;
   }
   $output = "$param_target/$output";

   ## print progress
   print "$param_file";
   print "=>$output\n";

   open (OUT, ">", $output);
   print OUT $convertedSql;
   close OUT;
}



sub convert_create_sql{
    my $sql = shift;
    my ($head,$tail);
    
    $sql =~ /^(.+)\(/;
    $head = $1;

    ## remote head and tail
    $sql =~ s/^.+\(//;
    $sql =~ s/\)$//;

    ## remote return
    $sql =~ s/[\r\n]//g;

    
    ## get fields
    my @fields;
    my @list = split(/\,/, $sql);
    foreach(@list){
       my $field = $_;
       $field =~ /^(\w+)\s+([\w\(\)]+)/; 
       my $col = $1;
       my $data_type = $2;
       my $info = $field;
       
       $info =~ s/^\w+\s+[\w\(\)]+//;
       $info =~ s/^\s+//;
       #print "(col,data_type,info)=>($col,$data_type,$info)\n";

       ## map
       my $DATA_TYPE = $data_type;
       $DATA_TYPE =~ tr/[a-z]/[A-Z]/;
	   
       if(exists $hash->{$DATA_TYPE}){       
           #print $hash->{$DATA_TYPE}."\n";
           $data_type = $hash->{$DATA_TYPE};
        }
   
        $field = "$col $data_type";
        if($info){
           $field = "$field $info";
        }

        push(@fields, $field);
    }
	
	$sql = join(",\n", @fields);
	$sql = "$head(\n$sql\n)";
	
    ## create new sql
    return $sql;
}






__DATA__
BIT TINYINT(1)
REAL FLOAT
NUMERIC	DECIMAL
MONEY DECIMAL
SMALLMONEY DECIMAL
CHAR CHAR
NCHAR CHAR
NVARCHAR VARCHAR
DATETIME2 DATETIME
MALLDATETIME DATETIME
DATETIMEOFFSET DATETIME
ROWVERSION TIMESTAMP
TEXT MEDIUMTEXT
IMAGE MEDIUMBLOB
SQL_VARIANT [not-migrated]
TABLE [not-migrated]
HIERARCHYID [not-migrated]
UNIQUEIDENTIFIER VARCHAR(64)
SYSNAME VARCHAR(160)
XML TEXT
TIMESTAMP VARBINARY(8)
