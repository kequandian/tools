#!/bin/perl

while(<>){
    print;
    my $tab = $_;
    $tab =~ s/[\s\r\n]+$//;

    my $sql = "\"delete from $tab\"";
    #print $sql."\n";
    print `./run_sql.sh $sql`;
}
