# define the mssql-test alias
$ cp app.ZHBG.alias mssql-test.alias

# dump table description
./show_tables.sh | pipe_desc_table.pl <target dir>

# translate desc lines into sql  file
$ find <target> | pipe_desc_raw2sql.pl


# query table data 

