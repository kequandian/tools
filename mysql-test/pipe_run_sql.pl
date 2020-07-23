#!/bin/perl

while(<>){

    my $file = $_;
    $file =~ s/[\s\r\n\t]+//g;
    if(!(-f $file)){
       next;
    }
    
    print $file."\n";
    print `run_sql_whth.sh $file`;
}
